package com.magmaguy.elitemobs.quests.playercooldowns;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.SchedulerUtil.TaskWrapper;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

public class QuestCooldown implements Serializable {
    @Getter
    private final String permission;
    private final boolean permanent;
    @Getter
    private long targetUnixTime = 0;
    @Getter
    private transient SchedulerUtil.TaskWrapper bukkitTask = null;

    public QuestCooldown(int delayInMinutes, String permission, UUID player) {
        this.permanent = delayInMinutes < 1;
        if (!permanent)
            this.targetUnixTime = System.currentTimeMillis() + 60L * 1000 * delayInMinutes;
        this.permission = permission;
        startCooldown(player);
    }

    public void startCooldown(UUID player) {
        long delay = Math.max((targetUnixTime - System.currentTimeMillis()) / 1000L * 20L, 0L);
        PermissionAttachment permissionAttachment = Objects.requireNonNull(Bukkit.getPlayer(player)).addAttachment(MetadataHandler.PLUGIN);
        if (!permanent && delay < 1) {
            permissionAttachment.unsetPermission(permission);
            return;
        }
        permissionAttachment.setPermission(permission, true);
        Bukkit.getPlayer(player).setMetadata(permission, new LazyMetadataValue(MetadataHandler.PLUGIN, () -> true));
        if (!permanent)
            bukkitTask = SchedulerUtil.runTaskLater((task) -> {
if (Bukkit.getPlayer(player) != null) {
                        permissionAttachment.unsetPermission(permission);
                        Bukkit.getPlayer(player).removeMetadata(permission, MetadataHandler.PLUGIN);
                        PlayerData.updatePlayerQuestCooldowns(player, PlayerData.getPlayerQuestCooldowns(player));
                    }
                }, delay);
    }

}
