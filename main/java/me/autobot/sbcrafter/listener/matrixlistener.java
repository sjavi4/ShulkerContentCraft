package me.autobot.sbcrafter.listener;

import me.autobot.sbcrafter.util;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.*;

public class matrixlistener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPrepare (PrepareItemCraftEvent event) {

        //Start Check Matrix
        if (event.isRepair()) {return;}


        Recipe recipe = util.recipe.getRecipe(event.getInventory());

        if (recipe == null) {return;}

        ItemStack result = recipe.getResult();
        List<ItemStack> matrix = util.recipe.getMatrix();
        int inputBox = util.recipe.getInputBox();


        // (64 / maxstack) * amount = required box
        if (result.getAmount() * (64 / result.getMaxStackSize()) > inputBox) {
            return;
        }
        if (result.getType() == Material.SHULKER_BOX || result.getType() == Material.FIREWORK_ROCKET || result.getType() == Material.FIREWORK_STAR) {
            return;
        }
        //End Check Matrix

        //Start Prepare Result
        ItemStack[] insideMatrix = util.recipe.getInsideMatrix();

        int[] pos = {0};
        boolean flag = Arrays.stream(insideMatrix).filter(Objects::nonNull).anyMatch(e -> e.getMaxStackSize() < 64) && Arrays.stream(insideMatrix).filter(Objects::nonNull).anyMatch(e -> e.getMaxStackSize() == 64);
        if (flag) {
            int min = Arrays.stream(insideMatrix).filter(Objects::nonNull).min(Comparator.comparing(ItemStack::getMaxStackSize)).get().getMaxStackSize();
            result.setAmount(min);
            Arrays.stream(insideMatrix).peek(e -> pos[0]++).filter(e -> e != null && e.getMaxStackSize() == min).findFirst();
            //first = matrix.get(pos[0] - 1);
        } else {
            result.setAmount(Math.min(result.getAmount() * Arrays.stream(insideMatrix).filter(Objects::nonNull).findFirst().get().getMaxStackSize() , result.getMaxStackSize()));
            //first = (matrix.stream().peek(e -> pos[0]++).filter(i -> i != null && ((BlockStateMeta)i.getItemMeta()).getBlockState() instanceof ShulkerBox).findFirst().get());
        }
        ItemStack first = flag? matrix.get(pos[0] - 1) : matrix.stream().peek(e -> pos[0]++).filter(i -> i != null && ((BlockStateMeta)i.getItemMeta()).getBlockState() instanceof ShulkerBox).findFirst().get();

        ItemStack[] recipeResult = new ItemStack[27];
        Arrays.fill(recipeResult, result);

        ItemStack resultBox = util.box.newbox(first.getType(),recipeResult);

        //End Prepare Result

        event.getInventory().setResult(resultBox);

    }
}
