package me.autobot.resbcrafter.scheduler;

import me.autobot.resbcrafter.IScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class FoliaTasks implements IScheduler {
    private final Plugin plugin;
    public FoliaTasks(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public void globalTask(Runnable r) {
        Bukkit.getServer().getGlobalRegionScheduler().execute(plugin, r);
    }

    @Override
    public void regionTask(Runnable r, Location l) {
        Bukkit.getServer().getRegionScheduler().run(plugin, l, (c)->r.run());
    }

    @Override
    public void entityTask(Runnable r, Entity e) {
        e.getScheduler().run(plugin, (c)->r.run(), null);
    }

    @Override
    public void globalTaskDelayed(Runnable r, long d) {
        d = checkDelay(d);
        Bukkit.getServer().getGlobalRegionScheduler().runDelayed(plugin, (c)->r.run(), d);
    }

    @Override
    public void regionTaskDelayed(Runnable r, Location l, long d) {
        d = checkDelay(d);
        Bukkit.getServer().getRegionScheduler().runDelayed(plugin, l, (c)->r.run(), d);
    }

    @Override
    public void entityTaskDelayed(Runnable r, Entity e, long d) {
        d = checkDelay(d);
        e.getScheduler().runDelayed(plugin, (c)->r.run(), null, d);
    }

    private long checkDelay(long d) {
        return d <= 0 ? 1 : d;
    }
}
