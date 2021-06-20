package com.customwrld.claims.managers;

import com.customwrld.claims.Claims;
import com.customwrld.claims.Plot;
import com.customwrld.claims.holograms.HologramArea;
import com.customwrld.claims.utils.GeometryUtils;
import com.customwrld.customlib.util.MetadataUtil;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PreviewManager {

    public static final String ACTIVE_PREVIEWS_KEY = "claims-activePreviews";

    @Getter private static final Set<Player> players = new HashSet<>();
    @Getter private static final BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {

                if(players.isEmpty()) { return; }

                for(Player player : players) {

                    World world = player.getWorld();
                    Chunk chunk = player.getChunk();

                    int originX = chunk.getX();
                    int originZ = chunk.getZ();

                    //Enters preview range
                    for(int x = originX - 8; x < originX + 8; x++) {
                        for(int z = originZ - 8; z < originZ + 8; z++) {

                            Chunk otherChunk = world.getChunkAt(x, z);

                            if(!otherChunk.isLoaded()) { continue; }

                            Set<Plot> plots = Claims.plotsManager.getPlotsOnChunk(otherChunk);

                            plots.forEach(plot -> {
                                var preview = plot.getPreview();
                                if(preview == null || preview.getViewers().contains(player)) { return; }

                                preview.render(player.getPlayer());

                                showPreviewToPlayer(player, preview);
                            });

                        }
                    }

                    //Leaves preview range
                    hidePreviewsNotInRange(player, 512);
                }

            }
        }.runTaskTimer(Claims.get(), 0, 40L);

    //Autopreview
    public static boolean enableAutoPreview(Player player) {
        return players.add(player);
    }

    public static boolean disableAutoPreview(Player player) {
        hideAllPreviews(player);
        return players.remove(player);
    }

    public static boolean hasAutoPreviewEnabled(Player player) {
        return players.contains(player);
    }

    public static boolean toggleAutoPreview(Player player) {
        if(!hasAutoPreviewEnabled(player)) {
            enableAutoPreview(player);
            return true;
        } else {
            disableAutoPreview(player);
            return false;
        }
    }

    //Preview Management
    public static void showPreviewToPlayer(Player player, HologramArea preview) {
        List<HologramArea> areas = MetadataUtil.getList(player, ACTIVE_PREVIEWS_KEY, Claims.get(), new ArrayList<>(), HologramArea.class, false);
        if(!areas.contains(preview)) { areas.add(preview); }
        player.setMetadata(ACTIVE_PREVIEWS_KEY, new FixedMetadataValue(Claims.get(), areas));
    }

    public static void hidePreviewFromPlayer(Player player, HologramArea preview) {
        preview.delete(player);
        MetadataUtil.removeFromListAndDeleteIfEmpty(player, ACTIVE_PREVIEWS_KEY, Claims.get(), preview);
    }

    public static void hidePreviewsNotInRange(Player player, int range) {
        if(!player.hasMetadata(ACTIVE_PREVIEWS_KEY)) { return; }

        List<HologramArea> areas = MetadataUtil.getList(player, ACTIVE_PREVIEWS_KEY, Claims.get(), null, HologramArea.class, false);
        if(areas == null) { return; }

        for (HologramArea area : areas) {
            if (!area.getPolygon().getCentroid().isWithinDistance(GeometryUtils.bukkitLocationToJTSPoint(player.getLocation()), range)) {
                areas.remove(area);
                area.delete(player);
            }
        }

        if(areas.isEmpty()) {
            player.removeMetadata(ACTIVE_PREVIEWS_KEY, Claims.get());
        } else {
            player.setMetadata(ACTIVE_PREVIEWS_KEY, new FixedMetadataValue(Claims.get(), areas));
        }
    }

    public static void hideAllPreviews(Player player) {
        if(!player.hasMetadata(ACTIVE_PREVIEWS_KEY)) { return; }

        List<HologramArea> areas = MetadataUtil.getList(player, ACTIVE_PREVIEWS_KEY, Claims.get(), null, HologramArea.class, false);
        if(areas == null) { return; }

        for(HologramArea area : areas) {
            area.delete(player);
        }

        player.removeMetadata(ACTIVE_PREVIEWS_KEY, Claims.get());
    }

}
