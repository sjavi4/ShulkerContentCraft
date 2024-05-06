package me.autobot.resbcrafter.listeners;

import me.autobot.resbcrafter.constants.Items;
import me.autobot.resbcrafter.helper.RecipeHelper;
import me.autobot.resbcrafter.helper.RecipeRegister;
import me.autobot.resbcrafter.helper.ShulkerBoxHelper;
import me.autobot.resbcrafter.helper.SpecialRemaining;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;

public class PrepareCraft implements Listener {

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {

        // Not to interrupt repairing
        if (event.isRepair()) {
            return;
        }
        // Not to Process Bukkit.CraftItem()
        if (event.getInventory().getViewers().isEmpty()) {
            return;
        }

        CraftingInventory craftingInventory = event.getInventory();
        // Non vanilla matrix
        if (craftingInventory.getMatrix().length != 9) {
            return;
        }
        RecipeHelper recipeHelper = new RecipeHelper(craftingInventory);
        Recipe recipe = recipeHelper.getRecipe();
        //System.out.println("Recipe: " + (System.nanoTime() - start));
        if (recipe == null) {
            return;
        }

        ItemStack resultItem = recipeHelper.getCondensedResult();
        if (resultItem == null || resultItem.isSimilar(Items.AIR)) {
            return;
        }

        // Hardcode Disabled - shulker box
        if (resultItem.getType() == Material.SHULKER_BOX) {
            return;
        }
        // Handle config : Disabled Crafting Recipes
        /*
        if () {
            return;
        }
         */

        int firstBoxIndex = recipeHelper.firstBoxIndex;

        // Honey bottle currently
        SpecialRemaining specialRemaining = new SpecialRemaining(recipe, recipeHelper.condensedMatrix, resultItem);

        // Use resultItem as real result
        // Recipe.getResult() might inaccurate
        long boxInput = recipeHelper.getInputBoxCount();
        //long emptyCount = recipeHelper.getEmptyBoxCount();

        // ([64.0 /] result_maxstack) * (result_amount * input_maxstack [/ 64.0]) = required box
        ItemStack firstBoxContent = recipeHelper.condensedMatrix[firstBoxIndex];

        float grantTotal = ((float) (resultItem.getAmount() * firstBoxContent.getMaxStackSize()) / resultItem.getMaxStackSize()) + specialRemaining.extraInputCount;
        // Not enough Input to carry all contents
        //System.out.println(grantTotal);
        //System.out.println(boxInput);
        if (grantTotal > boxInput) {
            return;
        }
        //System.out.println("Valid Input: " + (System.nanoTime() - start));
        int leastStackSize = recipeHelper.leastMaxStackSize;
        boolean mixStackCrafting = recipeHelper.mixStackCrafting();
        ItemStack finalResult = resultItem.clone();

        if (mixStackCrafting) {
            finalResult.setAmount(leastStackSize);
            // Get the least maxStack as the result count
        } else {
            finalResult.setAmount(Math.min(firstBoxContent.getMaxStackSize() * finalResult.getAmount(), finalResult.getMaxStackSize()));
            // Get first box as Result
        }
        ItemStack[] finalResultStack = new ItemStack[27];
        Arrays.fill(finalResultStack, finalResult);
        ItemStack boxedFinalResult = ShulkerBoxHelper.newbox(Items.SHULKER_BOXES.get(recipeHelper.originalMatrixWithBoxColor[firstBoxIndex]), finalResultStack);

        //Register recipe & refresh gui
        if (Bukkit.getCraftingRecipe(recipeHelper.originalMatrix, recipeHelper.player.getWorld()) == null) {
            RecipeRegister.registerRecipe(boxedFinalResult, recipeHelper.originalMatrix);
            ItemStack last = recipeHelper.originalMatrix[8];
            craftingInventory.setItem(9,Items.AIR);
            craftingInventory.setItem(9,last);
        }
    }
}
