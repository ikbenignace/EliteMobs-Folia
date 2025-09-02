package com.magmaguy.elitemobs.utils;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility class for FoliaLib scheduler operations.
 * Provides a unified interface for all scheduling operations across different server platforms.
 */
public class FoliaScheduler {
    private static PlatformScheduler scheduler;
    private static Plugin plugin;

    /**
     * Initialize the FoliaLib scheduler
     * @param plugin The plugin instance
     */
    public static void initialize(Plugin plugin) {
        FoliaScheduler.plugin = plugin;
        FoliaLib foliaLib = new FoliaLib(plugin);
        scheduler = foliaLib.getImpl();
    }

    /**
     * Get the platform scheduler instance
     * @return PlatformScheduler instance
     */
    public static PlatformScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Run a task asynchronously
     * @param runnable Task to run
     * @return CompletableFuture<Void>
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return scheduler.runAsync(task -> runnable.run());
    }

    /**
     * Run a task asynchronously after a delay
     * @param runnable Task to run
     * @param delay Delay in ticks
     * @return WrappedTask instance
     */
    public static WrappedTask runLaterAsync(Runnable runnable, long delay) {
        return scheduler.runLaterAsync(runnable, delay);
    }

    /**
     * Run a task asynchronously with a repeating timer
     * @param runnable Task to run
     * @param delay Initial delay in ticks
     * @param period Period between executions in ticks
     * @return WrappedTask instance
     */
    public static WrappedTask runTimerAsync(Runnable runnable, long delay, long period) {
        return scheduler.runTimerAsync(runnable, delay, period);
    }

    /**
     * Run a task on the next tick
     * @param runnable Task to run
     * @return CompletableFuture<Void>
     */
    public static CompletableFuture<Void> runNextTick(Runnable runnable) {
        return scheduler.runNextTick(task -> runnable.run());
    }

    /**
     * Run a task after a delay
     * @param runnable Task to run
     * @param delay Delay in ticks
     * @return WrappedTask instance
     */
    public static WrappedTask runLater(Runnable runnable, long delay) {
        return scheduler.runLater(runnable, delay);
    }

    /**
     * Run a repeating task
     * @param runnable Task to run
     * @param delay Initial delay in ticks
     * @param period Period between executions in ticks
     * @return WrappedTask instance
     */
    public static WrappedTask runTimer(Runnable runnable, long delay, long period) {
        return scheduler.runTimer(runnable, delay, period);
    }

    /**
     * Run a task at a specific location
     * @param location Location to run the task at
     * @param runnable Task to run
     * @return CompletableFuture<Void>
     */
    public static CompletableFuture<Void> runAtLocation(Location location, Runnable runnable) {
        return scheduler.runAtLocation(location, task -> runnable.run());
    }

    /**
     * Run a task at a specific location after a delay
     * @param location Location to run the task at
     * @param runnable Task to run
     * @param delay Delay in ticks
     * @return WrappedTask instance
     */
    public static WrappedTask runAtLocationLater(Location location, Runnable runnable, long delay) {
        return scheduler.runAtLocationLater(location, runnable, delay);
    }

    /**
     * Run a repeating task at a specific location
     * @param location Location to run the task at
     * @param runnable Task to run
     * @param delay Initial delay in ticks
     * @param period Period between executions in ticks
     * @return WrappedTask instance
     */
    public static WrappedTask runAtLocationTimer(Location location, Runnable runnable, long delay, long period) {
        return scheduler.runAtLocationTimer(location, runnable, delay, period);
    }

    /**
     * Run a task for a specific entity
     * @param entity Entity to run the task for
     * @param runnable Task to run
     * @return CompletableFuture (EntityTaskResult is auto-converted)
     */
    public static CompletableFuture<?> runAtEntity(Entity entity, Runnable runnable) {
        return scheduler.runAtEntity(entity, task -> runnable.run());
    }

    /**
     * Run a task for a specific entity after a delay
     * @param entity Entity to run the task for
     * @param runnable Task to run
     * @param delay Delay in ticks
     * @return WrappedTask instance
     */
    public static WrappedTask runAtEntityLater(Entity entity, Runnable runnable, long delay) {
        return scheduler.runAtEntityLater(entity, runnable, delay);
    }

    /**
     * Run a repeating task for a specific entity
     * @param entity Entity to run the task for
     * @param runnable Task to run
     * @param delay Initial delay in ticks
     * @param period Period between executions in ticks
     * @return WrappedTask instance
     */
    public static WrappedTask runAtEntityTimer(Entity entity, Runnable runnable, long delay, long period) {
        return scheduler.runAtEntityTimer(entity, runnable, delay, period);
    }

    /**
     * Cancel a task
     * @param task Task to cancel
     */
    public static void cancelTask(WrappedTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Cancel all tasks owned by this plugin
     */
    public static void cancelAllTasks() {
        scheduler.cancelAllTasks();
    }
}