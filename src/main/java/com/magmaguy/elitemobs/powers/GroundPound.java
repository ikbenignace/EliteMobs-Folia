package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import com.magmaguy.elitemobs.utils.NonSolidBlockTypes;
import com.magmaguy.elitemobs.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class GroundPound extends MinorPower implements Listener {

    public GroundPound() {
        super(PowersConfig.getPower("ground_pound.yml"));
    }

    private static void cloudParticle(Location location) {
        location.getWorld().spawnParticle(Particle.CLOUD, location, 10, 0.01, 0.01, 0.01, 0.7);
    }

    private static void landCloudParticle(Location location) {
        location.getWorld().spawnParticle(Particle.CLOUD, location, 20, 0.1, 0.01, 0.1, 0.7);
    }

    @EventHandler
    public void onEliteDamaged(EliteMobDamagedByPlayerEvent event) {
        GroundPound groundPound = (GroundPound) event.getEliteMobEntity().getPower(this);
        if (groundPound == null) return;
        if (groundPound.isInGlobalCooldown()) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.10) return;
        groundPound.doGlobalCooldown(20 * 10);

        doGroundPound(event.getEliteMobEntity());

    }

    public void doGroundPound(EliteEntity eliteEntity) {

        //step 1: make boss go up
        SchedulerUtil.runTaskLater((task) -> {
if (!eliteEntity.isValid()) {
                    task.cancel();
                    return;
                }
                eliteEntity.getLivingEntity().setVelocity(new Vector(0, 1.5, 0));
                cloudParticle(eliteEntity.getLivingEntity().getLocation());

            }, 1);

        //step 2: make boss go down
                final int[] counter = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (!eliteEntity.isValid()) {
                    task.cancel();
                    return;
                }
                counter[0]++;
                if (!NonSolidBlockTypes.isPassthrough(eliteEntity.getLivingEntity().getLocation().clone().subtract(new Vector(0, 0.2, 0)).getBlock().getType())) {

                    eliteEntity.getLivingEntity().setVelocity(new Vector(0, -2, 0));
                    cloudParticle(eliteEntity.getLivingEntity().getLocation());

                    SchedulerUtil.runTaskTimer((task) -> {
if (counter[0] > 20 * 5 || !eliteEntity.isValid()) {
                                task.task.cancel();
                                return;}, 20, 1);

    }

}
