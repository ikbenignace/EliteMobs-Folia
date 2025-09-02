package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import com.magmaguy.elitemobs.utils.FoliaScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Implosion extends MinorPower implements Listener {

    public Implosion() {
        super(PowersConfig.getPower("implosion.yml"));
    }

    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteEntity().hasPower(this)) return;

        WrappedTask explosionTask = FoliaScheduler.runAtLocationTimer(event.getEntity().getLocation(), new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter < 20)
                    for (int i = 0; i < 20; i++)
                        event.getEntity().getLocation().getWorld().spawnParticle(Particle.PORTAL, event.getEntity().getLocation(), 1, 0.1, 0.1, 0.1, 1);
                if (counter > 20 * 3) {
                    for (Entity entity : event.getEntity().getWorld().getNearbyEntities(event.getEntity().getLocation(), 10, 10, 10))
                        if (entity instanceof LivingEntity) {
                            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
                            if (eliteEntity instanceof CustomBossEntity customBossEntity) {
                                if (customBossEntity.getCustomBossesConfigFields().isFrozen())
                                    continue;
                            }
                            if (entity.getType().equals(EntityType.PLAYER) && ((Player) entity).getGameMode().equals(GameMode.SPECTATOR))
                                continue;
                            try {
                                entity.setVelocity(event.getEliteEntity().getLocation().clone().subtract(entity.getLocation()).toVector().normalize());
                            } catch (Exception ex) {
                                //Sometimes this is infinite. That just means players shouldn't move.
                            }
                        }
                    return; // Exit the runnable
                }
                counter++;
            }
        }, 1, 1);
        
        // Cancel task when explosion ends
        FoliaScheduler.runLater(() -> {
            if (explosionTask != null) {
                explosionTask.cancel();
            }
        }, 20 * 3 + 5);
    }

}
