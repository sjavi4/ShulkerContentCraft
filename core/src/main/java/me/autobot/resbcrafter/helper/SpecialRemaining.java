package me.autobot.resbcrafter.helper;

import me.autobot.resbcrafter.constants.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;


public class SpecialRemaining {
    private ItemStack[] matrix;
    private final ItemStack result;
    public int extraInputCount = 0;
    public ItemStack returnItem = Items.AIR;
    public SpecialRemaining(Recipe recipe, ItemStack[] matrix, ItemStack actualResult) {
        this.result = recipe.getResult();
        if (!result.equals(actualResult)) {
            return;
        }
        this.matrix = matrix;
        process();
    }

    private void process() {
        boolean present = false;
        for (ItemStack matrixItem : this.matrix) {
            if (matrixItem == null || matrixItem.isSimilar(Items.AIR)) {
                continue;
            }
            if (matrixItem.getType() == Material.HONEY_BOTTLE) {
                present = true;
                break;
            }
            if (matrixItem.getType() == Material.MILK_BUCKET) {
                present = true;
                break;
            }
        }
        if (!present) {
            return;
        }
        switch (result.getType()) {
            case HONEY_BLOCK -> {
                returnItem = new ItemStack(Material.GLASS_BOTTLE);
                returnItem.setAmount(returnItem.getMaxStackSize());
                extraInputCount = 0;
            }
            case SUGAR -> {
                returnItem = new ItemStack(Material.GLASS_BOTTLE);
                returnItem.setAmount(16);
                extraInputCount = 1;
            }
            case CAKE -> {
                returnItem = new ItemStack(Material.BUCKET);
                returnItem.setAmount(3);
            }
            default -> {
                extraInputCount = 0;
                returnItem = Items.AIR;
            }
        }
    }
}
