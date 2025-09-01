package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.FoliaScheduler;

import java.util.List;

public class CooldownHandler {
    private CooldownHandler() {
    }

    public static void initialize(List list, Object object, int cooldownInTicks) {
        list.add(object);

        FoliaScheduler.runLater(() -> {
            list.remove(object);
        }, cooldownInTicks);
    }

}
