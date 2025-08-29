package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.SchedulerUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;

public class DialogArmorStand {

    public static TextDisplay createDialogArmorStand(Entity sourceEntity, String dialog, Vector offset) {

        offset.add(getDisplacementVector(sourceEntity).subtract(new Vector(0, 1, 0)));
        Vector finalOffset = offset;
        TextDisplay armorStand = VisualDisplay.generateTemporaryTextDisplay(sourceEntity.getLocation().clone().add(finalOffset), dialog);

        //This part is necessary because armorstands are visible on their first tick to players
        final int[] taskTimer = {0}; // Counter that persists across executions
        final Object[] taskRef = new Object[1]; // Reference to store the task for cancellation
        
        taskRef[0] = SchedulerUtil.runTaskTimer(() -> {
            taskTimer[0]++;

            if (taskTimer[0] > 15 || !sourceEntity.isValid()) {
                EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                SchedulerUtil.cancelTask(taskRef[0]);
                return;
            }
            armorStand.teleport(sourceEntity.getLocation().clone().add(finalOffset).add(new Vector(0, taskTimer[0] * 0.05, 0)));
        }, 0, 2);

        return armorStand;
    }

    private static Vector getDisplacementVector(Entity sourceEntity) {
        double height = 2.3;
        if (sourceEntity instanceof LivingEntity)
            height = ((LivingEntity) sourceEntity).getEyeHeight();
        return new Vector(0, height, 0);
    }

    public static TextDisplay createDialogArmorStand(LivingEntity sourceEntity, String dialog) {

        if (sourceEntity == null) return null;

        TextDisplay armorStand = VisualDisplay.generateTemporaryTextDisplay(sourceEntity.getLocation().clone().add(getDisplacementVector(sourceEntity)), dialog);
        //This part is necessary because armorstands are visible on their first tick to players
        final int[] taskTimer2 = {0}; // Counter that persists across executions
        final Object[] taskRef2 = new Object[1]; // Reference to store the task for cancellation
        
        taskRef2[0] = SchedulerUtil.runTaskTimer(() -> {
            if (taskTimer2[0] > 15 || !sourceEntity.isValid()) {
                EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                SchedulerUtil.cancelTask(taskRef2[0]);
                return;
            }
            armorStand.teleport(sourceEntity.getLocation().clone().add(getDisplacementVector(sourceEntity)));
            taskTimer2[0]++;
        }, 0, 1);
        return armorStand;
    }

}
