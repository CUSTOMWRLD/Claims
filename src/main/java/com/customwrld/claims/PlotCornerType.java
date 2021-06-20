package com.customwrld.claims;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

public enum PlotCornerType {
    FIRST("First Corner (LMB)", ChatColor.RED, Material.REDSTONE_BLOCK),
    SECOND("Second Corner (RMB)", ChatColor.BLUE, Material.LAPIS_BLOCK),
    OTHER("", ChatColor.WHITE, Material.ORANGE_CONCRETE);

    @Getter
    private final String displayName;
    @Getter
    private final ChatColor color;
    @Getter
    private final Material material;

    PlotCornerType(final String displayName, final ChatColor color, final Material material) {
        this.displayName = displayName;
        this.color = color;
        this.material = material;
    }
}
