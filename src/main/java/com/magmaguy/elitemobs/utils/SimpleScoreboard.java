package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import com.magmaguy.elitemobs.utils.SchedulerUtil;

public class SimpleScoreboard {

    public static Scoreboard lazyScoreboard(Player player, String displayName, List<String> scoreboardContents) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        int lineCount = Math.min(scoreboardContents.size(), 15);

        Objective objective = scoreboard.registerNewObjective("test", Criteria.DUMMY, displayName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < lineCount; i++) {
            String scoreString = scoreboardContents.get(i);
            if (scoreString.length() > 40) scoreString = scoreString.substring(0, 39);
            Score score = objective.getScore(scoreString);
            score.setScore(i);
        }

        player.setScoreboard(scoreboard);

        return scoreboard;
    }

    public static Scoreboard temporaryScoreboard(Player player, String displayName, List<String> scoreboardContents, int ticksTimeout) {
        Scoreboard scoreboard = lazyScoreboard(player, displayName, scoreboardContents);
        SchedulerUtil.runTaskLater(() -> {if (player.getScoreboard().equals(scoreboard))
                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());}, ticksTimeout);

        return scoreboard;
    }
}
