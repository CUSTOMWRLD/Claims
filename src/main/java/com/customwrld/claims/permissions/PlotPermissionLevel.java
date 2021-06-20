package com.customwrld.claims.permissions;

import com.customwrld.claims.permissions.groups.PlotGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum PlotPermissionLevel {
    ALL(ChatColor.GREEN, "Everyone", Material.LIME_CONCRETE, null),
    BUILDERS(ChatColor.GOLD, "Builders", Material.YELLOW_CONCRETE, PlotGroup.BUILDERS),
    TRUSTED(ChatColor.AQUA, "Trusted", Material.LIGHT_BLUE_CONCRETE, PlotGroup.TRUSTED),
    OWNER(ChatColor.RED, "Owner", Material.RED_CONCRETE, null);

    private final ChatColor color;
    private final String displayName;
    private final Material material;
    private final PlotGroup group;

    public PlotPermissionLevel getNext() {
        return PlotPermissionLevel.values()[(this.ordinal() + 1) % PlotPermissionLevel.values().length];
    }
}
