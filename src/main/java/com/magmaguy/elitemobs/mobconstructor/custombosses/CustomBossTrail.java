package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import com.magmaguy.elitemobs.utils.FoliaScheduler;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomBossTrail {

    private final CustomBossEntity customBossEntity;
    private final ArrayList<WrappedTask> wrappedTasks = new ArrayList<>();
    private LivingEntity livingEntity;

    public CustomBossTrail(CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        this.livingEntity = customBossEntity.getLivingEntity();
        startBossTrails();
    }

    private void startBossTrails() {
        if (customBossEntity.customBossesConfigFields.getTrails() == null) return;
        for (String string : customBossEntity.customBossesConfigFields.getTrails()) {
            try {
                Particle particle = Particle.valueOf(string);
                if (particle.equals(Particle.BLOCK)) return;
                doParticleTrail(particle);
            } catch (Exception ex) {
            }
            try {
                if (string.equals("LAVA")) return;
                Material material = Material.valueOf(string);
                doItemTrail(material);
            } catch (Exception ex) {
            }
        }
    }

    private void doParticleTrail(Particle particle) {
        if (particle.equals(Particle.BLOCK_MARKER) ||
                particle.equals(Particle.ENTITY_EFFECT) ||
                particle.equals(Particle.DUST_PILLAR) ||
                particle.equals(Particle.FALLING_DUST) ||
                particle.equals(Particle.BLOCK) ||
                particle.equals(Particle.ITEM) ||
                particle.equals(Particle.DUST))
            return;
        wrappedTasks.add(FoliaScheduler.runAtEntityTimer(livingEntity, () -> {
            //In case of boss death or chunk unload, stop the effect
            if (!livingEntity.isValid()) {
                return;
            }
            //All conditions cleared, do the boss flair effect
            Location entityCenter = livingEntity.getLocation().clone().add(0, livingEntity.getHeight() / 2, 0);
            livingEntity.getWorld().spawnParticle(particle, entityCenter, 1, 0.1, 0.1, 0.1, 0.05);
        }, 0, 1));
    }

    private void doItemTrail(Material material) {
        wrappedTasks.add(FoliaScheduler.runAtEntityTimer(livingEntity, () -> {
            //In case of boss death, stop the effect
            if (!livingEntity.isValid()) {
                return;
            }
            //All conditions cleared, do the boss flair effect
            Location entityCenter = livingEntity.getLocation().clone().add(0, livingEntity.getHeight() / 2, 0);
            Item item = VisualItemInitializer.initializeItem(ItemStackGenerator.generateItemStack
                    (material, "visualItem", List.of(ThreadLocalRandom.current().nextDouble() + "")), entityCenter);
            item.setVelocity(new Vector(
                    ThreadLocalRandom.current().nextDouble() / 5 - 0.10,
                    ThreadLocalRandom.current().nextDouble() / 5 - 0.10,
                    ThreadLocalRandom.current().nextDouble() / 5 - 0.10));
            FoliaScheduler.runLater(() -> {
                item.remove();
                EntityTracker.unregister(item, RemovalReason.EFFECT_TIMEOUT);
            }, 20);

        }, 0, 5));
    }

    public void terminateTrails() {
        for (WrappedTask wrappedTask : wrappedTasks) wrappedTask.cancel();
        wrappedTasks.clear();
    }

    public void restartTrails() {
        this.livingEntity = customBossEntity.getLivingEntity();
        startBossTrails();
    }

}
