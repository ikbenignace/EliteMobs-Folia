package com.magmaguy.elitemobs.dungeons.utility;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.Logger;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
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
            // If we got a fallback world (like main world), provide additional context
            if (isFolia() && world.equals(Bukkit.getWorlds().get(0))) {
                Logger.info("World package '" + worldName + "' installed with limited functionality on Folia (using main world).");
            }
        } else {
            Logger.warn("Failed to install world package '" + worldName + "' - world loading failed. Package will be marked as not installed.");
        }
        return world;
    }

    public static World loadWorld(String worldName, World.Environment environment, ContentPackagesConfigFields contentPackagesConfigFields) {
        // Try multiple approaches for world loading on different server platforms
        World world = null;
        
        // First approach: Check if world already exists and load it
        try {
            world = Bukkit.getWorld(worldName);
            if (world != null) {
                EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
                Logger.info("Using existing world '" + worldName + "' instead of creating new one.");
                return world;
            }
        } catch (Exception e) {
            Logger.warn("Failed to access existing world '" + worldName + "': " + e.getMessage());
        }
        
        // Second approach: Check if world folder exists and try to load it using WorldCreator
        try {
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(worldName);
                creator.environment(environment);
                world = creator.createWorld();
                if (world != null) {
                    EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
                    Logger.info("Successfully loaded existing world '" + worldName + "' using WorldCreator.");
                    return world;
                }
            }
        } catch (UnsupportedOperationException e) {
            Logger.warn("WorldCreator not supported on this platform for '" + worldName + "', trying other approaches...");
        } catch (Exception e) {
            Logger.warn("Failed to load existing world folder for '" + worldName + "': " + e.getMessage());
        }
        
        // Third approach: Try direct world creation using WorldCreator (for new worlds)
        if (!isFolia()) {
            try {
                org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(worldName);
                creator.environment(environment);
                // Create a void world for dungeon purposes
                creator.generateStructures(false);
                creator.type(org.bukkit.WorldType.FLAT);
                creator.generatorSettings("3;minecraft:air;127;");
                
                world = creator.createWorld();
                if (world != null) {
                    EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
                    Logger.info("Successfully created new world '" + worldName + "' using WorldCreator.");
                    return world;
                }
            } catch (UnsupportedOperationException e) {
                Logger.warn("World creation not supported on this platform for '" + worldName + "' (likely Folia), using fallback...");
            } catch (Exception e) {
                Logger.warn("World creation failed for '" + worldName + "': " + e.getMessage() + ", using fallback...");
            }
        } else {
            Logger.warn("Skipping world creation on Folia platform for '" + worldName + "', using fallback...");
        }
        
        // Fourth approach: Try to use main world as fallback for essential functionality
        if (isFolia()) {
            try {
                World mainWorld = Bukkit.getWorlds().get(0);
                if (mainWorld != null) {
                    Logger.warn("World creation not supported on Folia. Using main world as fallback for '" + worldName + "'. Some features may be limited.");
                    // Don't register as EliteMobsWorld since it's not actually the dungeon world
                    return mainWorld;
                }
            } catch (Exception e) {
                Logger.warn("Failed to access main world as fallback: " + e.getMessage());
            }
        }
        
        Logger.warn("All world loading approaches failed for '" + worldName + "'. Dungeon features will be unavailable.");
        return null;
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