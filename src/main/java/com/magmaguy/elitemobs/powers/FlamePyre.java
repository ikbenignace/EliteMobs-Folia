package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import com.magmaguy.elitemobs.utils.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.concurrent.ThreadLocalRandom;

public class FlamePyre extends BossPower implements Listener {

    public FlamePyre() {
        super(PowersConfig.getPower("flame_pyre.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {

        FlamePyre flamePyre = (FlamePyre) event.getEliteMobEntity().getPower(this);
        if (flamePyre == null) return;
        if (!eventIsValid(event, flamePyre)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        flamePyre.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doFlamePyrePhase1(event.getEliteMobEntity());

    }

    /**
     * Warning phase
     */
    private void doFlamePyrePhase1(EliteEntity eliteEntity) {
        eliteEntity.getLivingEntity().setAI(false);
        FoliaScheduler.runAtEntityTimer(eliteEntity.getLivingEntity(), new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                if (!eliteEntity.isValid()) {
                    return;
                }
                spawnPhase1Particle(eliteEntity.getLivingEntity().getLocation().clone(), Particle.SMOKE);
                if (counter < 20 * 2) return;
                doFlamePyrePhase2(eliteEntity);
            }
        }, 0, 1);
    }

    private void spawnPhase1Particle(Location location, Particle particle) {
        for (int i = 0; i < 10; i++) {
            location.getWorld().spawnParticle(particle, new Location(
                    location.getWorld(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.5 + location.getX(),
                    location.getY(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.5 + location.getZ()
            ), 0, 0, 1, 0, ThreadLocalRandom.current().nextDouble() * 2);
        }
    }

    /**
     * First damage phase
     */
    private void doFlamePyrePhase2(EliteEntity eliteEntity) {
        FoliaScheduler.runAtEntityTimer(eliteEntity.getLivingEntity(), new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!eliteEntity.isValid()) {
                    return;
                }
                counter++;
                spawnPhase1Particle(eliteEntity.getLivingEntity().getLocation().clone(), Particle.FLAME);
                doDamage(eliteEntity, 0.5, 50, 0.5);
                spawnPhase2Particle(eliteEntity.getLivingEntity().getLocation().clone(), Particle.SMOKE);
                if (counter < 20 * 2) return;
                doFlamePyrePhase3(eliteEntity);
            }
        }, 0, 1);
    }

    private void spawnPhase2Particle(Location location, Particle particle) {
        for (int i = 0; i < 10; i++) {
            location.getWorld().spawnParticle(particle, new Location(
                    location.getWorld(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 3 + location.getX(),
                    location.getY(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 3 + location.getZ()
            ), 0, 0, 1, 0, ThreadLocalRandom.current().nextDouble() * 2);
        }
    }

    private void doDamage(EliteEntity eliteEntity, double range1, double range2, double range3) {
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(range1, range2, range3))
            if (entity instanceof LivingEntity)
                BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), (LivingEntity) entity, 1);
    }

    /**
     * Second damage phase / last warning phase
     *
     * @param eliteEntity
     */
    private void doFlamePyrePhase3(EliteEntity eliteEntity) {
        FoliaScheduler.runAtEntityTimer(eliteEntity.getLivingEntity(), new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!eliteEntity.isValid()) {
                    return;
                }
                counter++;
                spawnPhase2Particle(eliteEntity.getLivingEntity().getLocation().clone(), Particle.FLAME);
                doDamage(eliteEntity, 3, 50, 3);
                spawnPhase3Particle(eliteEntity.getLivingEntity().getLocation().clone(), Particle.SMOKE);
                if (counter < 20 * 2) return;
                doFlamePyrePhase4(eliteEntity);
            }
        }, 0, 1);
    }

    private void spawnPhase3Particle(Location location, Particle particle) {
        location.getWorld().spawnParticle(particle, location, 50, 0.01, 0.01, 0.01, 0.1);
    }

    /**
     * Final/full damage phase
     */
    private void doFlamePyrePhase4(EliteEntity eliteEntity) {
        FoliaScheduler.runAtEntityTimer(eliteEntity.getLivingEntity(), new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!eliteEntity.isValid()) {
                    return;
                }
                counter++;
                spawnPhase3Particle(eliteEntity.getLivingEntity().getLocation().clone(), Particle.FLAME);
                doDamage(eliteEntity, 5, 50, 5);
                if (counter < 20 * 2) return;
                eliteEntity.getLivingEntity().setAI(true);
            }
        }, 0, 1);
    }

}
