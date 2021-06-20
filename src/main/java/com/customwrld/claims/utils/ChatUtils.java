package com.customwrld.claims.utils;

import com.customwrld.customlib.util.CC;
import com.customwrld.flame.FlameAPI;
import com.customwrld.flame.profile.Profile;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Set;

public class ChatUtils {
    private ChatUtils() { }

    public static ArrayList<String> getGroupMembersLore(Set<OfflinePlayer> members) {
        var lore = new ArrayList<String>();

        var currentLine = new StringBuilder();

        var iterator = members.iterator();
        while(iterator.hasNext()) {
            if(currentLine.length() > 32) {
                lore.add(currentLine.toString());
                currentLine = new StringBuilder();
            }

            OfflinePlayer member = iterator.next();

            var profile = Profile.getByUuid(member.getUniqueId());
            String displayName = (member.isOnline() ?
                    CC.RGB(0x00FF00) :
                    CC.RGB(0xFF0000)) + "● " + ChatColor.RESET + FlameAPI.getChatFormat(profile);

            currentLine
                    .append(displayName)
                    .append(iterator.hasNext() ? ", " : "");
        }

        if(!currentLine.isEmpty())
            lore.add(currentLine.toString());

        return lore;
    }

}
