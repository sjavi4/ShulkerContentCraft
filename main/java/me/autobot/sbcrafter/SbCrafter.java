package me.autobot.sbcrafter;

import me.autobot.sbcrafter.listener.matrixlistener;
import me.autobot.sbcrafter.listener.postcraft;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SbCrafter extends JavaPlugin {

    private static Plugin plugin;

    public static Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        getServer().getPluginManager().registerEvents(new matrixlistener(),this);
        getServer().getPluginManager().registerEvents(new postcraft(),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
