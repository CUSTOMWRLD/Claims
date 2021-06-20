package com.customwrld.claims.permissions.groups;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class GroupHolder {
    private Map<PlotGroup, Set<OfflinePlayer>> plotGroups;

    public GroupHolder() {
        plotGroups = new EnumMap<>(PlotGroup.class);
        for (PlotGroup group : PlotGroup.values()) {
            plotGroups.put(group, new HashSet<>());
        }
    }

    public GroupHolder(Object obj) {
        this();

        if(!(obj instanceof Map))
            return;

        Map<?, ?> simpleGroups = (Map<?, ?>) obj;
        for (Map.Entry<?, ?> ent : simpleGroups.entrySet()) {
            if (!(ent.getKey() instanceof String && ent.getValue() instanceof List))
                continue;

            PlotGroup group = PlotGroup.byId((String) ent.getKey());

            if (group == null)
                continue;

            List<?> members = (List<?>) ent.getValue();
            for(Object obj2 : members){
                if(!(obj2 instanceof String))
                    continue;

                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString((String) obj2));
                plotGroups.get(group).add(player);
            }
        }
    }

    public Set<OfflinePlayer> getMembers(PlotGroup group) {
        return plotGroups.getOrDefault(group, new HashSet<>());
    }

    public PlotGroup getGroup(OfflinePlayer player) {
        for (Map.Entry<PlotGroup, Set<OfflinePlayer>> ent : plotGroups.entrySet()) {
            if (ent.getValue().contains(player)) {
                return ent.getKey();
            }
        }
        return null;
    }

    public void setGroup(OfflinePlayer player, PlotGroup group) {
        kick(player);
        plotGroups.get(group).add(player);
    }

    public void kick(OfflinePlayer player) {
        for (Set<OfflinePlayer> members : plotGroups.values()) {
            members.remove(player);
        }
    }

    //Data Transformers

    public Map<String, List<String>> getSimpleGroups() {
        Map<String, List<String>> groups = new HashMap<>();
        for (PlotGroup group : PlotGroup.values()) {
            List<String> members = new ArrayList<>();
            for (OfflinePlayer member : getMembers(group)) {
                members.add(member.getUniqueId().toString());
            }
            groups.put(group.getId(), members);
        }
        return groups;
    }
}