package com.customwrld.claims.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionBarUtils {
    private static final Map<String, String> actionBarMessages = new HashMap<>();
    private static final Map<Player, String> actionBarPlayers = new HashMap<>();

    public static boolean addPersistentMessage(String msgId, String msg) {
        if (actionBarMessages.containsKey(msgId)) return false;

        actionBarMessages.put(msgId, msg);

        return true;
    }

    public static boolean addPersistentMessage(String msgId, Player player, String msg) {
        if (actionBarMessages.containsKey(msgId)) return false;
        if (actionBarPlayers.containsKey(player)) return false;

        actionBarMessages.put(msgId, msg);
        actionBarPlayers.put(player, msgId);

        return true;
    }

    public static int addPersistentMessage(String msgId, Collection<Player> players, String msg) {
        if (actionBarMessages.containsKey(msgId)) return -1;

        actionBarMessages.put(msgId, msg);

        int i = 0;
        for (Player player : players) {
            if (actionBarPlayers.containsKey(player)) {
                i++;
                continue;
            }

            actionBarPlayers.put(player, msgId);
        }

        return i;
    }


    public static boolean addPlayerToMessage(String msgId, Player player) {
        if (!actionBarMessages.containsKey(msgId)) return false;
        if (actionBarPlayers.containsKey(player)) return false;

        actionBarPlayers.put(player, msgId);

        return true;
    }

    public static int addPlayerToMessage(String msgId, List<Player> players) {
        if (!actionBarMessages.containsKey(msgId)) return -1;

        int i = 0;
        for (Player player : players) {
            if (actionBarPlayers.containsKey(player)) {
                i++;
                continue;
            }

            actionBarPlayers.put(player, msgId);
        }

        return i;
    }


    public static boolean replacePlayerMessage(String msgId, Player player) {
        if (!actionBarMessages.containsKey(msgId)) return false;
        if (!actionBarPlayers.containsKey(player)) return false;

        actionBarPlayers.replace(player, msgId);

        return true;
    }

    public static int replacePlayerMessage(String msgId, List<Player> players) {
        if (!actionBarMessages.containsKey(msgId)) return -1;

        int i = 0;
        for (Player player : players) {
            if (!actionBarPlayers.containsKey(player)) {
                i++;
                continue;
            }

            actionBarPlayers.replace(player, msgId);
        }

        return i;
    }


    public static boolean removePersistentMessage(String msgId) {
        if (!actionBarMessages.containsKey(msgId)) return false;
        actionBarMessages.remove(msgId);

        return true;
    }

    public static int removePersistentMessages(List<String> msgIds) {
        int i = 0;

        for (String msgId : msgIds) {
            if (!actionBarMessages.containsKey(msgId)) {
                i++;
                continue;
            }
            actionBarMessages.remove(msgId);
        }

        return i;
    }


    public static boolean removePlayerFromMessage(Player player) {
        if (!actionBarPlayers.containsKey(player)) {
            return false;
        }
        actionBarPlayers.remove(player);

        return true;
    }

    public static int removePlayersFromMessages(List<Player> players) {
        int i = 0;
        for (Player player : players) {
            if (!actionBarPlayers.containsKey(player)) {
                i++;
                continue;
            }

            actionBarPlayers.remove(player);
        }

        return i;
    }


    public static String getCurrentPersistentMessage(Player player) {
        return actionBarPlayers.getOrDefault(player, null);
    }

    public static String getMessageFromId(String msgId) {
        return actionBarMessages.getOrDefault(msgId, null);
    }


    //Prepare task
    public static void prepare(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Player, String> entry : actionBarPlayers.entrySet()) {
                    entry.getKey().sendActionBar('&', getMessageFromId(entry.getValue()));
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20 * 2);
    }
}
