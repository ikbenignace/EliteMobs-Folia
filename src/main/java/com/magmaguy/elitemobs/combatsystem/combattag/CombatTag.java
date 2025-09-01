package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

public class CombatTag implements Listener {

    private static Player playerFinder(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player && EntityTracker.isEliteMob(event.getEntity()))
            return (Player) event.getDamager();
        if (event.getEntity() instanceof Player && (EntityTracker.isEliteMob(event.getEntity()) ||
                event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity &&
                        EntityTracker.isEliteMob(((LivingEntity) ((Projectile) event.getDamager()).getShooter()))))
            return (Player) event.getEntity();
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player &&
                EntityTracker.isEliteMob(event.getEntity()))
            return (Player) ((Projectile) event.getDamager()).getShooter();

        return null;

    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {

        Player player = playerFinder(event);

        if (player == null) return;

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        //if (player.isInvulnerable()) player.setInvulnerable(false);
        if (player.isFlying()) {
            player.setFlying(false);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(CombatTagConfig.getCombatTagMessage()));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 60, 0));
            SchedulerUtil.runTaskTimer((task) -> {
if (!player.isOnline() || player.isDead())
                        task.cancel();
                    if (player.isOnGround()) {
                        task.cancel();
                        player.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                }, 0, 1);
        }
    }

}
