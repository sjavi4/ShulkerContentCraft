package me.autobot.shulkerboxnbtcrafter.handler.Craft;

import me.autobot.shulkerboxnbtcrafter.ShulkerBoxNBTCrafter;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.*;

public class InputHandler implements Listener {
    @EventHandler
    public void HandleShulkerBoxInput(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        if (inventory.isEmpty() || !inventory.contains(Material.SHULKER_BOX)) {
            return;
        }

        iterateCraftingTable.getCraftingTableMatrix(inventory);

        int inputCount = iterateCraftingTable.getInputCount();
        ItemStack anyItemInShulkerBox =  iterateCraftingTable.getAnyItemInShulkerBox();
        ItemStack[] nbtContainsType =  iterateCraftingTable.getNbtContainsType();
        List<ItemStack> nbtItems =  iterateCraftingTable.getNbtItems();

        ItemStack resultedItem = ShulkerBoxNBTCrafter.recipeGetResult(nbtContainsType);

        if (resultedItem == null) {return;}
        int resultItemCount = resultedItem.getAmount();

        nbtItems.sort(Comparator.comparingInt(ItemStack::getMaxStackSize));

        ItemStack boxedItem;
        if (anyItemInShulkerBox == null) {return;}

        if (nbtItems.get(0).getMaxStackSize() < resultedItem.getMaxStackSize()) {
            boxedItem = ShulkerBoxNBTCrafter.shulkerBoxFill(resultedItem, nbtItems.get(0).getMaxStackSize() * resultedItem.getAmount());
        } else {
            boxedItem = ShulkerBoxNBTCrafter.shulkerBoxFill(resultedItem, resultedItem.getMaxStackSize());
        }

        int privided = resultedItem.getMaxStackSize() * inputCount;
        int required = nbtItems.get(0).getMaxStackSize() * resultItemCount;
        if ((privided >= required) && resultedItem.getMaxStackSize() != 1) {
            if (inputCount == 1 && resultedItem.getType() == Material.SUGAR && resultItemCount != 1) {
                return;
            }
            event.getInventory().setResult(boxedItem);
        }
    }


    public static class iterateCraftingTable {
        private static List<ItemStack> nbtItems;
        private static ItemStack[] nbtContainsType;
        private static int inputCount;
        private static ItemStack anyItemInShulkerBox;
        public static void getCraftingTableMatrix(CraftingInventory inventory) {
            inputCount = 0;
            int loopIndex = 0;
            nbtItems = new ArrayList<>();
            nbtContainsType = new ItemStack[9];
            for (ItemStack craftingTableItem : inventory.getMatrix()) {

                if (craftingTableItem != null && craftingTableItem.getType() != Material.SHULKER_BOX) {return;}

                if (craftingTableItem == null) {
                    loopIndex++;
                    continue;
                }

                inputCount++;

                BlockStateMeta craftingTableShulkerBoxMeta = (BlockStateMeta) craftingTableItem.getItemMeta();
                ShulkerBox craftingTableShulkerBox = (ShulkerBox) craftingTableShulkerBoxMeta.getBlockState();

                if (craftingTableShulkerBox.getInventory().isEmpty()) {loopIndex++;continue;}

                ItemStack[] itemsInShulkerBox = craftingTableShulkerBox.getInventory().getContents();
                anyItemInShulkerBox = craftingTableShulkerBox.getInventory().getContents()[0];

                ItemStack finalAnyItemInShulkerBox = anyItemInShulkerBox;
                if (Arrays.stream(itemsInShulkerBox).anyMatch(i -> i == null || i.getType() != finalAnyItemInShulkerBox.getType() || i.getAmount() != i.getMaxStackSize())) {return;}

                nbtContainsType[loopIndex] = new ItemStack(anyItemInShulkerBox);
                nbtItems.add(anyItemInShulkerBox);

                loopIndex++;
            }
        }
        public static int getInputCount() {
            return inputCount;
        }
        public static ItemStack[] getNbtContainsType() {
            return nbtContainsType;
        }
        public static List<ItemStack> getNbtItems() {
            return nbtItems;
        }
        public static ItemStack getAnyItemInShulkerBox() {
            return anyItemInShulkerBox;
        }
    }

}
