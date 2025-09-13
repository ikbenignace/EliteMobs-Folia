package com.magmaguy.magmacore.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Logger {
    
    public static void info(String message) {
        Bukkit.getLogger().info("[EliteMobs] " + message);
    }
    
    public static void warn(String message) {
        Bukkit.getLogger().warning("[EliteMobs] " + message);
    }
    
    public static void warn(String message, boolean debug) {
        if (debug) {
            Bukkit.getLogger().warning("[EliteMobs DEBUG] " + message);
        } else {
            warn(message);
        }
    }
    
    public static void error(String message) {
        Bukkit.getLogger().severe("[EliteMobs] " + message);
    }
    
    public static void debug(String message) {
        // Debug logging - only show if debug is enabled
        Bukkit.getLogger().info("[EliteMobs DEBUG] " + message);
    }
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public static void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public static void sendSimpleMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public static void sendSimpleMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public static BaseComponent[] hoverMessage(String usage, String description) {
        TextComponent component = new TextComponent(usage);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(description).create()));
        return new BaseComponent[]{component};
    }
    
    public static void showLocation(Location location) {
        // Debug method to show location information
        info("Location: " + location.toString());
    }
}