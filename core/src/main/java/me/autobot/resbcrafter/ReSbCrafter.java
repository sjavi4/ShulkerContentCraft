package me.autobot.resbcrafter;

import me.autobot.resbcrafter.listeners.CraftedItem;
import me.autobot.resbcrafter.listeners.DiscoverRecipe;
import me.autobot.resbcrafter.listeners.PrepareCraft;
import me.autobot.resbcrafter.scheduler.BukkitTasks;
import me.autobot.resbcrafter.scheduler.FoliaTasks;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReSbCrafter extends JavaPlugin {

    public static IScheduler scheduler;
    public static Plugin plugin;
    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic
        if (getClass("io.papermc.paper.threadedregions.RegionizedServer")) {
            scheduler = new FoliaTasks(this);
        } else {
            scheduler = new BukkitTasks(this);
        }
        PluginManager manager = Bukkit.getServer().getPluginManager();
        manager.registerEvents(new PrepareCraft(),this);
        manager.registerEvents(new CraftedItem(), this);
        manager.registerEvents(new DiscoverRecipe(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    boolean getClass(String c) {
        try {
            Class.forName(c);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
