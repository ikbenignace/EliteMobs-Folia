package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.SchedulerUtil;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class StringColorAnimator {
    private StringColorAnimator() {
    }

    public static void startTitleAnimation(Player player, String title, String subtitle, ChatColor primaryColor, ChatColor secondaryColor) {

        subtitle = ChatColor.stripColor(subtitle);

        String finalSubtitle = subtitle;
        final int[] counter = {0}; // Counter that persists across executions
        final int[] titleIndex = {0}; // Title index that persists across executions
        final int[] subtitleIndex = {0}; // Subtitle index that persists across executions
        final Object[] taskRef = new Object[1]; // Reference to store the task for cancellation
        
        final int titleSize = title.length();
        final int subtitleSize = finalSubtitle.length();
        
        taskRef[0] = SchedulerUtil.runTaskTimer(() -> {
            counter[0]++;

            if (titleIndex[0] <= titleSize) {
                StringBuilder newTitle = new StringBuilder(title).insert(titleIndex[0], primaryColor);
                if (titleIndex[0] > 1)
                    newTitle.insert(titleIndex[0] - 2, secondaryColor);
                titleIndex[0]++;
                player.sendTitle(primaryColor + ChatColorConverter.convert(newTitle.toString()),
                        secondaryColor + ChatColorConverter.convert(finalSubtitle), 0, 5, 0);
                return;
            }

            if (subtitleIndex[0] > subtitleSize) {
                SchedulerUtil.cancelTask(taskRef[0]);
                return;
            }

            StringBuilder newSubtitle = new StringBuilder(finalSubtitle).insert(subtitleIndex[0], secondaryColor);
            if (subtitleIndex[0] > 1)
                newSubtitle.insert(subtitleIndex[0] - 2, primaryColor);
            subtitleIndex[0]++;
            player.sendTitle("",
                    secondaryColor + ChatColorConverter.convert(newSubtitle.toString()), 0, 5, 0);
        }, 0, 2);

    }

}
