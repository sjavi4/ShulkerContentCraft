package me.autobot.shulkerboxnbtcrafter;


import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public final class ShulkerBoxNBTCrafter extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    static class shulk {
        ItemStack createContent(ItemStack item, int itemCount) {
            ItemStack[] craftedItem = new ItemStack[27];
            for (int i = 0; i < craftedItem.length; i++) {
                craftedItem[i] = new ItemStack(item.getType(), itemCount);
            }
            ItemStack boxedItem = new ItemStack(Material.SHULKER_BOX, 1);
            //Register New Shulker Box for result
            BlockStateMeta bsm = (BlockStateMeta) boxedItem.getItemMeta();
            ShulkerBox box = (ShulkerBox) bsm.getBlockState();
            Inventory boxInv = box.getInventory();
            boxInv.setContents(craftedItem);
            bsm.setBlockState(box);
            boxedItem.setItemMeta(bsm);
            box.update();
            return boxedItem;
        }
    }
    static class recipe {
        ItemStack getrecipe(ItemStack[] itemStacks) {
            Recipe resultedRecipe = Bukkit.getCraftingRecipe(itemStacks, Bukkit.getWorlds().get(0));
            if (resultedRecipe == null) {return null;}
            ItemStack resultedItem = resultedRecipe.getResult();
            return resultedItem;
        }
    }
    @EventHandler
    public void handleCustomCrafting(PrepareItemCraftEvent event) {

        CraftingInventory inventory = event.getInventory();
        if (inventory.isEmpty() || !inventory.contains(Material.SHULKER_BOX)) { //Return if empty or no shulker box
            return;
        }
        //Stores shulker box's inventory as 1 itemstack

        ItemStack[] nbtContainsType = new ItemStack[9];
        int[] nbtcounts = new int[9];
        int index = 0;
        int inputCount = 0;
        ItemStack FirstItem = null;

        //Iterate all items in Crafting table inventory
        for (ItemStack item : inventory.getMatrix()) {

            //Only Shulker Box
            if (item == null || item.getType() != Material.SHULKER_BOX) {index++; continue;}
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta ItemMeta = (BlockStateMeta) item.getItemMeta();
                if (ItemMeta.getBlockState() instanceof ShulkerBox) {
                    inputCount++;
                    ShulkerBox shulker = (ShulkerBox) ItemMeta.getBlockState();
                    //Ensure Each shulker box contains only 1 type of full stacked items
                    if (!shulker.getInventory().isEmpty()) {
                        FirstItem = shulker.getInventory().getContents()[0];
                        if (FirstItem == null) {
                            index++;
                            continue;
                        }
                        for (ItemStack nbtItemStack : shulker.getInventory()) {
                            if (nbtItemStack == null
                                    || nbtItemStack.getAmount() != nbtItemStack.getMaxStackSize()
                                    || FirstItem.getType() != nbtItemStack.getType()
                            ) {
                                break;
                            }
                            nbtContainsType[index] = new ItemStack(FirstItem);
                            nbtcounts[index] = FirstItem.getAmount();
                        }
                    }
                }
            }
            index++;
        }

        ItemStack resultedItem = new recipe().getrecipe(nbtContainsType);
        if (resultedItem == null) {return;}
        int resultedCount = resultedItem.getAmount();
        /*
        Recipe resultedRecipe = Bukkit.getCraftingRecipe(nbtContainsType, Bukkit.getWorlds().get(0));
        if (resultedRecipe == null) {return;}
        ItemStack resultedItem = resultedRecipe.getResult();
        int resultedCount = resultedItem.getAmount();
         */

        //Check for numbers of shulker boxes in crafting table
        int[] filteredArray = Arrays.stream(nbtcounts).filter(num -> num != 0).toArray();
        Arrays.sort(filteredArray);

        ItemStack boxedItem;
        if (FirstItem != null) {
            if (filteredArray[0] < resultedItem.getMaxStackSize()) {
                boxedItem = new shulk().createContent(resultedItem, filteredArray[0] * resultedItem.getAmount());
            } else {
                boxedItem = new shulk().createContent(resultedItem, resultedItem.getMaxStackSize());
            }
            if (
                    //form count = 1
                    (resultedCount == 1 && resultedItem.getMaxStackSize() == 64
                            //form count > 1 and boxes are sufficient to store (total spaces of box > sum of product)
                            //sum of product = maxstack of product * count
                            //total space of box = 27 * maxstack of product * number of input
                            || (resultedCount > 1 && 27 * resultedItem.getMaxStackSize() * inputCount >= 27 * resultedCount * resultedItem.getMaxStackSize())
                            //form count > 1 and
                            //|| (resultedCount > 1 && filteredArray[0] * resultedCount <= 64 && filteredArray[0] < 64)
                    )
                    && resultedItem.getMaxStackSize() != 1
            ) {
                event.getInventory().setResult(boxedItem);
            }
        }
    }

    @EventHandler
    public void handleCraftingResult(InventoryClickEvent event) {
        int getInputCount = 0;
        int index = 0;
        int[] nbtcounts = new int[9];
        ItemStack[] nbtContainsType = new ItemStack[9];
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.WORKBENCH) {
            CraftingInventory inv = (CraftingInventory) event.getInventory();
            if (((CraftingInventory) event.getInventory()).getResult() == null) {return;}
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {

                for (ItemStack item : inv.getMatrix()) {
                    if (item != null && item.getType() == Material.SHULKER_BOX) {
                        if (item.getItemMeta() instanceof BlockStateMeta) {
                            BlockStateMeta ItemMeta = (BlockStateMeta) item.getItemMeta();
                            if (ItemMeta.getBlockState() instanceof ShulkerBox) {
                                ShulkerBox shulker = (ShulkerBox) ItemMeta.getBlockState();

                                //Ensure Each shulker box contains only 1 type of full stacked items
                                ItemStack FirstItem = shulker.getInventory().getContents()[0];
                                if (!shulker.getInventory().isEmpty()) {
                                    nbtContainsType[index] = new ItemStack(FirstItem);
                                    nbtcounts[index] = FirstItem.getAmount();
                                }
                            }
                        }
                        getInputCount++;
                    }
                    index++;
                }
            }
        }
        int finalGetInputCount = getInputCount;
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.WORKBENCH) {
                    CraftingInventory inv = (CraftingInventory) event.getInventory();
                    if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                        ItemStack resultedItem = new recipe().getrecipe(nbtContainsType);
                        //int resultedCount = resultedItem.getAmount();
                        //Recipe resultedRecipe = Bukkit.getCraftingRecipe(nbtContainsType, Bukkit.getWorlds().get(0));
                        if (resultedItem != null) {
                            for (int i = 0; i < finalGetInputCount; i++) {
                                if (resultedItem.getAmount() == 1) { //Form
                                    if (resultedItem.getType() == Material.HONEY_BLOCK) {
                                        if (i == 1) {
                                            ItemStack boxedItem = new shulk().createContent(new ItemStack(Material.GLASS_BOTTLE), Material.GLASS_BOTTLE.getMaxStackSize());

                                            inv.setItem(i, boxedItem);
                                        } else {
                                            inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                        }
                                    } else if (resultedItem.getType() == Material.ENDER_EYE) {
                                        if (i == 1) {
                                            ItemStack boxedItem = new shulk().createContent(new ItemStack(Material.BLAZE_POWDER), 48);

                                            inv.setItem(i, boxedItem);
                                        } else {
                                            inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                        }
                                    } else {
                                        int exceedbox = 0;
                                        for (ItemStack itemStack : nbtContainsType) {
                                            if (itemStack != null && (itemStack.getMaxStackSize() > resultedItem.getMaxStackSize())) {
                                                exceedbox = itemStack.getMaxStackSize() / resultedItem.getMaxStackSize() -1;
                                                break;
                                            }
                                        }
                                        if (exceedbox > 0 && i <= exceedbox) {
                                            ItemStack boxedItem = new shulk().createContent(resultedItem, resultedItem.getMaxStackSize());

                                            inv.setItem(i, boxedItem);
                                        } else {
                                            inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                        }
                                    }
                                } else {
                                    if (i < resultedItem.getAmount()) {
                                        int[] filteredArray = Arrays.stream(nbtcounts).filter(num -> num != 0).toArray();
                                        Arrays.sort(filteredArray);
                                        if (resultedItem.getAmount() * filteredArray[0] > resultedItem.getMaxStackSize()) {
                                            ItemStack boxedItem;
                                            boxedItem = new shulk().createContent(resultedItem, resultedItem.getMaxStackSize());
                                            inv.setItem(i, boxedItem);
                                        }
                                    } else {
                                        inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        task.runTaskLater(this,0);
    }
}