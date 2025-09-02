package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import com.magmaguy.elitemobs.utils.FoliaScheduler;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class FrostWalker extends MinorPower {

    public FrostWalker() {
        super(PowersConfig.getPower("frost_walker.yml"));
    }

    @Override
    public void applyPowers(LivingEntity livingEntity) {
        FoliaScheduler.runLater(() -> {
            ItemStack frostWalkerBoots = new ItemStack(Material.LEATHER_BOOTS);
            frostWalkerBoots.addEnchantment(Enchantment.FROST_WALKER, 2);
            frostWalkerBoots.addEnchantment(Enchantment.DEPTH_STRIDER, 3);
            livingEntity.getEquipment().setBoots(frostWalkerBoots);
        }, 1);
    }

}
