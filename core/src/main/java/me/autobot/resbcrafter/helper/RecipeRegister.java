package me.autobot.resbcrafter.helper;

import me.autobot.resbcrafter.ReSbCrafter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class RecipeRegister {
    public static final Set<NamespacedKey> recipeKeys = new HashSet<>();
    public static void registerRecipe(ItemStack resultBox, ItemStack[] matrix) {
        NamespacedKey key = new NamespacedKey(ReSbCrafter.plugin, Long.toString(System.nanoTime()));
        recipeKeys.add(key);

        ShapedRecipe recipe = new ShapedRecipe(key, resultBox);
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                string.append(" ");
                continue;
            }
            string.append(i);
        }
        recipe.shape(string.toString().split("(?<=\\G.{3})")); //Split to 3 by 3
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                continue;
            }
            recipe.setIngredient(Integer.toString(i).charAt(0), new RecipeChoice.ExactChoice(matrix[i]));
        }
        Bukkit.addRecipe(recipe);
        Runnable runnable = () -> Bukkit.removeRecipe(key);
        ReSbCrafter.scheduler.globalTask(runnable);
    }
}
