package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.utils.FoliaScheduler;
import com.magmaguy.elitemobs.utils.Lerp;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RotatingRay extends Ray {

    private final int animationDuration;
    private final Location originalCenterLocation;
    private final Location target2;
    private final Vector raySegment;
    private final double distanceSquared;
    private WrappedTask rotatingTask;

    public RotatingRay(boolean ignoresSolidBlocks,
                       double pointRadius,
                       Location target,
                       Location target2,
                       double pitchPreRotation,
                       double yawPreRotation,
                       double pitchRotation,
                       double yawRotation,
                       int animationDuration) {
        super(ignoresSolidBlocks, pointRadius, target, target2);
        this.originalCenterLocation = target.clone();
        this.target2 = target2;
        this.animationDuration = animationDuration;
        raySegment = target2.clone().subtract(target).toVector().normalize().multiply(pointRadius * 2);
        if (yawPreRotation != 0)
            raySegment.rotateAroundY(Math.toRadians(yawPreRotation));
        if (pitchPreRotation != 0) {
            Vector perpendicularVector = raySegment.clone().rotateAroundY(Math.toRadians(90));
            raySegment.rotateAroundAxis(perpendicularVector, Math.toRadians(pitchPreRotation));
        }
        locations = drawLine(originalCenterLocation, target2);
        distanceSquared = target.distanceSquared(target2);
        if (animationDuration > 0) startRotating(animationDuration, pitchRotation, yawRotation);
    }

    @Override
    protected List<Location> drawLine(Location location1, Location location2) {
        currentSource = location1;
        currentTarget = location2;
        List<Location> locations = new ArrayList<>();
        Location currentLocation = originalCenterLocation.clone();
        locations.add(originalCenterLocation);
        for (int i = 0; i < maxDistance; i++) {
            currentLocation.add(raySegment);
            if (originalCenterLocation.distanceSquared(currentLocation) > distanceSquared) break;
            if (!ignoresSolidBlocks && currentLocation.getBlock().getType().isSolid()) break;
            locations.add(currentLocation.clone());
        }
        return locations;
    }


    private void startRotating(int totalTickDuration, double pitchRotation, double yawRotation) {
        double singleTickPitchRotation = pitchRotation != 0 ? pitchRotation / totalTickDuration : 0;
        double singleTickYawRotation = yawRotation != 0 ? yawRotation / totalTickDuration : 0;
        Vector perpendicularVector = raySegment.clone().setY(0).normalize().rotateAroundY(Math.toRadians(90));
        rotatingTask = FoliaScheduler.runAtLocationTimer(originalCenterLocation, new Runnable() {
            private int counter = 1;

            @Override
            public void run() {
                if (counter > animationDuration) {
                    if (rotatingTask != null) {
                        rotatingTask.cancel();
                    }
                    return;
                }
                counter++;

                if (target2 != null)
                    centerLocation = Lerp.lerpLocation(originalCenterLocation, target2, counter / (double) animationDuration);

                if (singleTickPitchRotation > 0)
                    raySegment.rotateAroundAxis(perpendicularVector, Math.toRadians(singleTickPitchRotation));
                if (singleTickYawRotation > 0)
                    raySegment.rotateAroundY(Math.toRadians(singleTickYawRotation));
                locations = drawLine(centerLocation,target2);
            }
        }, 1L, 1L);
    }

}
