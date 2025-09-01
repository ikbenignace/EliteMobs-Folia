package com.magmaguy.elitemobs.dungeons.utility;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.TemporaryWorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class DungeonUtils {
    private static Boolean isFolia = null;
    
    /**
     * Check if the server is running on Folia
     * @return true if running on Folia, false otherwise
     */
    private static boolean isFolia() {
        if (isFolia == null) {
            try {
                // Try to access a Folia-specific class
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                isFolia = true;
            } catch (ClassNotFoundException e) {
                isFolia = false;
            }
        }
        return isFolia;
    }
    
    public static Pair getLowestAndHighestLevels(List<CustomBossEntity> customBossEntities) {
        int lowestLevel = 0;
        int highestLevel = 0;

        for (CustomBossEntity customBossEntity : customBossEntities) {
            try {
                int level = customBossEntity.getLevel();
                lowestLevel = lowestLevel == 0 ? level : Math.min(lowestLevel, level);
                highestLevel = highestLevel == 0 ? level : Math.max(highestLevel, level);
            } catch (Exception ex) {
            }
        }
        return new Pair(lowestLevel, highestLevel);
    }

    public static World loadWorld(WorldPackage worldPackage) {
        String worldName = worldPackage.getContentPackagesConfigFields().getWorldName();
        World.Environment environment = worldPackage.getContentPackagesConfigFields().getEnvironment();
        World world = loadWorld(worldName, environment, worldPackage.getContentPackagesConfigFields());
        
        // Handle wormhole world loading with proper error handling
        if (worldPackage.getContentPackagesConfigFields().getWormholeWorldName() != null) {
            World wormholeWorld = loadWorld(worldPackage.getContentPackagesConfigFields().getWormholeWorldName(), environment, worldPackage.getContentPackagesConfigFields());
            if (wormholeWorld == null) {
                Logger.warn("Failed to load wormhole world for package: " + worldPackage.getContentPackagesConfigFields().getWormholeWorldName());
            }
        }
        
        if (world != null) {
            worldPackage.setInstalled(true);
        } else {
            Logger.warn("Failed to install world package '" + worldName + "' - world loading failed");
        }
        return world;
    }

    public static World loadWorld(String worldName, World.Environment environment, ContentPackagesConfigFields contentPackagesConfigFields) {
        // Check if running on Folia and skip world creation to prevent UnsupportedOperationException
        if (isFolia()) {
            Logger.warn("Skipping world creation for '" + worldName + "' - dynamic world creation is not supported on Folia. Some dungeon features may be unavailable.");
            return null;
        }
        
        try {
            World world = TemporaryWorldManager.loadVoidTemporaryWorld(worldName, environment);
            if (world != null) EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
            return world;
        } catch (UnsupportedOperationException e) {
            Logger.warn("Failed to load world '" + worldName + "' - dynamic world creation is not supported on this server platform. Some dungeon features may be unavailable.");
            return null;
        } catch (Exception e) {
            Logger.warn("Failed to load world '" + worldName + "' due to an unexpected error: " + e.getMessage());
            return null;
        }
    }

    public static boolean unloadWorld(WorldPackage worldPackage) {
        World defaultWorld = Bukkit.getWorlds().get(0);
        World wormholeWorld = null;
        if (worldPackage instanceof WorldDungeonPackage && ((WorldDungeonPackage) worldPackage).getWormholeWorld() != null)
            wormholeWorld = ((WorldDungeonPackage) worldPackage).getWormholeWorld();
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getWorld() == worldPackage.getWorld() || player.getWorld() == wormholeWorld)
                if (defaultWorld == null)
                    return false;
                else
                    player.teleport(defaultWorld.getSpawnLocation());
        Bukkit.unloadWorld(worldPackage.getWorld(), false);
        if (worldPackage instanceof WorldDungeonPackage && ((WorldDungeonPackage) worldPackage).getWormholeWorld() != null)
            Bukkit.unloadWorld(((WorldDungeonPackage) worldPackage).getWormholeWorld(), false);

        EliteMobsWorld.destroy(worldPackage.getWorld().getUID());

        return true;
    }

    public static class Pair {
        @Getter
        Integer lowestValue;
        @Getter
        Integer highestValue;

        public Pair(Integer lowestValue, Integer highestValue) {
            this.lowestValue = lowestValue;
            this.highestValue = highestValue;
        }
    }
}