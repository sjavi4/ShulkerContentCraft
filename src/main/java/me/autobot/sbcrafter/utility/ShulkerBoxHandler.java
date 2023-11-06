package me.autobot.sbcrafter.utility;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerBoxHandler {
    public static ItemStack newbox(Material material, ItemStack[] itemStack) {
        ItemStack resultBox = new ItemStack(material);
        BlockStateMeta bsm = (BlockStateMeta) resultBox.getItemMeta();
        if (bsm == null) {return resultBox;}

        ShulkerBox box = (ShulkerBox) bsm.getBlockState();
        Inventory boxInv = box.getInventory();
        boxInv.setContents(itemStack);
        bsm.setBlockState(box);
        resultBox.setItemMeta(bsm);
        box.update();
        return resultBox;
    }

    public static ItemStack newbox(Material material, ItemStack itemStack, int counts) {
        ItemStack resultBox = new ItemStack(material);
        BlockStateMeta bsm = (BlockStateMeta) resultBox.getItemMeta();
        if (bsm == null) {return resultBox;}

        ShulkerBox box = (ShulkerBox) bsm.getBlockState();
        Inventory boxInv = box.getInventory();
        for (int i = 0; i < boxInv.getSize(); i++) {
            int maxStack = itemStack.getMaxStackSize();
            itemStack.setAmount(Math.min(counts, maxStack));
            boxInv.setItem(i, itemStack);
            counts -= maxStack;
        }
        bsm.setBlockState(box);
        resultBox.setItemMeta(bsm);
        box.update();
        return resultBox;
    }

}
