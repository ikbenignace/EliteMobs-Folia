package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

public class SummonTheReturned extends BossPower implements Listener {

    public SummonTheReturned() {
        super(PowersConfig.getPower("summon_the_returned.yml"));
    }

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        if (event.isCancelled()) return;
        SummonTheReturned summonTheReturned = (SummonTheReturned) event.getEliteMobEntity().getPower(this);
        if (summonTheReturned == null) return;
        if (!eventIsValid(event, summonTheReturned)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        summonTheReturned.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doSummonParticles(event.getEliteMobEntity());
    }

    private void doSummonParticles(EliteEntity eliteEntity) {
        eliteEntity.getLivingEntity().setAI(false);
                final int[] counter = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (!eliteEntity.isValid()) {
                    task.cancel();
                    return;
                }
                counter[0]++;
                eliteEntity.getLivingEntity().getWorld().spawnParticle(Particle.PORTAL,
                        eliteEntity.getLivingEntity().getLocation().add(new Vector(0, 1, 0)), 50, 0.01, 0.01, 0.01, 1);
                if (counter[0] < 20 * 3) return;
                task.cancel();
                doSummon(eliteEntity);
                eliteEntity.getLivingEntity().setAI(true);
            }, 0, 1);

    }

    private void doSummon(EliteEntity eliteEntity) {

        for (int i = 0; i < 10; i++) {
            Location spawnLocation = eliteEntity.getLivingEntity().getLocation();

            CustomBossEntity.createCustomBossEntity("the_returned.yml").spawn(spawnLocation, eliteEntity.getLevel(), false);

            double x = ThreadLocalRandom.current().nextDouble() - 0.5;
            double z = ThreadLocalRandom.current().nextDouble() - 0.5;

            try {
                eliteEntity.getLivingEntity().setVelocity(new Vector(x, 0.5, z));
            } catch (Exception ex) {
                Logger.warn("Attempted to complete Summon the Returned power but a reinforcement mob wasn't detected! Did the boss move to an area that prevents spawning?");
            }
        }

    }

}
