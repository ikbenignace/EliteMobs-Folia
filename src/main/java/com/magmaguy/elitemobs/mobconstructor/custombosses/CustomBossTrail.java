package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

public class CustomBossTrail {

    private final CustomBossEntity customBossEntity;
    private final ArrayList<BukkitTask> bukkitTasks = new ArrayList<>();
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
        bukkitTasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                //In case of boss death or chunk unload, stop the effect
                if (!livingEntity.isValid()) {
                    cancel();
                    return;
                }
                //All conditions cleared, do the boss flair effect
                Location entityCenter = livingEntity.getLocation().clone().add(0, livingEntity.getHeight() / 2, 0);
                livingEntity.getWorld().spawnParticle(particle, entityCenter, 1, 0.1, 0.1, 0.1, 0.05);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1));
    }

    private void doItemTrail(Material material) {
        bukkitTasks.add(new BukkitRunnable() {

            @Override
            public void run() {
                //In case of boss death, stop the effect
                if (!livingEntity.isValid()) {
                    cancel();
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
                SchedulerUtil.runTaskLater(() -> {item.remove();
                        EntityTracker.unregister(item, RemovalReason.EFFECT_TIMEOUT);}, 20);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5));
    }

    public void terminateTrails() {
        for (BukkitTask bukkitTask : bukkitTasks) bukkitTask.cancel();
        bukkitTasks.clear();
    }

    public void restartTrails() {
        this.livingEntity = customBossEntity.getLivingEntity();
        startBossTrails();
    }

}
