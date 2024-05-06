package me.autobot.resbcrafter.listeners;

import me.autobot.resbcrafter.ReSbCrafter;
import me.autobot.resbcrafter.constants.Items;
import me.autobot.resbcrafter.helper.RecipeHelper;
import me.autobot.resbcrafter.helper.ShulkerBoxHelper;
import me.autobot.resbcrafter.helper.SpecialRemaining;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;

public class CraftedItem implements Listener {
    @EventHandler
    public void onCrafted(CraftItemEvent event) {
        CraftingInventory craftingInventory = event.getInventory();
        RecipeHelper recipeHelper = new RecipeHelper(craftingInventory);
        Recipe recipe = recipeHelper.getRecipe();
        if (recipe == null) {
            return;
        }
        // Prevent Player click while having item on cursor
        ItemStack cursorItem = event.getCursor();
        if (cursorItem != null && cursorItem.getType() != Material.AIR && !event.isShiftClick()) {
            return;
        }


        ItemStack[] modifiedMatrix = recipeHelper.originalMatrix;
        ItemStack[] condensedMatrix = recipeHelper.condensedMatrix;
        int[] boxColorMatrix = recipeHelper.originalMatrixWithBoxColor;
        int leastStackSize = recipeHelper.leastMaxStackSize;
        int firstBoxIndex = recipeHelper.firstBoxIndex;
        ItemStack resultContent = recipeHelper.getCondensedResult();
        int extraFilling = leastStackSize < 64 ? 0 : resultContent.getAmount() * 64 / resultContent.getMaxStackSize() -1;

        // Honey bottle currently
        SpecialRemaining specialRemaining = new SpecialRemaining(recipe, condensedMatrix, resultContent);

        // Box is obtained from result slot, delete the old one
        modifiedMatrix[firstBoxIndex] = Items.AIR;
        condensedMatrix[firstBoxIndex] = null;
        boxColorMatrix[firstBoxIndex] = -2;
        boolean mixStackCrafting = recipeHelper.mixStackCrafting();


        if (mixStackCrafting) {
            //Handler remainder of different stacks

            // sum of larger stack - sum of smaller stack > 64 x 27 x 5 - 16 x 27 = 8208
            // difference / larger stack = number of full box > 8208 / 1728 = 4.75 ~ 4 full + 1 non-full
            // difference % larger stack = remainder > 8208 % 1728 = 1296 = 64 * 20 + 16

            // It might go wrong when there are 2+ types of max stackable size in matrix (non-vanilla?)
            //
            //int minMaxStack = indexMap.get("minMaxStack");


            // already -1 for result
            for (int index = 0; index < condensedMatrix.length; index++) {
                // Non-shulker box
                // Skip this as it already processed
                if (index == firstBoxIndex) {
                    continue;
                }

                ItemStack condensedContent = condensedMatrix[index];

                if (condensedContent == null || condensedContent.equals(Items.AIR)) {
                    // Mixed box or Empty box
                    continue;
                }

                modifiedMatrix[index] = ShulkerBoxHelper.newbox(Items.SHULKER_BOXES.get(boxColorMatrix[index]),
                        condensedContent,
                        27 * condensedContent.getMaxStackSize() - leastStackSize * 27
                );
                condensedMatrix[index] = null;
                boxColorMatrix[index] = -2;
            }
        }
        // Special Returns
        if (specialRemaining.returnItem != Items.AIR) {
            // Find first empty box to store the special return
            for (int index = 0; index < condensedMatrix.length; index++) {
                ItemStack condensedItem = condensedMatrix[index];
                if (condensedItem == null) {
                    continue;
                }
                ItemStack[] returnItem = new ItemStack[27];
                Arrays.fill(returnItem, specialRemaining.returnItem);
                modifiedMatrix[index] = ShulkerBoxHelper.newbox(Items.SHULKER_BOXES.get(boxColorMatrix[index]), returnItem);
                condensedMatrix[index] = null;
                boxColorMatrix[index] = -2;
                break;
            }
        }

        ItemStack[] resultItem = new ItemStack[27];
        ItemStack clonedResult = resultContent.clone();
        clonedResult.setAmount(clonedResult.getMaxStackSize());
        Arrays.fill(resultItem, clonedResult);

        //for (int k = 0; k < extraFilling ; k++) {
        for (int j = 0; j < boxColorMatrix.length; j++) {
            if (boxColorMatrix[j] < 0) {
                continue;
            }
            if (condensedMatrix[j] == null) {
                continue;
            }
            Material colorBox = Items.SHULKER_BOXES.get(boxColorMatrix[j]);
            if (extraFilling <= 0) {
                modifiedMatrix[j] = new ItemStack(colorBox);
                continue;
            }
            modifiedMatrix[j] = ShulkerBoxHelper.newbox(colorBox, resultItem);
            condensedMatrix[j] = null;
            extraFilling--;
        }
        Runnable runnable = () -> craftingInventory.setMatrix(modifiedMatrix);
        ReSbCrafter.scheduler.regionTaskDelayed(runnable, craftingInventory.getLocation(), 0);
    }
}
