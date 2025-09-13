package com.magmaguy.magmacore.util;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class TemporaryWorldManager {
    
    /**
     * Creates a void temporary world for EliteMobs dungeons
     * This is the original implementation that was working before Folia changes
     */
    public static World loadVoidTemporaryWorld(String worldName, World.Environment environment) {
        try {
            // Check if world already exists
            World existingWorld = Bukkit.getWorld(worldName);
            if (existingWorld != null) {
                return existingWorld;
            }
            
            // Create void world using the original approach
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(environment);
            creator.generateStructures(false);
            creator.type(WorldType.FLAT);
            creator.generatorSettings("3;minecraft:air;127;");
            
            World world = creator.createWorld();
            if (world != null) {
                world.setKeepSpawnInMemory(false);
                world.setDifficulty(Difficulty.HARD);
            }
            return world;
            
        } catch (UnsupportedOperationException e) {
            // This happens on Folia - handle gracefully
            Logger.warn("Dynamic world creation not supported on this server platform (Folia).");
            Logger.warn("To use world '" + worldName + "' on Folia:");
            Logger.warn("1. Pre-create the world using a world management plugin");
            Logger.warn("2. Or copy the world folder to your server and restart");
            return null;
        } catch (Exception e) {
            Logger.warn("Failed to create world '" + worldName + "': " + e.getMessage());
            return null;
        }
    }
}