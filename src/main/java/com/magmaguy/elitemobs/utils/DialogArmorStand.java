package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.FoliaScheduler;
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
        FoliaScheduler.runAtEntityTimer(armorStand, new Runnable() {
            int taskTimer = 0;

            @Override
            public void run() {
                taskTimer++;

                if (taskTimer > 15 || !sourceEntity.isValid()) {
                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                    return;
                }
                armorStand.teleport(sourceEntity.getLocation().clone().add(finalOffset).add(new Vector(0, taskTimer * 0.05, 0)));
            }
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
        FoliaScheduler.runAtEntityTimer(armorStand, new Runnable() {
            int taskTimer = 0;

            @Override
            public void run() {
                if (taskTimer > 15 || !sourceEntity.isValid()) {
                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                    return;
                }
                armorStand.teleport(sourceEntity.getLocation().clone().add(getDisplacementVector(sourceEntity)));
                taskTimer++;
            }
        }, 0, 1);
        return armorStand;
    }

}
