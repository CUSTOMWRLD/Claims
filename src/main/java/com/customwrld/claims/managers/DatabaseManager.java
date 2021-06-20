package com.customwrld.claims.managers;

import com.customwrld.claims.Claims;
import com.customwrld.claims.Plot;
import com.customwrld.customlib.CustomLib;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;

public class DatabaseManager {
    private MongoClient client;
    private MongoDatabase db;

    private MongoCollection<Document> plots;

    //public void connect(String host) {
//        client = MongoClients.create(new ConnectionString(host));
//    }

    public void init() {
        db = CustomLib.getCustomLib().getMongoDatabase();

        //Collections
        plots = db.getCollection("plots");
    }

    public static void savePlot(Plot plot) {
        MongoCollection<Document> collection = Claims.databaseManager.plots;
        collection.updateOne(Filters.eq(plot.getId()),
                Updates.combine(
                        Updates.set("name", plot.getName()),

                        Updates.set("owner", plot.getOwner().getUniqueId().toString()),
                        Updates.set("world", plot.getWorld().getUID().toString()),

                        Updates.set("createdAt", plot.getCreatedAt()),
                        Updates.set("lastModifiedAt", plot.getLastModifiedAt()),

                        Updates.set("vertices", plot.getVertices()),
                        Updates.set("chunks", plot.getChunkPositions()),

                        Updates.set("options", plot.getOptionsHolder().getSimpleOptions()),

                        Updates.set("permissions", plot.getPermissionsHolder().getSimplePermissions()),
                        Updates.set("groups", plot.getGroupsHolder().getSimpleGroups())
                ),

                new UpdateOptions().upsert(true)
        );
    }

    public static void savePlots(Set<Plot> plots) {
        for (Plot plot : plots) {
            savePlot(plot);
        }
    }

    public static Set<Plot> loadPlots(OfflinePlayer player) {
        Set<Plot> loadedPlots = new HashSet<>();

        if(!Claims.plotsManager.isPlotInCache(player)) {

            MongoCollection<Document> collection = Claims.databaseManager.plots;

            try (MongoCursor<Document> cursor = collection.find(Filters.eq("owner", player.getUniqueId().toString())).cursor()) {
                while (cursor.hasNext()) {
                    Plot plot = new Plot(cursor.next(), null);
                    loadedPlots.add(plot);
                    Claims.plotsManager.addPlot(plot);
                }
            }

        } else {
            loadedPlots = Claims.plotsManager.getPlayerPlots(player);
        }

        return loadedPlots;
    }

    public static Set<Plot> loadPlots(Chunk chunk) {
        Set<Plot> loadedPlots = Claims.plotsManager.getPlotsOnChunk(chunk);

        if(loadedPlots.isEmpty()) {

            MongoCollection<Document> collection = Claims.databaseManager.plots;

            try (MongoCursor<Document> cursor = collection.find(
                    Filters.and(
                            Filters.eq("world", chunk.getWorld().getUID().toString()),
                            Filters.eq("chunks", new Document()
                                            .append("x", chunk.getX())
                                            .append("z", chunk.getZ()))
                    )
            ).cursor()) {
                while (cursor.hasNext()) {
                    var plot = new Plot(cursor.next(), Claims.plotsManager::addPlot);
                    loadedPlots.add(plot);
                }
            }

        }

        return loadedPlots;
    }

    public static void deletePlot(Plot plot) {
        MongoCollection<Document> collection = Claims.databaseManager.plots;
        collection.deleteOne(Filters.eq(plot.getId()));
    }
}