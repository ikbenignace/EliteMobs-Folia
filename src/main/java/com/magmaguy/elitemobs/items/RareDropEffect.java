package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.ItemQualityColorizer;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

public class RareDropEffect implements Listener {

    public static void runEffect(Item item) {

        if (!ItemSettingsConfig.isEnableRareItemParticleEffects()) return;
        if (!(ItemQualityColorizer.getItemQuality(item.getItemStack()).equals(ItemQualityColorizer.ItemQuality.LIGHT_BLUE) ||
                ItemQualityColorizer.getItemQuality(item.getItemStack()).equals(ItemQualityColorizer.ItemQuality.GOLD)))
            return;

                final int[] counter = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (item == null || !item.isValid() || item.isDead()) {
                    task.cancel();
                    return;
                }

                item.getWorld().spawnParticle(Particle.PORTAL, item.getLocation(), 5, 0.01, 0.01, 0.01, 0.5);

                counter[0] += 20;
                if (counter[0] > 20 * 60 * 2)
                    task.cancel();
            }, 0, 20);

    }

    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {
        if (!ItemTagger.isEliteItem(event.getEntity().getItemStack())) return;
        runEffect(event.getEntity());
    }

}
