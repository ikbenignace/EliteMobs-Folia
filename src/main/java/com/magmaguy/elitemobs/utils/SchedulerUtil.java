package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;

/**
 * Utility class for cross-server scheduler compatibility between Folia and Paper/Spigot
 */
public class SchedulerUtil {
    private static boolean isFolia = false;
    private static boolean foliaChecked = false;

    static {
        checkFolia();
    }

    private static void checkFolia() {
        if (foliaChecked) return;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }
        foliaChecked = true;
    }

    public static boolean isFolia() {
        return isFolia;
    }

    /**
     * Runs a task repeatedly at fixed intervals.
     * Uses appropriate scheduler based on server type.
     */
    public static Object runTaskTimer(Runnable task, long delay, long period) {
        if (isFolia) {
            return Bukkit.getGlobalRegionScheduler().runAtFixedRate(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay, period);
        } else {
            return Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, task, delay, period);
        }
    }

    /**
     * Runs a task on the region thread that owns the specified location.
     * Falls back to sync scheduler for Paper/Spigot.
     */
    public static void runTask(Location location, Runnable task) {
        if (isFolia) {
            Bukkit.getRegionScheduler().run(MetadataHandler.PLUGIN, location, (scheduledTask) -> task.run());
        } else {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, task);
        }
    }

    /**
     * Runs a task on the main thread.
     * Uses appropriate scheduler based on server type.
     */
    public static void runTask(Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(MetadataHandler.PLUGIN, (scheduledTask) -> task.run());
        } else {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, task);
        }
    }

    /**
     * Runs a task on the region thread that owns the specified entity.
     * Falls back to sync scheduler for Paper/Spigot.
     */
    public static void runTask(Entity entity, Runnable task) {
        if (isFolia) {
            entity.getScheduler().run(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, task);
        }
    }

    /**
     * Runs a task asynchronously.
     * Uses AsyncScheduler for Folia, async scheduler for Paper/Spigot.
     */
    public static void runTaskAsync(Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(MetadataHandler.PLUGIN, (scheduledTask) -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, task);
        }
    }

    /**
     * Runs a task later on the main thread.
     * Uses appropriate scheduler based on server type.
     */
    public static Object runTaskLater(Runnable task, long delay) {
        if (isFolia) {
            return Bukkit.getGlobalRegionScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay);
        } else {
            return Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, task, delay);
        }
    }

    /**
     * Runs a task later on the region thread that owns the specified location.
     * Falls back to sync scheduler for Paper/Spigot.
     */
    public static Object runTaskLater(Location location, Runnable task, long delay) {
        if (isFolia) {
            return Bukkit.getRegionScheduler().runDelayed(MetadataHandler.PLUGIN, location, (scheduledTask) -> task.run(), delay);
        } else {
            return Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, task, delay);
        }
    }

    /**
     * Runs a task later asynchronously.
     * Uses AsyncScheduler for Folia, async scheduler for Paper/Spigot.
     */
    public static Object runTaskLaterAsync(Runnable task, long delay) {
        if (isFolia) {
            return Bukkit.getAsyncScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay);
        } else {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(MetadataHandler.PLUGIN, task, delay);
        }
    }

    /**
     * Runs a task repeatedly at fixed intervals for a limited number of executions.
     * Uses appropriate scheduler based on server type.
     */
    public static Object runTaskTimerLimited(Location location, Runnable task, long delay, long period, int maxExecutions) {
        if (isFolia) {
            final int[] executionCount = {0};
            return Bukkit.getRegionScheduler().runAtFixedRate(MetadataHandler.PLUGIN, location, (scheduledTask) -> {
                executionCount[0]++;
                if (executionCount[0] > maxExecutions) {
                    scheduledTask.cancel();
                    return;
                }
                task.run();
            }, delay, period);
        } else {
            final int[] executionCount = {0};
            return Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
                executionCount[0]++;
                if (executionCount[0] > maxExecutions) {
                    return; // BukkitTask will be cancelled externally
                }
                task.run();
            }, delay, period);
        }
    }

    /**
     * Schedules an async delayed task using the appropriate scheduler.
     */
    public static Object scheduleAsyncDelayedTask(Runnable task, long delay) {
        if (isFolia) {
            return Bukkit.getAsyncScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay);
        } else {
            return Bukkit.getScheduler().scheduleAsyncDelayedTask(MetadataHandler.PLUGIN, task, delay);
        }
    }

    /**
     * Schedules a sync delayed task using the appropriate scheduler.
     * Uses appropriate scheduler based on server type.
     */
    public static Object scheduleSyncDelayedTask(Runnable task, long delay) {
        if (isFolia) {
            return Bukkit.getGlobalRegionScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay);
        } else {
            return Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, task, delay);
        }
    }

    /**
     * Runs a repeating async task using the appropriate scheduler.
     */
    public static Object runTaskTimerAsync(Runnable task, long delay, long period) {
        if (isFolia) {
            return Bukkit.getAsyncScheduler().runAtFixedRate(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay, period);
        } else {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN, task, delay, period);
        }
    }

    /**
     * Cancels all tasks for the plugin.
     * Used during plugin shutdown.
     */
    public static void cancelAllTasks() {
        // For plugin shutdown, we need to cancel all tasks regardless of Folia/Paper
        // This is appropriate since we're shutting down the entire plugin
        Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
    }
}