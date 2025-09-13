package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.utils.FoliaScheduler;
import com.magmaguy.elitemobs.utils.Lerp;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;

public class TranslatingRay extends Ray {
    private final Location finalCenterLocation;
    private WrappedTask animationTask;

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
        animationTask = FoliaScheduler.runAtLocationTimer(startLocation1, new Runnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > animationDuration) {
                    if (animationTask != null) {
                        animationTask.cancel();
                    }
                    return;
                }
                counter++;
                locations = drawLine(
                        Lerp.lerpLocation(startLocation1, endLocation1, counter / (double) animationDuration),
                        Lerp.lerpLocation(startLocation2, endLocation2, counter / (double) animationDuration));
            }
        }, 1L, 1L);
    }
}
