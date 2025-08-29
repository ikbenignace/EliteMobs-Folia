package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;

import java.util.List;

public class CooldownHandler {
    private CooldownHandler() {
    }

    public static void initialize(List list, Object object, int cooldownInTicks) {
        list.add(object);

        SchedulerUtil.runTaskLater(() -> list.remove(object), cooldownInTicks);
    }

}
