package me.autobot.sbcrafter;

import me.autobot.sbcrafter.command.CommandHandler;
import me.autobot.sbcrafter.listener.OnCraft;
import me.autobot.sbcrafter.listener.PostCraft;
import me.autobot.sbcrafter.listener.DiscoverRecipe;
import me.autobot.sbcrafter.manage.RecipeManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


public final class SBCrafter extends JavaPlugin {
    public static final List<String> disabledMaterialList = new ArrayList<>();
    private static Plugin plugin;
    public static Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic
        var pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(new OnCraft(),this);
        pluginManager.registerEvents(new PostCraft(),this);
        pluginManager.registerEvents(new DiscoverRecipe(),this);
        var handler = new CommandHandler();
        getCommand("SBCrafter").setExecutor(handler);
        getCommand("SBCrafter").setTabCompleter(handler);
        RecipeManager.loadRecipe();

        //this.saveDefaultConfig();
        var config = this.getConfig();
        config.addDefault("Disable",new ArrayList<>());
        config.options().copyDefaults(true);
        this.saveConfig();
        disabledMaterialList.addAll(config.getStringList("Disable"));
    }

    @Override
    public void onDisable() {
        this.getConfig().set("Disable", disabledMaterialList);
        this.saveConfig();
    }
}
