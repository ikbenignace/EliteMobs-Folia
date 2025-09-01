package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.magmaguy.elitemobs.utils.FoliaScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;

public class StringColorAnimator {
    private StringColorAnimator() {
    }

    public static void startTitleAnimation(Player player, String title, String subtitle, ChatColor primaryColor, ChatColor secondaryColor) {

        subtitle = ChatColor.stripColor(subtitle);

        String finalSubtitle = subtitle;
        final int titleSize = title.length();
        final int subtitleSize = finalSubtitle.length();
        final int[] counter = {0};
        final int[] titleIndex = {0};
        final int[] subtitleIndex = {0};
        
        WrappedTask task = FoliaScheduler.runTimer(() -> {
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
                return;
            }

            StringBuilder newSubtitle = new StringBuilder(finalSubtitle).insert(subtitleIndex[0], secondaryColor);
            if (subtitleIndex[0] > 1)
                newSubtitle.insert(subtitleIndex[0] - 2, primaryColor);
            subtitleIndex[0]++;
            player.sendTitle("",
                    secondaryColor + ChatColorConverter.convert(newSubtitle.toString()), 0, 5, 0);
        }, 0, 2);
        
        // Schedule task cancellation when animation should finish
        FoliaScheduler.runLater(() -> {
            if (task != null) task.cancel();
        }, (titleSize + subtitleSize + 5) * 2);
    }

}
