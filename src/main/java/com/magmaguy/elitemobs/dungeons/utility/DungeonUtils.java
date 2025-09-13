package com.magmaguy.elitemobs.dungeons.utility;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.TemporaryWorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class DungeonUtils {
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
        if (worldPackage.getContentPackagesConfigFields().getWormholeWorldName() != null)
            loadWorld(worldPackage.getContentPackagesConfigFields().getWormholeWorldName(), environment, worldPackage.getContentPackagesConfigFields());
        if (world != null) worldPackage.setInstalled(true);
        return world;
    }

    public static World loadWorld(String worldName, World.Environment environment, ContentPackagesConfigFields contentPackagesConfigFields) {
        // First try the original MagmaCore approach
        World world = TemporaryWorldManager.loadVoidTemporaryWorld(worldName, environment);
        
        // If MagmaCore fails (returns null), try alternative approaches
        if (world == null) {
            world = createWorldAlternative(worldName, environment);
        }
        
        if (world != null) {
            EliteMobsWorld.create(world.getUID(), contentPackagesConfigFields);
        }
        return world;
    }
    
    /**
     * Alternative world creation method that works around Folia limitations
     */
    private static World createWorldAlternative(String worldName, World.Environment environment) {
        try {
            // First check if world already exists
            World existingWorld = Bukkit.getWorld(worldName);
            if (existingWorld != null) {
                return existingWorld;
            }
            
            // Check if world folder exists
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                // Try to load existing world folder
                try {
                    WorldCreator creator = new WorldCreator(worldName);
                    creator.environment(environment);
                    World world = creator.createWorld();
                    if (world != null) {
                        return world;
                    }
                } catch (UnsupportedOperationException e) {
                    // Folia doesn't support world creation, but we can provide better feedback
                    com.magmaguy.magmacore.util.Logger.warn("Dynamic world creation not supported on this server platform.");
                    com.magmaguy.magmacore.util.Logger.warn("World '" + worldName + "' folder exists but cannot be loaded dynamically.");
                    com.magmaguy.magmacore.util.Logger.warn("Please restart the server to load this world.");
                    return null;
                }
            }
            
            // Try to create a new void world
            try {
                WorldCreator creator = new WorldCreator(worldName);
                creator.environment(environment);
                creator.generateStructures(false);
                creator.type(WorldType.FLAT);
                creator.generatorSettings("3;minecraft:air;127;");
                
                World world = creator.createWorld();
                if (world != null) {
                    world.setKeepSpawnInMemory(false);
                    world.setDifficulty(Difficulty.HARD);
                    return world;
                }
            } catch (UnsupportedOperationException e) {
                // Folia limitation - provide clear feedback
                com.magmaguy.magmacore.util.Logger.warn("World creation not supported on this server platform.");
                com.magmaguy.magmacore.util.Logger.warn("To use EliteMobs worlds on Folia:");
                com.magmaguy.magmacore.util.Logger.warn("1. Use a world management plugin to pre-create the world '" + worldName + "'");
                com.magmaguy.magmacore.util.Logger.warn("2. Or copy the world folder to your server directory and restart");
                return null;
            }
            
        } catch (Exception e) {
            com.magmaguy.magmacore.util.Logger.warn("Failed to create world '" + worldName + "': " + e.getMessage());
        }
        
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