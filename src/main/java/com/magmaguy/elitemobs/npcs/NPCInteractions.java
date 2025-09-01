package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRankMenuHandler;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.menus.*;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.QuestInteractionHandler;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import com.magmaguy.elitemobs.thirdparty.FoliaScheduler;

import java.util.HashSet;

public class NPCInteractions implements Listener {

    private static final HashSet<Player> cooldowns = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerNPCInteract(PlayerInteractAtEntityEvent event) {

        if (cooldowns.contains(event.getPlayer())) return;
        cooldowns.add(event.getPlayer());
        FoliaScheduler.runLater(() -> cooldowns.remove(event.getPlayer()), 1);
        if (event.isCancelled()) return;

        NPCEntity npcEntity = EntityTracker.getNPCEntity(event.getRightClicked());
        if (npcEntity == null) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[EliteMobs] You can't rename NPCs using name tags!");
            return;
        }

        event.setCancelled(true);

        switch (npcEntity.getNPCsConfigFields().getInteractionType()) {
            case GUILD_GREETER:
                if (event.getPlayer().hasPermission("elitemobs.rank.npc")) {
                    FoliaScheduler.runLater(() -> {
                        GuildRankMenuHandler.initializeGuildRankMenu(event.getPlayer());
                    }, 1);
                }
                break;
            case CHAT:
                npcEntity.sayDialog(event.getPlayer());
                break;
            case CUSTOM_SHOP:
                if (event.getPlayer().hasPermission("elitemobs.shop.custom.npc"))
                    FoliaScheduler.runLater(() -> {
                        CustomShopMenu.customShopInitializer(event.getPlayer());
                    }, 1);
                break;
            case PROCEDURALLY_GENERATED_SHOP:
                if (event.getPlayer().hasPermission("elitemobs.shop.dynamic.npc"))
                    FoliaScheduler.runLater(() -> {
                        ProceduralShopMenu.shopInitializer(event.getPlayer());
                    }, 1);
                break;
            case QUEST_GIVER:
                if (event.getPlayer().hasPermission("elitemobs.quest.npc"))
                    FoliaScheduler.runLater(() -> {
                        QuestInteractionHandler.processDynamicQuests(event.getPlayer(), npcEntity);
                    }, 1);
                break;
            case CUSTOM_QUEST_GIVER:
                QuestInteractionHandler.processNPCQuests(event.getPlayer(), npcEntity);
                break;
            case BAR:
                event.getPlayer().sendMessage("[EliteMobs] This feature is coming soon!");
                break;
            case SELL:
                if (event.getPlayer().hasPermission("elitemobs.shop.sell.npc"))
                    FoliaScheduler.runLater(() -> {
                        SellMenu sellMenu = new SellMenu();
                        sellMenu.constructSellMenu(event.getPlayer());
                    }, 1);
                break;
            case TELEPORT_BACK:
                if (event.getPlayer().hasPermission("elitemobs.back.npc")) {
                    Location previousLocation = PlayerData.getBackTeleportLocation(event.getPlayer());
                    if (previousLocation == null) {
                        if (npcEntity.npCsConfigFields.noPreviousLocationMessage != null)
                            event.getPlayer().sendMessage(ChatColorConverter.convert(npcEntity.npCsConfigFields.noPreviousLocationMessage));
                    } else
                        PlayerPreTeleportEvent.teleportPlayer(event.getPlayer(), previousLocation);
                }
                break;
            case SCRAPPER:
                if (event.getPlayer().hasPermission("elitemobs.scrap.npc")) {
                    FoliaScheduler.runLater(() -> {
                        ScrapperMenu scrapperMenu = new ScrapperMenu();
                        scrapperMenu.constructScrapMenu(event.getPlayer());
                    }, 1);
                }
                break;
            case REPAIRMAN:
                if (event.getPlayer().hasPermission("elitemobs.repair.npc")) {
                    FoliaScheduler.runLater(() -> {
                        RepairMenu repairMenu = new RepairMenu();
                        repairMenu.constructRepairMenu(event.getPlayer());
                    }, 1);
                }
                break;
            case UNBINDER:
                if (event.getPlayer().hasPermission("elitemobs.unbind.npc")) {
                    FoliaScheduler.runLater(() -> {
                        UnbindMenu unbindMenu = new UnbindMenu();
                        unbindMenu.constructUnbinderMenu(event.getPlayer());
                    }, 1);
                }
                break;
            case ARENA_MASTER:
                FoliaScheduler.runLater(() -> {
                    ArenaMenu arenaMenu = new ArenaMenu();
                    arenaMenu.constructArenaMenu(event.getPlayer(), npcEntity.getNPCsConfigFields().getArenaFilename());
                }, 1);
                break;
            case NONE:
            default:
                break;
            case COMMAND:
                if (npcEntity.getNPCsConfigFields().getCommand() == null) {
                    Logger.warn("Failed to run NPC command because none is configured for " + npcEntity.getNPCsConfigFields().getFilename());
                    return;
                }
                FoliaScheduler.runLater(() -> {
                    event.getPlayer().performCommand(npcEntity.getNPCsConfigFields().getCommand());
                }, 1);
                break;
            case ENHANCER:
            case REFINER:
            case SMELTER:
                event.getPlayer().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cThis feature has been replaced! This NPC should be removed by an admin as soon as possible."));
                if (event.getPlayer().isOp() || event.getPlayer().hasPermission("elitemobs.*")) {
                    event.getPlayer().sendMessage(ChatColorConverter.convert("&2To remove this NPC, use the command &6/em remove &2and hit the NPC!"));
                }
                break;
            case ENCHANTER:
                if (event.getPlayer().hasPermission("elitemobs.enchant.npc"))
                    FoliaScheduler.runLater(() -> {
                        new ItemEnchantmentMenu(event.getPlayer());
                    }, 1);
                break;
            case SCROLL_APPLIER:
                if (event.getPlayer().hasPermission("elitemobs.scroll.npc"))
                    FoliaScheduler.runLater(() -> {
                        new EliteScrollMenu(event.getPlayer());
                    }, 1);
                break;
        }

    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {

        if (!event.getInventory().getType().equals(InventoryType.MERCHANT)) return;

        for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values())
            if (event.getView().getTitle().equals(npcEntity.getNPCsConfigFields().getName())) {
                event.setCancelled(true);
                return;
            }

    }

    public enum NPCInteractionType {
        GUILD_GREETER,
        CHAT,
        CUSTOM_SHOP,
        PROCEDURALLY_GENERATED_SHOP,
        BAR,
        ARENA,
        QUEST_GIVER,
        CUSTOM_QUEST_GIVER,
        NONE,
        SELL,
        TELEPORT_BACK,
        SCRAPPER,
        SMELTER,
        REPAIRMAN,
        ENHANCER,
        REFINER,
        UNBINDER,
        ARENA_MASTER,
        COMMAND,
        ENCHANTER,
        SCROLL_APPLIER
    }


}
