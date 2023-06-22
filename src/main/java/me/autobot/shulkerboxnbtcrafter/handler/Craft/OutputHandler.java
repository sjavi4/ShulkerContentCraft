package me.autobot.shulkerboxnbtcrafter.handler.Craft;

import me.autobot.shulkerboxnbtcrafter.ShulkerBoxNBTCrafter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class OutputHandler implements Listener {

    @EventHandler
    public void HandleCraftingResult(InventoryClickEvent event) {

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.WORKBENCH) {return;}

        CraftingInventory craftingInv = (CraftingInventory) event.getInventory();
        if (craftingInv.getResult() == null) {return;}
        if (event.getSlotType() != InventoryType.SlotType.RESULT) {return;}

        InputHandler.iterateCraftingTable.getCraftingTableMatrix(craftingInv);

        List<ItemStack> nbtname = InputHandler.iterateCraftingTable.getNbtItems();
        nbtname.sort(Comparator.comparingInt(ItemStack::getMaxStackSize));

        int finalGetInputCount = InputHandler.iterateCraftingTable.getInputCount();
        ItemStack resultedItem = ShulkerBoxNBTCrafter.recipeGetResult(InputHandler.iterateCraftingTable.getNbtContainsType());

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.WORKBENCH) {
                    CraftingInventory inv = (CraftingInventory) event.getInventory();
                    if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                        if (resultedItem != null) {
                            for (int i = 1; i <= finalGetInputCount -1; i++) {
                                if (resultedItem.getAmount() == 1) {
                                    if (resultedItem.getType() == Material.HONEY_BLOCK) {
                                        if (i == 1) {
                                            ItemStack boxedItem = ShulkerBoxNBTCrafter.shulkerBoxFill(new ItemStack(Material.GLASS_BOTTLE), Material.GLASS_BOTTLE.getMaxStackSize());
                                            inv.setItem(i, boxedItem);
                                        } else {
                                            inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                        }
                                    } else {
                                        if (nbtname.stream().allMatch(count -> count.getMaxStackSize() == 64) && 64 > resultedItem.getMaxStackSize()) {
                                            int exceedbox = 0;
                                            ItemStack boxedItem;
                                            for (ItemStack itemStack : nbtname) {
                                                if (itemStack != null && (itemStack.getMaxStackSize() > resultedItem.getMaxStackSize())) {
                                                    exceedbox = itemStack.getMaxStackSize() / resultedItem.getMaxStackSize() - 1;
                                                    break;
                                                }
                                            }
                                            if (exceedbox > 0 && i <= exceedbox) {
                                                boxedItem = ShulkerBoxNBTCrafter.shulkerBoxFill(resultedItem, resultedItem.getMaxStackSize());

                                                inv.setItem(i, boxedItem);
                                            } else if (finalGetInputCount > 1) {
                                                inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                            }
                                        } else {
                                            int countExceedBox = (int) nbtname.stream().filter(num -> num.getMaxStackSize() > nbtname.get(0).getMaxStackSize()).count();

                                            ItemStack boxedItem;
                                            if (i <= countExceedBox) {
                                                if (i == 1) {
                                                    boxedItem = ShulkerBoxNBTCrafter.shulkerBoxFill(new ItemStack(nbtname.get(countExceedBox).getType()), nbtname.get(countExceedBox).getMaxStackSize() - nbtname.get(0).getMaxStackSize());
                                                } else {
                                                    boxedItem = ShulkerBoxNBTCrafter.shulkerBoxFill(new ItemStack(nbtname.get(countExceedBox).getType()), nbtname.get(countExceedBox).getMaxStackSize());
                                                }
                                                inv.setItem(i, boxedItem);
                                            } else {
                                                inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                            }
                                        }
                                    }
                                } else {
                                    if (resultedItem.getType() == Material.SUGAR) {
                                        if (i == 1) {
                                            ItemStack boxedItem = ShulkerBoxNBTCrafter.shulkerBoxFill(new ItemStack(Material.GLASS_BOTTLE), 16);

                                            inv.setItem(i, boxedItem);
                                        } else {
                                            inv.setItem(i, new ItemStack(Material.SHULKER_BOX, 1));
                                        }
                                    }
                                    if (i < resultedItem.getAmount()) {
                                        if (resultedItem.getAmount() * nbtname.get(0).getMaxStackSize() > resultedItem.getMaxStackSize()) {
                                            ItemStack boxedItem;
                                            boxedItem = ShulkerBoxNBTCrafter.shulkerBoxFill(resultedItem, resultedItem.getMaxStackSize());
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
        task.runTaskLater(ShulkerBoxNBTCrafter.getPlugin(),0);
    }
}
