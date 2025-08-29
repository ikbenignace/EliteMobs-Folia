package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.CooldownHandler;
import com.magmaguy.elitemobs.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FlamethrowerEnchantment extends CustomEnchantment {

    private static final List<Player> playersUsingFlamethrower = new ArrayList<>();
    public static String key = "flamethrower";

    public FlamethrowerEnchantment() {
        super(key, false);
    }

    private static List<Location> generateDamagePoints(Player player, Location fixedPlayerLocation) {
        List<Location> locations = new ArrayList<>();
        Location eliteMobLocation = player.getLocation().clone();
        Vector toPlayerVector = fixedPlayerLocation.clone().subtract(eliteMobLocation).toVector().normalize().multiply(0.5);
        for (int i = 0; i < 40; i++)
            locations.add(eliteMobLocation.add(toPlayerVector).clone());
        return locations;
    }

    private static void doDamage(List<Location> locations, Player player) {
        for (Location location : locations)
            try {
                for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5))
                    if (entity instanceof LivingEntity) {
                        if (entity.getType().equals(EntityType.PLAYER)) continue;
                        if (((LivingEntity) entity).hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) continue;
                        BossCustomAttackDamage.dealCustomDamage(player, (LivingEntity) entity, ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getWeaponLevel(false));
                    }
            } catch (Exception e) {
                //don't really know why this happens, also doesn't really seem to matter. Seems like an internal MC or API issue.
            }
    }


    public static class FlamethrowerEnchantmentEvents implements Listener {

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {

            if (!EnchantmentsConfig.getEnchantment("flamethrower.yml").isEnabled()) return;

            Player player = event.getPlayer();

            if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)))
                return;

            if (playersUsingFlamethrower.contains(player)) return;

            ItemStack zombieKingAxe = player.getInventory().getItemInMainHand();

            if (hasCustomEnchantment(zombieKingAxe, "flamethrower")) {

                if (zombieKingAxe.getDurability() + 4 > zombieKingAxe.getType().getMaxDurability()) {
                    return;
                } else zombieKingAxe.setDurability((short) (zombieKingAxe.getDurability() + 4));


                doFlamethrowerPhase1(player, player.getTargetBlock(null, 30).getLocation().clone().add(0.5, 1, 0.5));
                CooldownHandler.initialize(playersUsingFlamethrower, player, 3 * 60);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 20));

            }

        }

        private void doFlamethrowerPhase1(Player player, Location targetLocation) {

                    final int[] counter = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (!player.isValid() || !player.getLocation().getWorld().equals(targetLocation.getWorld())) {
                        task.cancel();
                        return;
                    }

                    doParticleEffect(player, targetLocation, Particle.SMOKE);
                    counter[0]++;

                    if (counter[0] < 20) return;
                    doFlamethrowerPhase2(player, targetLocation);
                    task.cancel();

                }, 0, 1);

        }

        private void doParticleEffect(Player player, Location target, Particle particle) {
            if (!player.getWorld().equals(target.getWorld())) return;
            Vector directionVector = target.clone().subtract(player.getLocation()).toVector().normalize();
            for (int i = 0; i < 5; i++) {
                player.getWorld().spawnParticle(particle, player.getEyeLocation().clone().add(directionVector.getX(), -0.5, directionVector.getZ()), 0, (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getX(), (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getY(), (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getZ(), ThreadLocalRandom.current().nextDouble() + 0.05);
            }
        }

        /**
         * Damage phase
         *
         * @param player
         */
        private void doFlamethrowerPhase2(Player player, Location target) {
            List<Location> damagePoints = generateDamagePoints(player, target);
                    final int[] timer = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (!player.isValid() || !player.getLocation().getWorld().equals(target.getWorld())) {
                        task.cancel();
                        return;
                    }
                    doParticleEffect(player, target, Particle.FLAME);
                    if (timer[0] % 20 == 0) doDamage(damagePoints, player);
                    timer[0]++;
                    if (timer[0] < 20 * 3) return;
                    doFlamethrowerPhase3(player, target);
                    task.cancel();
                }, 0, 1);
        }


        /**
         * Cooldown phase
         *
         * @param player
         */
        private void doFlamethrowerPhase3(Player player, Location fixedPlayerLocation) {
                    final int[] timer = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (!player.isValid() || !player.getLocation().getWorld().equals(fixedPlayerLocation.getWorld())) {
                        task.cancel();
                        return;
                    }
                    timer[0]++;
                    doParticleEffect(player, fixedPlayerLocation, Particle.SMOKE);
                    if (timer[0] < 20) return;
                    task.cancel();
                }, 0, 1);
        }
    }

}
