package me.autobot.resbcrafter.helper;

import me.autobot.resbcrafter.constants.Items;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerBoxHelper {
    //public static
    public static ShulkerBox convertShulkerBox(ItemStack shulkerBox) {
        if (!Items.SHULKER_BOXES.contains(shulkerBox.getType())) {
            return null;
        }
        return (ShulkerBox)((BlockStateMeta)shulkerBox.getItemMeta()).getBlockState();
    }

    public static ItemStack newbox(Material material, ItemStack[] itemStack) {
        ItemStack shulkerBox = new ItemStack(material);
        BlockStateMeta bsm = (BlockStateMeta) shulkerBox.getItemMeta();
        ShulkerBox box = convertShulkerBox(shulkerBox);
        Inventory boxInv = box.getInventory();
        boxInv.setContents(itemStack);
        bsm.setBlockState(box);
        shulkerBox.setItemMeta(bsm);
        box.update();
        return shulkerBox;
    }

    public static ItemStack newbox(Material material, ItemStack itemStack, int counts) {
        ItemStack shulkerBox = new ItemStack(material);
        BlockStateMeta bsm = (BlockStateMeta) shulkerBox.getItemMeta();
        ShulkerBox box = convertShulkerBox(shulkerBox);
        Inventory boxInv = box.getInventory();
        for (int i = 0; i < boxInv.getSize(); i++) {
            int maxStack = itemStack.getMaxStackSize();
            itemStack.setAmount(Math.min(counts, maxStack));
            boxInv.setItem(i, itemStack);
            counts -= maxStack;
        }
        bsm.setBlockState(box);
        shulkerBox.setItemMeta(bsm);
        box.update();
        return shulkerBox;
    }
}
