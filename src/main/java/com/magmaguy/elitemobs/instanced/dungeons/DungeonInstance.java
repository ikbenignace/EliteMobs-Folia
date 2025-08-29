package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonCompleteEvent;
import com.magmaguy.elitemobs.api.DungeonStartEvent;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.api.WorldInstanceEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import com.magmaguy.elitemobs.utils.SchedulerUtil;
import com.magmaguy.magmacore.util.FileUtils;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DungeonInstance extends MatchInstance {
    @Getter
    private static final Set<DungeonInstance> dungeonInstances = new HashSet<>();
    private final List<DungeonObjective> dungeonObjectives = new ArrayList<>();
    @Getter
    private World world;
    @Getter
    private String instancedWorldName;
    private File instancedWorldFile;
    @Getter
    private ContentPackagesConfigFields contentPackagesConfigFields;
    private List<InstancedBossEntity> instancedBossEntities = new ArrayList<>();
    @Getter
    private int levelSync = -1;
    private String difficultyName = null;
    @Getter
    private String difficultyID = null;

    public DungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                           Location lobbyLocation,
                           Location startLocation,
                           World world,
                           File instancedWorldFile,
                           Player player,
                           String difficultyName) {
        super(startLocation,
                null, //todo: the end location is currently not definable
                contentPackagesConfigFields.getMinPlayerCount(),
                contentPackagesConfigFields.getMaxPlayerCount());
        if (cancelled) return;
        super.lobbyLocation = lobbyLocation;
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        for (String rawObjective : contentPackagesConfigFields.getRawDungeonObjectives())
            this.dungeonObjectives.add(DungeonObjective.registerObjective(this, rawObjective));
        this.world = world;
        this.instancedWorldName = world.getName();
        this.instancedWorldFile = instancedWorldFile;
        this.difficultyName = difficultyName;
        setDifficulty(difficultyName);
        addNewPlayer(player);
        SchedulerUtil.runTaskLater(() -> {
            NPCEntity.initializeInstancedNPCs(contentPackagesConfigFields.getWorldName(), world, players.size(), dungeonInstance);
            TreasureChest.initializeInstancedTreasureChests(contentPackagesConfigFields.getWorldName(), world);
        }, 20 * 3L);
        dungeonInstances.add(this);
        super.permission = contentPackagesConfigFields.getPermission();
    }

    public static void setupInstancedDungeon(Player player, String instancedDungeonConfigFieldsString, String difficultyName) {
        ContentPackagesConfigFields instancedDungeonsConfigFields = ContentPackagesConfig.getDungeonPackages().get(instancedDungeonConfigFieldsString);
        if (instancedDungeonsConfigFields == null) {
            player.sendMessage("[EliteMobs] Failed to get data for dungeon " + instancedDungeonConfigFieldsString + "! The dungeon will not start.");
            return;
        }

        if (instancedDungeonsConfigFields.getPermission() != null && !instancedDungeonsConfigFields.getPermission().isEmpty())
            if (!player.hasPermission(instancedDungeonsConfigFields.getPermission())) {
                player.sendMessage("[EliteMobs] You don't have the permission to go to this dungeon!");
                return;
            }

        String instancedWorldName = WorldInstantiator.getNewWorldName(instancedDungeonsConfigFields.getWorldName());

        if (!launchEvent(instancedDungeonsConfigFields, instancedWorldName, player)) return;

        CompletableFuture<File> future = CompletableFuture.supplyAsync(() ->
                cloneWorldFiles(instancedDungeonsConfigFields, instancedWorldName, player));
        future.thenAccept(file -> {
            if (file == null) return;
            SchedulerUtil.runTask(() -> initializeInstancedWorld(instancedDungeonsConfigFields, instancedWorldName, player, file, difficultyName));
        });
    }

    protected static boolean launchEvent(ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player) {
        WorldInstanceEvent worldInstanceEvent = new WorldInstanceEvent(
                instancedDungeonsConfigFields.getWorldName(),
                instancedWordName,
                instancedDungeonsConfigFields);
        new EventCaller(worldInstanceEvent);
        if (worldInstanceEvent.isCancelled()) {
            player.sendMessage("[EliteMobs] Something cancelled the instancing event! The dungeon will not start.");
            return false;
        }
        return true;
    }

    protected static File cloneWorldFiles(ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player) {
        File targetFile = WorldInstantiator.cloneWorld(instancedDungeonsConfigFields.getWorldName(), instancedWordName, instancedDungeonsConfigFields.getDungeonConfigFolderName());
        if (targetFile == null) {
            player.sendMessage("[EliteMobs] Failed to copy the world! Report this to the dev. The dungeon will not start.");
            return null;
        }
        return targetFile;
    }

    protected static DungeonInstance initializeInstancedWorld(ContentPackagesConfigFields instancedDungeonsConfigFields,
                                                              String instancedWordName,
                                                              Player player,
                                                              File targetFile,
                                                              String difficultyName) {
        World world = DungeonUtils.loadWorld(instancedWordName, instancedDungeonsConfigFields.getEnvironment(), instancedDungeonsConfigFields);
        if (world == null) {
            player.sendMessage("[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.");
            return null;
        }

        //Location where players are teleported to start completing the dungeon
        Location startLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getStartLocationString());
        startLocation.setWorld(world);
        //Lobby location is optional, if null it should be the same as the start location
        Location lobbyLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getTeleportLocationString());
        if (lobbyLocation != null) lobbyLocation.setWorld(world);
        else lobbyLocation = startLocation;
        //Location where players are teleported to upon completion, this usually gets overriden with the previous location players were at
        //todo: will probably want to define this at some point Location endLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getEndLocation());
        //endLocation.setWorld(world);
        if (!instancedDungeonsConfigFields.isEnchantmentChallenge())
            return new DungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world, targetFile, player, difficultyName);
        else
            return new EnchantmentDungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world, targetFile, player, difficultyName);
    }

    @Override
    public boolean addNewPlayer(Player player) {
        if (!super.addNewPlayer(player)) return false;
        if (levelSync > 0)
            player.sendMessage("[EliteMobs] Dungeon difficulty is set to " + difficultyName + " ! Level sync caps your item level to " + levelSync + ".");
        return true;
    }

    @Override
    protected void startMatch() {
        updateBossHealth();
        super.startMatch();
        new EventCaller(new DungeonStartEvent(this));
    }

    //Runs when the instance starts, adjusting boss health to the amount of players in the instance
    private void updateBossHealth() {
        instancedBossEntities.forEach(instancedBossEntity -> {
            instancedBossEntity.setNormalizedMaxHealth(players.size());
        });
    }

    public boolean checkCompletionStatus() {
        //if (!super.state.equals(InstancedRegionState.ONGOING)) return;
        for (DungeonObjective dungeonObjective : dungeonObjectives)
            if (!dungeonObjective.isCompleted())
                return false;
        new EventCaller(new DungeonCompleteEvent(this));
        //This means the dungeon just completed
        victory();
        return true;
    }

    @Override
    public void endMatch() {
        super.endMatch();
        if (players.isEmpty()) {
            removeInstance();
            return;
        }
        announce(DungeonsConfig.getInstancedDungeonCompleteMessage());
        SchedulerUtil.runTaskLater(() -> destroyMatch(), 2 * 60 * 20L);
    }

    @Override
    public void destroyMatch() {
        super.destroyMatch();
        removeInstance();
    }

    public void removeInstance() {
        participants.forEach(player -> player.sendMessage(DungeonsConfig.getInstancedDungeonClosingInstanceMessage()));
        HashSet<Player> participants = new HashSet<>(this.participants);
        participants.forEach(this::removeAnyKind);
        instances.remove(this);
        DungeonInstance dungeonInstance = this;
        if (world == null) {
            Logger.warn("Instanced dungeon's world was already unloaded before removing the entities in it! This shouldn't happen, but doesn't break anything.");
            return;
        }
        world.getEntities().forEach(entity -> EntityTracker.unregister(entity, RemovalReason.WORLD_UNLOAD));
        SchedulerUtil.runTaskLater(() -> {
            new EventCaller(new InstancedDungeonRemoveEvent(dungeonInstance));
            dungeonInstances.remove(dungeonInstance);

            if (!Bukkit.unloadWorld(world, false)) {
                Logger.warn("Failed to unload world " + instancedWorldName + " ! This is bad, report this to the developer!");
                return;
            }
            SchedulerUtil.runTaskLaterAsync(() -> {try{
                        FileUtils.deleteDirectory(instancedWorldFile);} catch (Exception e){
                        Logger.warn("Failed to delete " + instancedWorldFile + " ! This is bad, report this to the developer!");
                    }}, 20L * 60 * 2); //wait 2 minutes after unloading world before removing files
        }, 20 * 30L);
    }

    private void setDifficulty(String difficultyName) {
        if (difficultyName == null) return;
        if (contentPackagesConfigFields.getDifficulties() == null ||
                contentPackagesConfigFields.getDifficulties().isEmpty())
            return;
        Map difficulty = null;
        for (Map difficultyMap : contentPackagesConfigFields.getDifficulties())
            if (difficultyMap.get("name") != null && difficultyMap.get("name").equals(difficultyName)) {
                difficulty = difficultyMap;
                break;
            }
        if (difficulty == null) {
            Logger.warn("Failed to set difficulty " + difficulty + " for instanced dungeon " + contentPackagesConfigFields.getFilename());
            return;
        }

        if (difficulty.get("levelSync") != null) {
            try {
                this.levelSync = MapListInterpreter.parseInteger("levelSync", difficulty.get("levelSync"), contentPackagesConfigFields.getFilename());
            } catch (Exception exception) {
                Logger.warn("Incorrect level sync entry for dungeon " + contentPackagesConfigFields.getFilename() + " ! Value: " + levelSync + " . No level sync will be applied!");
                this.levelSync = 0;
            }
        } else
            this.levelSync = 0;

        //Used for loot
        if (difficulty.get("id") != null) {
            this.difficultyID = MapListInterpreter.parseString("id", difficulty.get("id"), contentPackagesConfigFields.getFilename());
        }
    }

    @Override
    protected boolean isInRegion(Location location) {
        return location.getWorld().equals(startLocation.getWorld());
    }




}
