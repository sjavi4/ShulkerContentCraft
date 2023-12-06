package me.autobot.sbcrafter.listener;

import me.autobot.sbcrafter.SBCrafter;
import me.autobot.sbcrafter.utility.RecipeHandler;
import me.autobot.sbcrafter.utility.ShulkerBoxHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class OnCraft implements Listener {
    RecipeHandler recipeHandler;
    ItemStack resultBox;
    ItemStack resultContent;

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent e) {
        //Reduce repeated event calling
        if (e.isRepair()) return;

        recipeHandler = new RecipeHandler(e.getInventory());
        Recipe recipe = recipeHandler.getRecipe();
        if (recipe == null) return;

        ItemStack resultContent = recipe.getResult();

        List<Material> matrix = recipeHandler.getMatrixList();

        int[] inputBox = {recipeHandler.getInputBox()};

        // (64 / result_maxstack) * (result_amount * input_maxstack / 64) = required box
        List<Material> boxContent = recipeHandler.getMatrixContentsList();

        var input = boxContent.stream().filter(Objects::nonNull).findFirst();
        int extraRequire = 0;
        if (input.isPresent()) {
            if (input.get() == Material.HONEY_BOTTLE && resultContent.getType() == Material.SUGAR) extraRequire = 1;
            if ((64.0 / (float)resultContent.getMaxStackSize() * (float)resultContent.getAmount() * (float)input.get().getMaxStackSize() / 64.0)+(float)extraRequire > (float)inputBox[0]) return;
        }
        if (resultContent.getType() == Material.SHULKER_BOX || resultContent.getType() == Material.FIREWORK_ROCKET || resultContent.getType() == Material.FIREWORK_STAR) return;
        if (SBCrafter.disabledMaterialList.contains(resultContent.getType().name())) return;
        //Basic-filter for nbt items
        //Future nbt items might still be able to craft

        //End Check Matrix

        //Start Preparing Result


        boolean mixStackCrafting = recipeHandler.isMixStackCrafting();
        Map<String,Integer> indexMap = recipeHandler.getLeastMaxStack_FirstBoxIndex();
        if (mixStackCrafting) {
            resultContent.setAmount(indexMap.get("minMaxStack"));
        } else {
            var item = boxContent.get(indexMap.get("firstBox"));
            resultContent.setAmount(Math.min(item.getMaxStackSize() * resultContent.getAmount(), resultContent.getMaxStackSize()));
        }

        Material firstBox = mixStackCrafting ? matrix.get(indexMap.get("leastMaxStackBox")) : matrix.get(indexMap.get("firstBox"));

        ItemStack[] recipeResult = new ItemStack[27];
        Arrays.fill(recipeResult, resultContent);

        resultBox = ShulkerBoxHandler.newbox(firstBox,recipeResult);
        this.resultContent = resultContent;
        //End Prepare Result

        //Register recipe & refresh gui
        if (Bukkit.getCraftingRecipe(e.getInventory().getMatrix(),Bukkit.getWorlds().get(0)) == null) {
            recipeHandler.registerRecipe(resultBox);
            var m = recipeHandler.getMatrix().clone();
            e.getInventory().setMatrix(new ItemStack[9]);
            e.getInventory().setMatrix(m);
        }
    }
}
