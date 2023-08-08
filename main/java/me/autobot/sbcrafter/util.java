package me.autobot.sbcrafter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class util {

    public static class box {
        public static ItemStack newbox(Material material,ItemStack[] itemStack) {
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
    }

    public static class recipe {
        private static List<ItemStack> matrix;
        private static final ItemStack[] insideMatrix = new ItemStack[9];

        private static int inputBox = 0;

        public static ItemStack[] getInsideMatrix() {
            return insideMatrix;
        }
        public static List<ItemStack> getMatrix() {
            return matrix;
        }
        public static int getInputBox() {
            return inputBox;
        }

        public static Recipe getRecipe(CraftingInventory craftingInventory) {

            if (Arrays.stream(craftingInventory.getMatrix()).filter(Objects::nonNull).noneMatch(i -> i.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta)i.getItemMeta()).getBlockState() instanceof ShulkerBox)) {
                return null;
            }


            matrix = Arrays.stream(craftingInventory.getMatrix()).toList();


            for (int i = 0; i < matrix.size() ; i++) {
                if (matrix.get(i) == null) {
                    insideMatrix[i] = null;
                    continue;
                }
                BlockState blockState = ((BlockStateMeta)matrix.get(i).getItemMeta()).getBlockState();
                if (!(blockState instanceof ShulkerBox)) {
                    insideMatrix[i] = null;
                    continue;
                }
                ItemStack[] contents = ((ShulkerBox)blockState).getInventory().getContents();
                if (Arrays.stream(contents).allMatch(Objects::isNull)) {
                    insideMatrix[i] = null;
                }
                if (Arrays.stream(contents).distinct().count() != 1) {
                    return null;
                }
                if (Arrays.stream(contents).anyMatch(k -> k != null && k.getAmount() != k.getMaxStackSize())) {
                    return null;
                }
                insideMatrix[i] = contents[0];
            }

            Recipe recipe = Bukkit.getCraftingRecipe(insideMatrix, Bukkit.getWorlds().get(0));
            if (recipe == null) {return null;}

            ItemStack result = recipe.getResult();

            inputBox = (int) matrix.stream().filter(i -> i != null && ((BlockStateMeta)i.getItemMeta()).getBlockState() instanceof ShulkerBox).count();

            return Bukkit.getCraftingRecipe(insideMatrix, Bukkit.getWorlds().get(0));
        }
    }

}
