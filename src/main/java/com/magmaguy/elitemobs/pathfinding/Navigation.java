package com.magmaguy.elitemobs.pathfinding;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.easyminecraftgoals.events.WanderBackToPointStartEvent;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

public class Navigation implements Listener {

    public static void stopMoving(LivingEntity livingEntity){
        NMSManager.getAdapter().doNotMove(livingEntity);
    }

    private static final HashMap<CustomBossEntity, Object> currentlyNavigating = new HashMap();

    public static void addSoftLeashAI(RegionalBossEntity regionalBossEntity) {
        if (NMSManager.getAdapter() == null) return;
        if (regionalBossEntity.getUnsyncedLivingEntity() != null &&
                regionalBossEntity.getUnsyncedLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        if (regionalBossEntity.getLivingEntity() instanceof Creature)
            NMSManager.getAdapter().wanderBackToPoint(
                            regionalBossEntity.getLivingEntity(),
                            regionalBossEntity.getSpawnLocation(),
                            regionalBossEntity.getLeashRadius() / 2D,
                            20 * 5)
                    .setSpeed(1.2f)
                    .setStopReturnDistance(1)
                    .setGoalRefreshCooldownTicks(20 * 3)
                    .setHardObjective(false)
                    .setTeleportOnFail(true)
                    .setStartWithCooldown(true)
                    .register();
    }

    public static void addHardLeashAI(RegionalBossEntity regionalBossEntity) {
        if (NMSManager.getAdapter() == null) return;
        if (regionalBossEntity.getUnsyncedLivingEntity() != null &&
                regionalBossEntity.getUnsyncedLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        NMSManager.getAdapter().wanderBackToPoint(
                        regionalBossEntity.getLivingEntity(),
                        regionalBossEntity.getSpawnLocation(),
                        regionalBossEntity.getLeashRadius(),
                        20 * 5)
                .setSpeed(2f)
                .setStopReturnDistance(0)
                .setGoalRefreshCooldownTicks(20 * 3)
                .setHardObjective(true)
                .setTeleportOnFail(true)
                .setStartWithCooldown(true)
                .register();
    }

    public static void shutdown() {
        currentlyNavigating.values().forEach(SchedulerUtil::cancelTask);
        currentlyNavigating.clear();
    }

    public static void navigateTo(CustomBossEntity customBossEntity, Double speed, Location destination, boolean force, int duration) {
        if (duration == 0) duration = 20 * 5;
        if (customBossEntity.getLivingEntity() == null) return;
        if (destination == null || destination.getWorld() == null) return;
        if (speed == null)
            speed = AttributeManager.getAttributeBaseValue(customBossEntity.getLivingEntity(), "generic_movement_speed");
        Double finalSpeed = speed;
        if (currentlyNavigating.get(customBossEntity) != null) SchedulerUtil.cancelTask(currentlyNavigating.get(customBossEntity));
        int finalDuration = duration;
        final Object[] taskRef = new Object[1]; // Reference to store the task for cancellation
        final int[] counter = {0}; // Counter that persists across executions
        taskRef[0] = SchedulerUtil.runTaskTimer(() -> {
            if (counter[0] >= finalDuration ||
                    !customBossEntity.exists() ||
                    customBossEntity.getLivingEntity() != null && customBossEntity.getLivingEntity().getLocation().distanceSquared(destination) < Math.pow(1, 2)) {
                if (customBossEntity.exists() && counter[0] >= finalDuration && force) {
                    customBossEntity.getLivingEntity().teleport(destination);
                }
                SchedulerUtil.cancelTask(taskRef[0]);
                currentlyNavigating.remove(customBossEntity);
                return;
            }
            NMSManager.getAdapter().move(customBossEntity.getLivingEntity(), finalSpeed.floatValue(), destination);
            counter[0]++;
        }, 0, 1);
        currentlyNavigating.put(customBossEntity, taskRef[0]);
    }

    @EventHandler(ignoreCancelled = true)
    public void makeReturningBossesInvulnerable(WanderBackToPointStartEvent event) {
        if (!event.isHardObjective()) return;
        if (event.getLivingEntity() == null) return;
        if (event.getLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;
        event.getLivingEntity().setInvulnerable(true);
        AttributeManager.setAttribute(event.getLivingEntity(), "generic_follow_range", regionalBossEntity.getCustomBossesConfigFields().getLeashRadius() * 1.5);
    }

    @EventHandler(ignoreCancelled = true)
    public void makeReturnedBossesVulnerable(WanderBackToPointEndEvent event) {
        if (!event.isHardObjective()) return;
        if (event.getLivingEntity() == null) return;
        if (event.getLivingEntity().getType() == EntityType.ENDER_DRAGON) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;
        if (!(eliteEntity instanceof RegionalBossEntity regionalBossEntity)) return;
        event.getLivingEntity().setInvulnerable(false);

        if (regionalBossEntity.getCustomBossesConfigFields().getFollowDistance() != 0)
            AttributeManager.setAttribute(event.getLivingEntity(), "generic_follow_range", regionalBossEntity.getCustomBossesConfigFields().getFollowDistance());
        else
            AttributeManager.setAttribute(event.getLivingEntity(), "generic_follow_range", AttributeManager.getAttributeDefaultValue(regionalBossEntity.getLivingEntity(), "generic_follow_range"));
        regionalBossEntity.fullHeal();
    }
}
