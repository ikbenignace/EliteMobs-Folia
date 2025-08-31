package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

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

    /**
     * Unified task wrapper that handles both Folia ScheduledTask and Bukkit BukkitTask
     */
    public static class TaskWrapper {
        private Object task;
        private boolean cancelled = false;

        public TaskWrapper(Object task) {
            this.task = task;
        }

        public void cancel() {
            if (cancelled || task == null) return;
            cancelled = true;
            
            if (isFolia) {
                // For Folia, task is a ScheduledTask, use reflection to call cancel()
                try {
                    task.getClass().getMethod("cancel").invoke(task);
                } catch (Exception e) {
                    // If reflection fails, we can't cancel the task
                }
            } else {
                // For Bukkit/Spigot, task can be a BukkitTask or task ID (Integer)
                if (task instanceof BukkitTask) {
                    ((BukkitTask) task).cancel();
                } else if (task instanceof Integer) {
                    Bukkit.getScheduler().cancelTask((Integer) task);
                }
            }
        }

        public boolean isCancelled() {
            if (cancelled) return true;
            
            if (isFolia) {
                try {
                    return (Boolean) task.getClass().getMethod("isCancelled").invoke(task);
                } catch (Exception e) {
                    return cancelled;
                }
            } else {
                if (task instanceof BukkitTask) {
                    return ((BukkitTask) task).isCancelled();
                } else if (task instanceof Integer) {
                    // For task IDs, we can't easily check if they're cancelled, 
                    // so we rely on our internal state
                    return cancelled;
                }
            }
            return cancelled;
        }

        public Object getTask() {
            return task;
        }
    }

    /**
     * Functional interface for creating cancellable runnables with access to their own task wrapper
     */
    @FunctionalInterface
    public interface CancellableRunnable {
        void run(TaskWrapper taskWrapper);
    }

    /**
     * Runs a task repeatedly at fixed intervals.
     * Uses appropriate scheduler based on server type.
     */
    public static TaskWrapper runTaskTimer(Runnable task, long delay, long period) {
        if (isFolia) {
            Object foliaTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay, period);
            return new TaskWrapper(foliaTask);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, task, delay, period);
            return new TaskWrapper(bukkitTask);
        }
    }

    /**
     * Runs a task repeatedly at fixed intervals with access to task wrapper for self-cancellation.
     * Uses appropriate scheduler based on server type.
     */
    public static TaskWrapper runTaskTimer(CancellableRunnable task, long delay, long period) {
        if (isFolia) {
            Object foliaTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(MetadataHandler.PLUGIN, (scheduledTask) -> {
                TaskWrapper wrapper = new TaskWrapper(scheduledTask);
                task.run(wrapper);
            }, delay, period);
            return new TaskWrapper(foliaTask);
        } else {
            TaskWrapper wrapper = new TaskWrapper(null);
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
                task.run(wrapper);
            }, delay, period);
            wrapper.task = bukkitTask;
            return wrapper;
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
    public static TaskWrapper runTaskLater(Runnable task, long delay) {
        if (isFolia) {
            Object foliaTask = Bukkit.getGlobalRegionScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay);
            return new TaskWrapper(foliaTask);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, task, delay);
            return new TaskWrapper(bukkitTask);
        }
    }

    /**
     * Runs a task later on the main thread with access to task wrapper.
     * Uses appropriate scheduler based on server type.
     */
    public static TaskWrapper runTaskLater(CancellableRunnable task, long delay) {
        if (isFolia) {
            Object foliaTask = Bukkit.getGlobalRegionScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> {
                TaskWrapper wrapper = new TaskWrapper(scheduledTask);
                task.run(wrapper);
            }, delay);
            return new TaskWrapper(foliaTask);
        } else {
            TaskWrapper wrapper = new TaskWrapper(null);
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
                task.run(wrapper);
            }, delay);
            wrapper.task = bukkitTask;
            return wrapper;
        }
    }

    /**
     * Runs a task later on the region thread that owns the specified location.
     * Falls back to sync scheduler for Paper/Spigot.
     */
    public static TaskWrapper runTaskLater(Location location, Runnable task, long delay) {
        if (isFolia) {
            Object foliaTask = Bukkit.getRegionScheduler().runDelayed(MetadataHandler.PLUGIN, location, (scheduledTask) -> task.run(), delay);
            return new TaskWrapper(foliaTask);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, task, delay);
            return new TaskWrapper(bukkitTask);
        }
    }

    /**
     * Runs a task later asynchronously.
     * Uses AsyncScheduler for Folia, async scheduler for Paper/Spigot.
     */
    public static TaskWrapper runTaskLaterAsync(Runnable task, long delay) {
        if (isFolia) {
            // Convert ticks to milliseconds (1 tick = 50ms)
            Object foliaTask = Bukkit.getAsyncScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay * 50, TimeUnit.MILLISECONDS);
            return new TaskWrapper(foliaTask);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(MetadataHandler.PLUGIN, task, delay);
            return new TaskWrapper(bukkitTask);
        }
    }

    /**
     * Runs a repeating async task using the appropriate scheduler.
     */
    public static TaskWrapper runTaskTimerAsync(Runnable task, long delay, long period) {
        if (isFolia) {
            // Convert ticks to milliseconds (1 tick = 50ms)
            Object foliaTask = Bukkit.getAsyncScheduler().runAtFixedRate(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS);
            return new TaskWrapper(foliaTask);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN, task, delay, period);
            return new TaskWrapper(bukkitTask);
        }
    }

    /**
     * Runs a repeating async task with access to task wrapper for self-cancellation.
     * Uses AsyncScheduler for Folia, async scheduler for Paper/Spigot.
     */
    public static TaskWrapper runTaskTimerAsync(CancellableRunnable task, long delay, long period) {
        if (isFolia) {
            // Convert ticks to milliseconds (1 tick = 50ms)
            Object foliaTask = Bukkit.getAsyncScheduler().runAtFixedRate(MetadataHandler.PLUGIN, (scheduledTask) -> {
                TaskWrapper wrapper = new TaskWrapper(scheduledTask);
                task.run(wrapper);
            }, delay * 50, period * 50, TimeUnit.MILLISECONDS);
            return new TaskWrapper(foliaTask);
        } else {
            TaskWrapper wrapper = new TaskWrapper(null);
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN, () -> {
                task.run(wrapper);
            }, delay, period);
            wrapper.task = bukkitTask;
            return wrapper;
        }
    }

    /**
     * Runs a task repeatedly at fixed intervals for a limited number of executions.
     * Uses appropriate scheduler based on server type.
     */
    public static TaskWrapper runTaskTimerLimited(Location location, Runnable task, long delay, long period, int maxExecutions) {
        if (isFolia) {
            final int[] executionCount = {0};
            Object foliaTask = Bukkit.getRegionScheduler().runAtFixedRate(MetadataHandler.PLUGIN, location, (scheduledTask) -> {
                executionCount[0]++;
                if (executionCount[0] > maxExecutions) {
                    scheduledTask.cancel();
                    return;
                }
                task.run();
            }, delay, period);
            return new TaskWrapper(foliaTask);
        } else {
            final int[] executionCount = {0};
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
                executionCount[0]++;
                if (executionCount[0] > maxExecutions) {
                    return; // BukkitTask will be cancelled externally
                }
                task.run();
            }, delay, period);
            return new TaskWrapper(bukkitTask);
        }
    }

    /**
     * Schedules an async delayed task using the appropriate scheduler.
     */
    public static TaskWrapper scheduleAsyncDelayedTask(Runnable task, long delay) {
        if (isFolia) {
            // Convert ticks to milliseconds (1 tick = 50ms)
            Object foliaTask = Bukkit.getAsyncScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay * 50, TimeUnit.MILLISECONDS);
            return new TaskWrapper(foliaTask);
        } else {
            int taskId = Bukkit.getScheduler().scheduleAsyncDelayedTask(MetadataHandler.PLUGIN, task, delay);
            return new TaskWrapper(taskId);
        }
    }

    /**
     * Schedules a sync delayed task using the appropriate scheduler.
     * Uses appropriate scheduler based on server type.
     */
    public static TaskWrapper scheduleSyncDelayedTask(Runnable task, long delay) {
        if (isFolia) {
            Object foliaTask = Bukkit.getGlobalRegionScheduler().runDelayed(MetadataHandler.PLUGIN, (scheduledTask) -> task.run(), delay);
            return new TaskWrapper(foliaTask);
        } else {
            int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, task, delay);
            return new TaskWrapper(taskId);
        }
    }

    /**
     * Cancels a specific task.
     * Handles both Folia ScheduledTask and Bukkit BukkitTask objects.
     */
    public static void cancelTask(Object task) {
        if (task == null) return;
        
        if (task instanceof TaskWrapper) {
            ((TaskWrapper) task).cancel();
            return;
        }
        
        if (isFolia) {
            // For Folia, task is a ScheduledTask, use reflection to call cancel()
            try {
                task.getClass().getMethod("cancel").invoke(task);
            } catch (Exception e) {
                // If reflection fails, we can't cancel the task
                // This shouldn't happen in normal circumstances
            }
        } else {
            // For Bukkit/Spigot, task is a BukkitTask
            if (task instanceof BukkitTask) {
                ((BukkitTask) task).cancel();
            }
        }
    }

    /**
     * Gets the underlying task ID if available (for backward compatibility)
     */
    public static Integer getTaskId(TaskWrapper taskWrapper) {
        if (taskWrapper == null || taskWrapper.task == null) return null;
        
        if (taskWrapper.task instanceof Integer) {
            return (Integer) taskWrapper.task;
        } else if (taskWrapper.task instanceof BukkitTask) {
            return ((BukkitTask) taskWrapper.task).getTaskId();
        }
        
        // For Folia tasks, we can't easily get an integer ID
        return null;
    }
    public static void cancelAllTasks() {
        // For plugin shutdown, we need to cancel all tasks regardless of Folia/Paper
        // This is appropriate since we're shutting down the entire plugin
        Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
    }
}