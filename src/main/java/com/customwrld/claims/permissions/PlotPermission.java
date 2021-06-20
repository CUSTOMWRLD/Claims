package com.customwrld.claims.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlotPermission {
    BLOCK_BREAK("Break Blocks", "block_break", PlotPermissionCategory.BLOCKS, PlotPermissionLevel.BUILDERS),
    BLOCK_PLACE("Place Blocks", "block_place", PlotPermissionCategory.BLOCKS, PlotPermissionLevel.BUILDERS),
    BLOCK_INTERACT("Interact with Blocks", "block_interact", PlotPermissionCategory.BLOCKS, PlotPermissionLevel.BUILDERS),

    PLAYER_SPLASH_POTIONS("Splash Potions", "player_splash_potions", PlotPermissionCategory.PLAYERS, PlotPermissionLevel.OWNER),
    PLAYER_ATTACK_ANIMALS("Attack Animals", "player_attack_animals", PlotPermissionCategory.PLAYERS, PlotPermissionLevel.OWNER),

    STORAGE_CHESTS("Open Chests", "storage_chests", PlotPermissionCategory.STORAGE, PlotPermissionLevel.OWNER),
    STORAGE_FURNACES("Open Furnaces", "storage_furnaces", PlotPermissionCategory.STORAGE, PlotPermissionLevel.OWNER),
    STORAGE_ANVILS("Open Anvils", "storage_chests", PlotPermissionCategory.STORAGE, PlotPermissionLevel.BUILDERS),
    STORAGE_ENCHANTMENT_TABLE("Open Enchantment Tables", "storage_enchantment_table", PlotPermissionCategory.STORAGE, PlotPermissionLevel.BUILDERS);

    private final String displayName;
    private final String id;
    private final PlotPermissionCategory category;
    private final PlotPermissionLevel defaultLevel;

    public static PlotPermission byId(String id) {
        for (PlotPermission perm : PlotPermission.values()) {
            if (perm.getId().equals(id)) {
                return perm;
            }
        }
        return null;
    }
}
