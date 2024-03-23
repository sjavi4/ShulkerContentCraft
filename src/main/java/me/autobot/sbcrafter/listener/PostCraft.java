package me.autobot.sbcrafter.listener;

import me.autobot.sbcrafter.SBCrafter;
import me.autobot.sbcrafter.utility.RecipeHandler;
import me.autobot.sbcrafter.utility.ShulkerBoxHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class PostCraft implements Listener {
    @EventHandler
    public void onCraft(CraftItemEvent e) {

        RecipeHandler recipeHandler = new RecipeHandler(e.getInventory());
        Recipe recipe = recipeHandler.getRecipe();
        if (recipe == null) return;

        if (recipeHandler.getRecipe() == null) return;

        if (e.getCursor().getType() != Material.AIR && !e.isShiftClick()) {
            //e.setCancelled(true);
            return;
        };
        ItemStack resultContent = recipeHandler.getRecipe().getResult();

        List<Material> matrixContents = recipeHandler.getMatrixContentsList();
        List<Material> matrix = recipeHandler.getMatrixList();

        int[] inputBox = {recipeHandler.getInputBox()};

        int extraFill = matrixContents.stream().filter(Objects::nonNull).anyMatch(i -> i.getMaxStackSize() < 64) ? 0 :
                resultContent.getAmount() * 64/resultContent.getMaxStackSize() -1;


        //Find Least MaxStack Box or Find First Box

        List<ItemStack> newMatrix = new ArrayList<>(Collections.nCopies(9,null));

        boolean mixStackCrafting = recipeHandler.isMixStackCrafting();
        Map<String,Integer> indexMap = recipeHandler.getLeastMaxStack_FirstBoxIndex();
        if (mixStackCrafting) {
            int leastMaxStackBox = indexMap.get("leastMaxStackBox");
            matrix.set(leastMaxStackBox, null);
            matrixContents.set(leastMaxStackBox, null);
        } else {
            int firstBox = indexMap.get("firstBox");
            matrix.set(firstBox, null);
            matrixContents.set(firstBox, null);
        }
        inputBox[0]--;


        //Handler remainder of different stacks
        if (mixStackCrafting) {
            // sum of larger stack - sum of smaller stack > 64 x 27 x 5 - 16 x 27 = 8208
            // difference / larger stack = number of full box > 8208 / 1728 = 4.75 ~ 4 full + 1 non-full
            // difference % larger stack = remainder > 8208 % 1728 = 1296 = 64 * 20 + 16

            //It might go wrong when there are 2+ types of max stackable size in matrix (non-vanilla)
            //Please switch off this recipe
            int minMaxStack = indexMap.get("minMaxStack");

            // -1 for 1 in result

            for (var box : matrix) {
                var item = matrixContents.get(matrix.indexOf(box));
                var index = matrix.indexOf(box);
                if (item == null) continue;
                inputBox[0]--;
                if (item.getMaxStackSize() == minMaxStack) {
                    newMatrix.set(index, new ItemStack(item));
                    matrix.set(index,null);
                    matrixContents.set(index,null);
                    continue;
                }
                var content = recipeHandler.getMatrixContents()[index];
                newMatrix.set(index, ShulkerBoxHandler.newbox(box, content, 27 * content.getMaxStackSize() - minMaxStack * 27));
                matrix.set(index,null);
                matrixContents.set(index,null);
            }
        }

        ItemStack[] special = new ItemStack[27];
        switch (resultContent.getType()) {
            case HONEY_BLOCK -> {
                Arrays.fill(special, new ItemStack(Material.GLASS_BOTTLE,64));
            }
            case SUGAR -> {
                Arrays.fill(special, new ItemStack(Material.GLASS_BOTTLE, 16));
            }
        }
        if (Arrays.stream(special).noneMatch(Objects::isNull)) {
            var item = matrix.stream().filter(Objects::nonNull).findFirst();
            if (item.isPresent()) {
                inputBox[0]--;
                int index = matrix.indexOf(item.get());
                newMatrix.set(index, ShulkerBoxHandler.newbox(item.get(),special));
                matrix.set(index,null);
                matrixContents.set(index,null);
            }

        }

        ItemStack[] resultItem = new ItemStack[27];
        Arrays.fill(resultItem,new ItemStack(resultContent.getType(),resultContent.getMaxStackSize()));

        for (int k = 0; k < extraFill ; k++) {
            var box = matrix.stream().filter(Objects::nonNull).findFirst();
            if (box.isPresent()) {
                int index = matrix.indexOf(box.get());
                inputBox[0]--;
                newMatrix.set(index, ShulkerBoxHandler.newbox(matrix.get(index), resultItem));
                matrix.set(index,null);
                matrixContents.set(index,null);
            }

        }

        for (int k = 0; k < inputBox[0]; k++) {
            var box = matrix.stream().filter(Objects::nonNull).findFirst();
            if (box.isPresent()) {
                int index = matrix.indexOf(box.get());
                newMatrix.set(index, new ItemStack(box.get()));
                matrix.set(index, null);
                matrixContents.set(index, null);
            }
        }
        Runnable runnable = ()->{e.getInventory().setMatrix(newMatrix.toArray(new ItemStack[0]));};
        if (SBCrafter.isFolia) {
            try {
                Object regionScheduler = Bukkit.getServer().getClass().getMethod("getRegionScheduler").invoke(Bukkit.getServer());
                Method execute = regionScheduler.getClass().getMethod("execute", Plugin.class, Location.class, Runnable.class);
                execute.invoke(regionScheduler, SBCrafter.getPlugin(),e.getInventory().getLocation(),runnable);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException ignored) {
                throw new RuntimeException(ignored);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(SBCrafter.getPlugin(),runnable,0);
        }

    }
}
