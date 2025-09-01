package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.CriticalStrikesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customenchantments.CriticalStrikesEnchantment;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.DialogArmorStand;
import com.magmaguy.elitemobs.utils.FoliaScheduler;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import com.magmaguy.magmacore.util.Round;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class PopupDisplay implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        if (!MobCombatSettingsConfig.isDisplayDamageOnHit()) return;

        Location mobLocation = event.getEliteMobEntity().getLocation();

        Vector offset = new Vector(ThreadLocalRandom.current().nextDouble(-2, 2), 0, ThreadLocalRandom.current().nextDouble(-2, 2));

        String colorPrefix = "";
        if (event.getDamageModifier() < 1) {
            //resist
            colorPrefix += MobCombatSettingsConfig.getResistTextColor();
            DialogArmorStand.createDialogArmorStand(event.getEliteMobEntity().getUnsyncedLivingEntity(),
                    MobCombatSettingsConfig.getResistText(), offset.clone().subtract(new Vector(0, 0.2, 0)));
            mobLocation.getWorld().playSound(mobLocation, Sound.BLOCK_ANVIL_USE, 1f, 1f);
            if (MobCombatSettingsConfig.isDoResistEffect())
                resistArmorStandCreator(event.getEliteMobEntity(), event.getPlayer(), Material.SHIELD);
        } else if (event.getDamageModifier() > 1) {
            //weak
            colorPrefix += MobCombatSettingsConfig.getWeakTextColor();
            DialogArmorStand.createDialogArmorStand(event.getEliteMobEntity().getUnsyncedLivingEntity(),
                    MobCombatSettingsConfig.getWeakText(), offset.clone().subtract(new Vector(0, 0.2, 0)));
            mobLocation.getWorld().playSound(mobLocation, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            if (MobCombatSettingsConfig.isDoWeakEffect())
                weakArmorStandCreator(event.getEliteMobEntity(), event.getPlayer(), Material.DIAMOND_SWORD);
        }
        if (event.isCriticalStrike()) {
            //crit
            colorPrefix += CriticalStrikesConfig.getCriticalHitColor();
            CriticalStrikesEnchantment.criticalStrikePopupMessage(event.getEliteMobEntity().getUnsyncedLivingEntity(), new Vector(0, 0.2, 0));
        }

        DialogArmorStand.createDialogArmorStand(event.getEliteMobEntity().getUnsyncedLivingEntity(), ChatColor.RED +
                colorPrefix + ChatColor.BOLD + Round.twoDecimalPlaces(event.getDamage()), offset);

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EliteMobHealEvent event) {
        if (!MobCombatSettingsConfig.isDisplayDamageOnHit()) return;
        if (!event.getEliteEntity().isValid()) return;

        Vector offset = new Vector(ThreadLocalRandom.current().nextDouble(-1, 1), 0, ThreadLocalRandom.current().nextDouble(-1, 1));

        if (event.isFullHeal()) {
            DialogArmorStand.createDialogArmorStand(event.getEliteEntity().getUnsyncedLivingEntity(),
                    MobCombatSettingsConfig.getFullHealMessage(), offset.clone().subtract(new Vector(0, 0.2, 0)));
        } else {
            DialogArmorStand.createDialogArmorStand(event.getEliteEntity().getUnsyncedLivingEntity(),
                    ChatColor.GREEN + "" + event.getHealAmount() + " HP HEAL!", offset.clone().subtract(new Vector(0, 0.2, 0)));
        }

    }

    private void resistArmorStandCreator(EliteEntity eliteEntity, Player player, Material material) {
        if (!eliteEntity.isValid() || !player.isValid() || !eliteEntity.getLocation().getWorld().equals(player.getWorld()))
            return;

        ArmorStand armorStand = VisualDisplay.generateTemporaryArmorStand(getResistLocation(player, eliteEntity), "Resist");
        armorStand.getEquipment().setItemInMainHand(new ItemStack(material));
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.setRightArmPose(new EulerAngle(Math.PI / 2d, Math.PI + Math.PI / 2d, Math.PI));

        FoliaScheduler.runAtLocationTimer(eliteEntity.getLocation(), new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 20 || !eliteEntity.isValid() || !player.isValid() || !eliteEntity.getLocation().getWorld().equals(player.getWorld())) {
                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                    return;
                }
                try {
                    armorStand.teleport(getResistLocation(player, eliteEntity));
                } catch (Exception e) {
                    //Sometimes, very rarely, x is not finite. Doesn't really matter.
                }
                counter++;
            }
        }, 1, 1);
    }

    private Location getResistLocation(Player player, EliteEntity eliteEntity) {
        Vector armorsStandVector = player.getLocation().subtract(eliteEntity.getLocation()).toVector().normalize().multiply(1.5);
        Location armorStandLocation = eliteEntity.getLocation().add(armorsStandVector);
        armorStandLocation.setDirection(armorsStandVector);
        return armorStandLocation;
    }

    private void weakArmorStandCreator(EliteEntity eliteEntity, Player player, Material material) {
        if (!eliteEntity.isValid() || !player.isValid() || !eliteEntity.getLocation().getWorld().equals(player.getWorld()))
            return;

        TextDisplay[] textDisplays = new TextDisplay[2];
        textDisplays[0] = generateWeakArmorStand(player, eliteEntity, material, -1);
        textDisplays[1] = generateWeakArmorStand(player, eliteEntity, material, 1);

        FoliaScheduler.runAtLocationTimer(eliteEntity.getLocation(), new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 10 || !eliteEntity.isValid() || !player.isValid() || !eliteEntity.getLocation().getWorld().equals(player.getWorld())) {
                    EntityTracker.unregister(textDisplays[0], RemovalReason.EFFECT_TIMEOUT);
                    EntityTracker.unregister(textDisplays[1], RemovalReason.EFFECT_TIMEOUT);
                    return;
                }
                for (TextDisplay armorStand : textDisplays)
                    armorStand.teleport(armorStand.getLocation().add(eliteEntity.getLocation().add(new Vector(0, 0, 0))
                            .subtract(armorStand.getLocation()).toVector().normalize().multiply(.4)));
                counter++;
            }
        }, 1, 1);
    }

    private TextDisplay generateWeakArmorStand(Player player, EliteEntity eliteEntity, Material material, int offset) {
        Vector armorsStandVector = player.getLocation().clone().add(new Vector(0, 2, 0)).subtract(eliteEntity.getLocation()).toVector().normalize().multiply(3.0).rotateAroundY(Math.PI / 8 * offset);
        Location armorStandLocation = eliteEntity.getLocation().add(armorsStandVector);
        armorStandLocation.setDirection(armorsStandVector.multiply(-1));
        return VisualDisplay.generateTemporaryTextDisplay(armorStandLocation, "Weak");
    }

}
