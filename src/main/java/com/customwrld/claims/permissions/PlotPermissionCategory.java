package com.customwrld.claims.permissions;

import com.customwrld.customlib.smartinvs.content.SlotPos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public enum PlotPermissionCategory {
    BLOCKS("Blocks", Material.GRASS_BLOCK, new SlotPos(2, 2)),
    PLAYERS("Players", Material.PLAYER_HEAD, new SlotPos(1, 4)),
    STORAGE("Storage", Material.CHEST, new SlotPos(2, 6));

    private final String displayName;
    private final Material material;
    private final SlotPos slot;

    public List<PlotPermission> getPermissions() {
        List<PlotPermission> permissions = new ArrayList<>();
        for (PlotPermission permission : PlotPermission.values()) {
            if (permission.getCategory().equals(this)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }
}
