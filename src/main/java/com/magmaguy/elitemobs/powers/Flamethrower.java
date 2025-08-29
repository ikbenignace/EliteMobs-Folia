package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

public class Flamethrower extends BossPower implements Listener {

    public Flamethrower() {
        super(PowersConfig.getPower("flamethrower.yml"));
    }

    private static List<Location> generateDamagePoints(EliteEntity eliteEntity, Location fixedPlayerLocation) {
        List<Location> locations = new ArrayList<>();
        Location eliteMobLocation = eliteEntity.getLivingEntity().getLocation().clone();
        Vector toPlayerVector = fixedPlayerLocation.clone().subtract(eliteMobLocation).toVector().normalize().multiply(0.5);
        for (int i = 0; i < 40; i++)
            locations.add(eliteMobLocation.add(toPlayerVector).clone());
        return locations;
    }

    private static void doDamage(List<Location> locations, EliteEntity eliteEntity) {
        for (Location location : locations)
            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5))
                if (entity instanceof LivingEntity) {
                    if (eliteEntity.getLivingEntity().equals(entity)) continue;
                    BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), (LivingEntity) entity, 1);
                }

    }

    /**
     * Warning phase
     *
     * @param eliteEntity
     */
    private void doFlamethrowerPhase1(EliteEntity eliteEntity, Location fixedPlayerLocation) {

        eliteEntity.getLivingEntity().setAI(false);

                final int[] counter = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (!eliteEntity.isValid()) {
                    task.cancel();
                    return;
                }

                doParticleEffect(eliteEntity, fixedPlayerLocation, Particle.SMOKE);
                counter[0]++;

                if (counter[0] < 20 * 2) return;
                doFlamethrowerPhase2(eliteEntity, fixedPlayerLocation);
                task.cancel();

            }, 0, 1);

    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {

        Flamethrower flameThrower = (Flamethrower) event.getEliteMobEntity().getPower(this);
        if (flameThrower == null) return;
        if (!eventIsValid(event, flameThrower)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        flameThrower.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doFlamethrowerPhase1(event.getEliteMobEntity(), event.getPlayer().getLocation().clone());

    }

    private void doParticleEffect(EliteEntity eliteEntity, Location fixedPlayerLocation, Particle particle) {
        Vector directionVector = fixedPlayerLocation.clone().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().normalize();
        for (int i = 0; i < 5; i++) {
            eliteEntity.getLivingEntity().getWorld().spawnParticle(
                    particle,
                    eliteEntity.getLivingEntity().getEyeLocation().clone().add(directionVector.getX(), -0.5, directionVector.getZ()),
                    0,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getX(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getY(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getZ(),
                    ThreadLocalRandom.current().nextDouble() + 0.05);
        }
    }

    /**
     * Damage phase
     *
     * @param eliteEntity
     */
    private void doFlamethrowerPhase2(EliteEntity eliteEntity, Location fixedPlayerLocation) {
        List<Location> damagePoints = generateDamagePoints(eliteEntity, fixedPlayerLocation);
                final int[] timer = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (!eliteEntity.isValid()) {
                    task.cancel();
                    return;
                }

                doParticleEffect(eliteEntity, fixedPlayerLocation, Particle.FLAME);
                doDamage(damagePoints, eliteEntity);
                timer[0]++;
                if (timer[0] < 20 * 3) return;
                doFlamethrowerPhase3(eliteEntity, fixedPlayerLocation);
                task.cancel();
            }, 0, 1);
    }

    /**
     * Cooldown phase
     *
     * @param eliteEntity
     */
    private void doFlamethrowerPhase3(EliteEntity eliteEntity, Location fixedPlayerLocation) {
                final int[] timer = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (!eliteEntity.isValid()) {
                    task.cancel();
                    return;
                }
                timer[0]++;
                doParticleEffect(eliteEntity, fixedPlayerLocation, Particle.SMOKE);
                if (timer[0] < 20) return;
                task.cancel();
                eliteEntity.getLivingEntity().setAI(true);
            }, 0, 1);
    }

}
