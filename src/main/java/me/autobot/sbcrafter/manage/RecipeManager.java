package me.autobot.sbcrafter.manage;

import org.bukkit.Bukkit;
import org.bukkit.inventory.*;

import java.util.*;


public class RecipeManager {
    public static final List<Recipe> recipeList = new ArrayList<>();
    public static void loadRecipe() {
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            var nextIndex = it.next();
            if (nextIndex.getResult().getType().isAir() || nextIndex.getResult().getMaxStackSize() == 1) {
                continue;
            }
            if (nextIndex instanceof ShapedRecipe || nextIndex instanceof ShapelessRecipe) {
                recipeList.add(nextIndex);
            }

        }
        recipeList.sort(Comparator.comparing(r -> r.getResult().toString()));
    }
}
