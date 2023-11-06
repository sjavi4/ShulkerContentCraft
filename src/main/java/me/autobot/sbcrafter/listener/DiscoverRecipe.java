package me.autobot.sbcrafter.listener;

import me.autobot.sbcrafter.utility.RecipeHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;

public class DiscoverRecipe implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDiscoverRecipe(PlayerRecipeDiscoverEvent event) {
        var recipeKeys = RecipeHandler.recipeKeys;
        if (recipeKeys.contains(event.getRecipe())) {
            recipeKeys.remove(event.getRecipe());
            event.setCancelled(true);
        }
    }
}
