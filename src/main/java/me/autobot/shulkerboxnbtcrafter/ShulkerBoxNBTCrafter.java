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
import java.util.Comparator;
import java.util.Objects;

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
        int loopIndex = 0;
        int inputCount = 0;
        ItemStack FirstItem = null;

        //Iterate all items in Crafting table inventory
        for (ItemStack item : inventory.getMatrix()) {

            if (item != null && item.getType() != Material.SHULKER_BOX) {return;} //Prevent Non shulker box inside crafting table
            if (item == null) {
                //event.getInventory().setResult(null);
                loopIndex++;
                continue;
            } //loopindex +1 when slot is Empty
            //Only shulkerBox left
            if (item.getItemMeta() instanceof BlockStateMeta) {
                //Inventory check
                BlockStateMeta ItemMeta = (BlockStateMeta) item.getItemMeta();
                if (ItemMeta.getBlockState() instanceof ShulkerBox) {
                    inputCount++; //inputCount +1 when shulkerbox
                    ShulkerBox shulker = (ShulkerBox) ItemMeta.getBlockState();
                    //Ensure Each shulker box contains only 1 type of fully stacked items
                    if (shulker.getInventory().isEmpty()) {loopIndex++;continue;} //Jump next & loopindex + 1 when empty box
                    //Non empty box left
                    FirstItem = shulker.getInventory().getContents()[0]; //Retrive the first item in Box (Must have, other has filtered out)
                    /*
                    if (FirstItem == null) {
                        loopIndex++;
                        continue;
                    }
                     */
                    //Loop for shulker inv
                    for (ItemStack nbtItemStack : shulker.getInventory()) {
                        if (//nbtItemStack == null ||
                                nbtItemStack.getAmount() != nbtItemStack.getMaxStackSize() //Full-stack check
                                || FirstItem.getType() != nbtItemStack.getType() //Mixed check
                        ) {
                            return; //Return if mixed or not fully stacked
                        }
                        //Record recipe ingrediant
                        nbtContainsType[loopIndex] = new ItemStack(FirstItem);
                        nbtcounts[loopIndex] = FirstItem.getAmount();
                    }
                }
            }
            loopIndex++;
        }

        ItemStack resultedItem = new recipe().getrecipe(nbtContainsType); //Check recipe by recorded shape
        if (resultedItem == null) {return;}
        int resultedCount = resultedItem.getAmount(); //get result item count

        //Check for numbers of shulker boxes in crafting table

        int[] filteredArray = Arrays.stream(nbtcounts).filter(num -> num != 0).toArray(); //empty = 0, remove emptys, get maxcounts of each item
        Arrays.sort(filteredArray); //sort by acending

        ItemStack boxedItem; //initialize the result in shulker box
        if (FirstItem != null) { //Prevent empty crafting
            if (filteredArray[0] < resultedItem.getMaxStackSize()) { //min maxstack of item < result item in maxstack, 16->32/64 ; 32 -> 64
                //Prepare a shulker in min-maxstack of ingrediant of resulted item * result count
                boxedItem = new shulk().createContent(resultedItem, filteredArray[0] * resultedItem.getAmount());
            } else { //min maxstack of item >= result item in maxstack, 16->16 ; 32->16/32 ; 64->16/32/64
                //Prepare a shulker in maxstack of the result item
                boxedItem = new shulk().createContent(resultedItem, resultedItem.getMaxStackSize());
            }

            int privided = resultedItem.getMaxStackSize() * inputCount; //calc the numbers of slots can be used ([shulker inv] * result.maxstack * shulker input)
            int required = filteredArray[0] * resultedCount; //calc the numbers of slots will be used ([shulker inv] * [maxstack] * result counts)
            if ((privided >= required) && resultedItem.getMaxStackSize() != 1) { //pass when enough and stackable
                if (inputCount == 1 && resultedItem.getType() == Material.SUGAR && resultedCount != 1) {
                    return;
                }
                event.getInventory().setResult(boxedItem);
            }
        }
    }

    @EventHandler
    public void handleCraftingResult(InventoryClickEvent event) {
        int getInputCount = 0;
        int index = 0;
        ItemStack[] nbtname = new ItemStack[9];
        int[] nbtcounts = new int[9];
        ItemStack[] nbtContainsType = new ItemStack[9];
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.WORKBENCH) { //Only crafting table
            CraftingInventory inv = (CraftingInventory) event.getInventory();
            if (((CraftingInventory) event.getInventory()).getResult() == null) {return;} //return if no result
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                //trigger when retrive result
                for (ItemStack item : inv.getMatrix()) { //loop crafting table
                    if (item != null && item.getType() == Material.SHULKER_BOX) { //only shulker box
                        if (item.getItemMeta() instanceof BlockStateMeta) {
                            BlockStateMeta ItemMeta = (BlockStateMeta) item.getItemMeta();
                            if (ItemMeta.getBlockState() instanceof ShulkerBox) {
                                ShulkerBox shulker = (ShulkerBox) ItemMeta.getBlockState();

                                //Record items in each shulker
                                ItemStack FirstItem = shulker.getInventory().getContents()[0];
                                if (!shulker.getInventory().isEmpty()) {
                                    nbtContainsType[index] = new ItemStack(FirstItem);
                                    nbtname[index] = FirstItem;
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
        BukkitRunnable task = new BukkitRunnable() { //Scheduled delay
            @Override
            public void run() {
                if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.WORKBENCH) { //when click in crafting table
                    CraftingInventory inv = (CraftingInventory) event.getInventory();
                    if (event.getSlotType() == InventoryType.SlotType.RESULT) { //Is result
                        ItemStack resultedItem = new recipe().getrecipe(nbtContainsType);
                        if (resultedItem != null) {
                            Object[] filteredobj = Arrays.stream(nbtname).filter(n -> n != null && n.getAmount() != 0).toArray();
                            ItemStack[] filterednbt = Arrays.copyOf(filteredobj, filteredobj.length, ItemStack[].class);
                            Arrays.sort(filterednbt, Comparator.comparing(ItemStack::getAmount));
                            for (int i = 1; i <= finalGetInputCount -1; i++) { //0 for result
                                if (resultedItem.getAmount() == 1) {
                                    if (resultedItem.getType() == Material.HONEY_BLOCK) {
                                        if (i == 1) {
                                            ItemStack boxedItem = new shulk().createContent(new ItemStack(Material.GLASS_BOTTLE), Material.GLASS_BOTTLE.getMaxStackSize());

                                            inv.setItem(i, boxedItem);
                                        } else {
                                            inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                        }
                                    } else {
                                        Object[] filteredcon = Arrays.stream(nbtContainsType).filter(n -> n != null).toArray();
                                        ItemStack[] filteredcontype = Arrays.copyOf(filteredcon, filteredobj.length, ItemStack[].class);
                                        if (Arrays.stream(filteredcontype).allMatch(cnt -> cnt.getMaxStackSize() == 64) && 64 > resultedItem.getMaxStackSize()) {
                                            int exceedbox = 0;
                                            ItemStack boxedItem;
                                            for (ItemStack itemStack : nbtContainsType) {
                                                if (itemStack != null && (itemStack.getMaxStackSize() > resultedItem.getMaxStackSize())) {
                                                    exceedbox = itemStack.getMaxStackSize() / resultedItem.getMaxStackSize() - 1;
                                                    break;
                                                }
                                            }
                                            if (exceedbox > 0 && i <= exceedbox) {
                                                boxedItem = new shulk().createContent(resultedItem, resultedItem.getMaxStackSize());

                                                inv.setItem(i, boxedItem);
                                            } else if (finalGetInputCount > 1) {
                                                inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                            }
                                        } else {
                                            int countExceedBox = (int) Arrays.stream(filterednbt).filter(num -> num.getMaxStackSize() > filterednbt[0].getMaxStackSize()).count();
                                            ItemStack boxedItem;
                                            if (i <= countExceedBox) {
                                                if (i == 1) {
                                                    boxedItem = new shulk().createContent(new ItemStack(filterednbt[countExceedBox].getType()), filterednbt[countExceedBox].getMaxStackSize() - filterednbt[0].getMaxStackSize());
                                                } else {
                                                    boxedItem = new shulk().createContent(new ItemStack(filterednbt[countExceedBox].getType()), filterednbt[countExceedBox].getMaxStackSize());
                                                }
                                                inv.setItem(i, boxedItem);
                                            } else {
                                                inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                            }
                                        }
                                    }
                                    /*else if (resultedItem.getType() == Material.ENDER_EYE) {
                                        if (i == 1) {
                                            ItemStack boxedItem = new shulk().createContent(new ItemStack(Material.BLAZE_POWDER), 48);

                                            inv.setItem(i, boxedItem);
                                        } else {
                                            inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                        }
                                    } */

                                } else {
                                    if (resultedItem.getType() == Material.SUGAR) {
                                        if (i == 1) {
                                            ItemStack boxedItem = new shulk().createContent(new ItemStack(Material.GLASS_BOTTLE), 16);

                                            inv.setItem(i, boxedItem);
                                        } else {
                                            inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                        }
                                    }
                                    if (i < resultedItem.getAmount()) {
                                        int[] filteredArray = Arrays.stream(nbtcounts).filter(num -> num != 0).toArray();
                                        Arrays.sort(filteredArray);
                                        /*
                                        if (resultedItem.getAmount() * filterednbt[0].getAmount() > resultedItem.getMaxStackSize()) {
                                            ItemStack boxedItem;
                                            boxedItem = new shulk().createContent(resultedItem, resultedItem.getMaxStackSize());
                                            inv.setItem(i, boxedItem);
                                        }
                                        */
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