package com.customwrld.claims.utils;

import cc.minetale.survival.Survival;
import com.customwrld.flame.profile.Profile;

public final class BalanceUtils {
    private BalanceUtils() {}

    public static final String BLOCKS_STORAGE_NAME = "claimableBlocks";

    public static int getBlocks(Profile profile) {
        var storage = profile.getGamemodeStorage(Survival.getGamemode());
        return storage.get(BLOCKS_STORAGE_NAME).asInt();
    }

    public static void addBlocks(Profile profile, int blocks) {
        int newAmount = getBlocks(profile) + blocks;

        var storage = profile.getGamemodeStorage(Survival.getGamemode());
        storage.get(BLOCKS_STORAGE_NAME).setValue(newAmount);
    }

    public static void removeBlocks(Profile profile, int blocks) {
        int newAmount = getBlocks(profile) - blocks;

        var storage = profile.getGamemodeStorage(Survival.getGamemode());
        storage.get(BLOCKS_STORAGE_NAME).setValue(newAmount);
    }

    public static void setBlocks(Profile profile, int blocks) {
        var storage = profile.getGamemodeStorage(Survival.getGamemode());
        storage.get(BLOCKS_STORAGE_NAME).setValue(blocks);
    }

    public static boolean canAffordWithBlocks(Profile profile, int blocks) {
        return getBlocks(profile) >= blocks;
    }

}
