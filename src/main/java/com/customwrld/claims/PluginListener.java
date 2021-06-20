package com.customwrld.claims;

import cc.minetale.survival.Survival;
import com.customwrld.claims.utils.BalanceUtils;
import com.customwrld.flame.profile.GamemodeStorage;
import com.customwrld.flame.profile.Profile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PluginListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        profile.ensureGamemodeStorage(Survival.getGamemode(),
                List.of(new GamemodeStorage.StorageValue(BalanceUtils.BLOCKS_STORAGE_NAME, 200, true)));
    }
}
