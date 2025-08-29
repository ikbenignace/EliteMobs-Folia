package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

/**
 * Created by MagmaGuy on 05/11/2016.
 */
public class MovementSpeed extends MinorPower {

    public MovementSpeed() {
        super(PowersConfig.getPower("movement_speed.yml"));
    }

    @Override
    public void applyPowers(LivingEntity livingEntity) {
        SchedulerUtil.runTaskLater(() -> {livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 1));}, 1);
    }

}