package me.autobot.sbcrafter.utility;

import me.autobot.sbcrafter.SBCrafter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class RecipeHandler {
    CraftingInventory inventory;
    ItemStack[] matrix;
    ItemStack[] matrixContents;
    int inputBox;

    public static final Set<NamespacedKey> recipeKeys = new HashSet<>();

    public ItemStack[] getMatrix() {
        return matrix;
    }

    public ItemStack[] getMatrixContents() {
        return matrixContents;
    }

    public List<Material> getMatrixList() {
        var a = Arrays.stream(matrix).map(i -> i == null ? null:i.getType()).toList();
        return new ArrayList<>(a);
    }
    public List<Material> getMatrixContentsList() {
        var a = Arrays.stream(matrixContents).map(i -> i == null ? null:i.getType()).toList();
        return new ArrayList<>(a);
    }
    public int getInputBox() {
        return inputBox;
    }
    public RecipeHandler(CraftingInventory inv) {
        this.inventory = inv;
        this.matrix = inv.getMatrix();
    }
    public Recipe getRecipe() {
        if (Arrays.stream(inventory.getMatrix())
                .filter(Objects::nonNull)
                .anyMatch(i -> !(i.getItemMeta() instanceof BlockStateMeta blockStateMeta && blockStateMeta.getBlockState() instanceof ShulkerBox))) {
            return null;
        }

        inputBox = 0;
        matrixContents = new ItemStack[9];
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                matrixContents[i] = null;
                continue;
            }
            if (matrix[i].getItemMeta() instanceof BlockStateMeta blockStateMeta && blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
                inputBox++;
                ItemStack[] contents = shulkerBox.getInventory().getContents();
                if (Arrays.stream(contents).allMatch(Objects::isNull)) {
                    matrixContents[i] = null;
                    continue;
                }
                if (Arrays.stream(contents).distinct().count() != 1) return null;
                if (Arrays.stream(contents).anyMatch(k -> k.getMaxStackSize() == 1)) return null;
                if (Arrays.stream(contents).anyMatch(k -> k.getAmount() != k.getMaxStackSize())) return null;
                matrixContents[i] = contents[0];
            }
        }
        return Bukkit.getCraftingRecipe(matrixContents, Bukkit.getWorlds().get(0));
    }

    public boolean isMixStackCrafting() {
        List<Material> boxContent = getMatrixContentsList();
        return boxContent.stream().filter(Objects::nonNull).anyMatch(i -> i.getMaxStackSize() != 64) &&
                boxContent.stream().filter(Objects::nonNull).anyMatch(i -> i.getMaxStackSize() == 64);
    }

    public void registerRecipe(ItemStack resultBox) {
        NamespacedKey key = new NamespacedKey(SBCrafter.getPlugin(), Long.toString(System.nanoTime()));
        recipeKeys.add(key);

        ShapedRecipe recipe = new ShapedRecipe(key, resultBox);
        StringBuilder string = new StringBuilder();
        var matrix = getMatrixList();
        for (int i = 0; i < matrix.size(); i++) {
            if (matrix.get(i) == null) {
                string.append(" ");
                continue;
            }
            string.append(i);
        }
        recipe.shape(string.toString().split("(?<=\\G.{3})")); //Split to 3 by 3
        for (int i = 0; i < matrix.size(); i++) {
            if (matrix.get(i) == null) {
                continue;
            }
            recipe.setIngredient(Integer.toString(i).charAt(0), matrix.get(i));
        }
        Bukkit.addRecipe(recipe);
        Runnable runnable = () -> Bukkit.removeRecipe(key);
        if (SBCrafter.isFolia) {
            try {
                Object globalRegionScheduler = Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
                Method execute = globalRegionScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class);
                execute.invoke(globalRegionScheduler, SBCrafter.getPlugin(),runnable);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException ignored) {
                throw new RuntimeException(ignored);
            }
        } else {
            Bukkit.getScheduler().runTask(SBCrafter.getPlugin(), runnable);
        }
    }

    public Map<String,Integer> getLeastMaxStack_FirstBoxIndex() {
        HashMap<String,Integer> map = new HashMap<>();
        var boxContent = getMatrixContentsList();
        if (isMixStackCrafting()) {
            int min = 64;
            // Find the first least maxStack box index
            for (var i : boxContent) {
                if (i == null) continue;
                if (i.getMaxStackSize() < min) {
                    min = i.getMaxStackSize();
                    map.put("leastMaxStackBox", boxContent.indexOf(i));
                }
            }
            map.put("minMaxStack",min);
        } else {
            var item = boxContent.stream().filter(Objects::nonNull).findFirst();
            item.ifPresent(itemStack -> map.put("firstBox", boxContent.indexOf(itemStack)));
        }
        return map;
    }
}
