package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.FoliaScheduler;
import org.bukkit.entity.Item;

public class VisualItemRemover {

    private VisualItemRemover() {
    }

    public static void removeItems(Object[][] multiDimensionalTrailTracker) {
        for (Object[] objects : multiDimensionalTrailTracker)
            for (Object object : objects) {
                if (!(object instanceof Item item)) continue;
                FoliaScheduler.runTimer(() -> {
                    item.remove();
                    EntityTracker.unregister(item, RemovalReason.EFFECT_TIMEOUT);
                }, 1, 0);
            }
    }

}
