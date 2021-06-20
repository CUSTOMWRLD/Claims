package com.customwrld.claims.managers;

import com.customwrld.claims.*;
import com.customwrld.claims.managers.listeners.bukkit.PlotListeners;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;

import java.util.*;

public class PlotsManager {
    private final Set<Plot> plots = new HashSet<>();

    private final Map<OfflinePlayer, Set<Plot>> BY_PLAYER = new HashMap<>();
    private final Map<Set<Chunk>, Plot> BY_CHUNK = new HashMap<>();

    @Getter
    private PlotLoader plotLoader;

    public void registerListeners() {
        //Event Listeners
        Bukkit.getPluginManager().registerEvents(new PlotListeners.PlotEvents(), Claims.get());
        Bukkit.getPluginManager().registerEvents(new PlotListeners.PlotLoaderEvents(), Claims.get());
        Bukkit.getPluginManager().registerEvents(new PlotListeners.PlotGUIs(), Claims.get());
    }

    public void startPlotLoader() {
        plotLoader = new PlotLoader(250); //This will be changed after a player joins
        plotLoader.runTaskTimerAsynchronously(Claims.get(), 0, 1);
    }

    public int getMaxPlotsForRank() {
        //TODO implement
        return 0;
    }

    public int getMaxPlotsForPlayer(HumanEntity player) {
        //TODO change to database manager
        return 2;
    }

    public int getSpaceLeftForPlayer(HumanEntity player) {
        //return BY_PLAYER.containsKey(player) ? BY_PLAYER.get(player).size() : loadPlotsFromDatabase(player).size();
        //TODO change to database manager
        return 0;
    }

    public Set<Plot> getPlayerPlots(OfflinePlayer player) { //Loads the plot without returning anything
        return BY_PLAYER.containsKey(player) ? BY_PLAYER.get(player) : DatabaseManager.loadPlots(player);
    }

    public Set<Plot> getPlotsOnChunk(Chunk chunk) {
        Set<Plot> plots = new HashSet<>();
        BY_CHUNK.forEach((chunks, plot) -> {
            if (chunks.contains(chunk)) {
                plots.add(plot);
            }
        });
        return plots;
    }

    public Plot getPlotOnLocation(World world, int x, int z) {
        Chunk chunk = world.getChunkAt(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        for (Map.Entry<Set<Chunk>, Plot> ent : BY_CHUNK.entrySet()) {
            if (ent.getKey().contains(chunk)) {
                if (ent.getValue().isInside(world, x, z)) {
                    return ent.getValue();
                }
            }
        }
        return null;
    }

    public Plot getPlotOnLocation(Location location) {
        return getPlotOnLocation(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    //Utils
    public Set<Plot> getPlotsIntersectingSession(ClaimingSession session) {
        var sessionPolygon = session.createPolygon(null, null);
        Set<Plot> overlappedPlots = new HashSet<>();
        for (Plot plot : plots) {
            if (plot.getPolygon().intersects(sessionPolygon)) {
                overlappedPlots.add(plot);
            }
        }
        return overlappedPlots;
    }

    public Set<Plot> convertSession(ClaimingSession session) {
        Set<Plot> overlappedPlots = getPlotsIntersectingSession(session);
        Set<Plot> plotsToModify = new HashSet<>();
        for (Plot plot : overlappedPlots) {
            if (session.getOwner().equals(plot.getOwner())) {
                plotsToModify.add(plot);
            } else {
                return new HashSet<>();
            }
        }

        WandAction operation = session.getOperation();
        var sessionPlot = new Plot(session);
        Set<Plot> modifiedPlots = new HashSet<>(); //This is a different set, because plotsToModify can contain plots which got removed by the CUT operation
        if (operation == WandAction.ADD) {
            modifiedPlots.add(sessionPlot);
        }

        boolean isMerged = plotsToModify.size() > 0 && operation == WandAction.ADD;

        //Merge / Cut
        for (Plot plot : plotsToModify) {
            plot.removePreview();
            switch (operation) {
                case ADD:
                    Claims.plotsManager.removePlot(plot);
                    DatabaseManager.deletePlot(plot);

                    //TODO Check if current plot that is merging has newer options than the sessionPlot and use the newer ones
                    sessionPlot.mergeWith(plot);
                    break;
                case CUT:
                    plot.trimWith(sessionPlot);
                    if (plot.getPolygon().isEmpty()) {
                        removePlot(plot);
                    } else {
                        modifiedPlots.add(plot);
                        plot.createPreview();
                    }
                    break;
            }
        }

        if(operation == WandAction.ADD) {
            addPlot(sessionPlot);
            sessionPlot.createPreview();
            DatabaseManager.savePlot(sessionPlot);
        }

        return modifiedPlots;
    }

    //Cache manipulation
    public void addPlot(Plot plot) {
        if (plots.stream().anyMatch(o -> Objects.equals(o.getId(), plot.getId())))
            return;
        plots.add(plot);
        OfflinePlayer owner = plot.getOwner();
        if (BY_PLAYER.containsKey(owner)) {
            BY_PLAYER.get(owner).add(plot);
        } else {
            BY_PLAYER.put(owner, new HashSet<>(Collections.singletonList(plot)));
        }
        BY_CHUNK.put(plot.getChunks(), plot);
    }

    public boolean isPlotInCache(OfflinePlayer player) {
        return BY_PLAYER.containsKey(player);
    }

    public boolean removePlots(OfflinePlayer player) {
        if (BY_PLAYER.containsKey(player)) {
            Set<Plot> plots = BY_PLAYER.get(player);
            for (Plot plot : plots) {
                plots.remove(plot);
                BY_CHUNK.values().removeAll(Collections.singletonList(plot));
            }
            BY_PLAYER.remove(player);
            return true;
        }
        return false;
    }

    public void removePlot(Plot plot) {
        plot.removePreview();

        plots.remove(plot);
        BY_PLAYER.get(plot.getOwner()).remove(plot);
        BY_CHUNK.values().removeAll(Collections.singletonList(plot));
    }
}
