package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import com.magmaguy.elitemobs.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NPCChatBubble {

    public NPCChatBubble(String message, NPCEntity npcEntity, Player player) {

        if (!npcEntity.getNPCsConfigFields().isCanTalk()) return;

        if (message == null) return;
        if (npcEntity.getVillager().hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        if (npcEntity.getIsTalking()) return;
        npcEntity.startTalkingCooldown();

        int lineCounter = 0;

        for (String substring : message.split("\\\\n")) {

            Location newLocation = npcEntity.getVillager().getEyeLocation().clone()
                    .add(player.getLocation().clone().subtract(npcEntity.getVillager().getLocation()).toVector().normalize().multiply(0.5))
                    .add(new Vector(0, -0.4 - (0.2 * lineCounter), 0));

            TextDisplay visualArmorStand = VisualDisplay.generateTemporaryTextDisplay(newLocation, substring);

                    final int[] counter = {0};
        SchedulerUtil.runTaskTimer((task) -> {
if (counter[0] > 20 * 5 || npcEntity.getVillager() == null || !npcEntity.getVillager().isValid() || !visualArmorStand.isValid()) {
                        visualArmorStand.remove();
                        task.cancel();
                        return;
                    }
                    visualArmorStand.teleport(visualArmorStand.getLocation().clone().add(new Vector(0, 0.005, 0)));
                    counter[0]++;
                }, 0, 1);

            lineCounter++;

        }

    }

    public NPCChatBubble(List<String> message, NPCEntity npcEntity, Player player) {

        String selectedMessage = message.get(ThreadLocalRandom.current().nextInt(message.size()));

        new NPCChatBubble(selectedMessage, npcEntity, player);

    }


}
