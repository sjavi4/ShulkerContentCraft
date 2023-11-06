package me.autobot.sbcrafter.command;

import me.autobot.sbcrafter.SBCrafter;
import me.autobot.sbcrafter.manage.RecipeManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player player && player.isOp()) {
            if (strings != null && strings.length >= 2) {
                Material material = Material.getMaterial(strings[0]);
                if (material != null) {
                    boolean onoff = strings[1].equalsIgnoreCase("enable");
                    List<String> list = SBCrafter.disabledMaterialList;
                    if (onoff) {
                        list.remove(material.name());
                        player.sendMessage("Enabled recipe.");
                    } else {
                        list.add(material.name());
                        player.sendMessage("Disabled recipe.");
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        // /command recipe on|off
        if (commandSender instanceof Player player && player.isOp()) {
            var argument = Arrays.stream(strings).toList();
            switch (argument.size()) {
                case 1 -> {
                    if (argument.get(0).length() == 0) return RecipeManager.recipeList.stream().filter(i -> i.getResult().getMaxStackSize() > 1).map(r -> r.getResult().getType().name()).toList();
                    return RecipeManager.recipeList.stream().filter(i -> i.getResult().getMaxStackSize() > 1).map(r -> r.getResult().getType().name()).filter(name -> name.startsWith(argument.get(0))).toList();
                }
                case 2 -> {
                    return List.of("enable","disable");
                }
                default -> {
                    return Collections.singletonList(" ");
                }
            }
        } else {
            return null;
        }
    }
}
