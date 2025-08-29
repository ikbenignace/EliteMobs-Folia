package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.Lerp;
import com.magmaguy.elitemobs.utils.SchedulerUtil;
import org.bukkit.Location;

public class TranslatingRay extends Ray {
    private final Location finalCenterLocation;

    public TranslatingRay(boolean ignoresSolidBlocks,
                          double pointRadius,
                          Location target,
                          Location finalTarget,
                          Location target2,
                          Location finalTarget2,
                          int animationDuration) {
        super(ignoresSolidBlocks, pointRadius, target, finalTarget);
        this.finalCenterLocation = finalTarget == null ? target : finalTarget;
        locations = drawLine(target, target2);
        startAnimation(target.clone(), finalCenterLocation.clone(), target2.clone(), finalTarget2.clone(), animationDuration);
    }

    private void startAnimation(Location startLocation1,
                                Location endLocation1,
                                Location startLocation2,
                                Location endLocation2,
                                int animationDuration) {
        final int[] counter = {0}; // Counter that persists across executions
        final Object[] taskRef = new Object[1]; // Reference to store the task for cancellation
        taskRef[0] = SchedulerUtil.runTaskTimer(() -> {
            if (counter[0] > animationDuration) {
                SchedulerUtil.cancelTask(taskRef[0]);
                return;
            }
            counter[0]++;
            locations = drawLine(
                    Lerp.lerpLocation(startLocation1, endLocation1, counter[0] / (double) animationDuration),
                    Lerp.lerpLocation(startLocation2, endLocation2, counter[0] / (double) animationDuration));
        }, 1L, 1L);
    }
}
