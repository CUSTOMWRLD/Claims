package com.customwrld.claims;

import com.customwrld.claims.managers.DatabaseManager;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PlotLoader extends BukkitRunnable {
    public int bulkSize;

    private final Queue<Chunk> queue;

    public PlotLoader(int bulkSize) {
        this.bulkSize = bulkSize;

        this.queue = new LinkedBlockingQueue<>();
    }

    public void addChunk(Chunk chunk) {
        this.queue.add(chunk);
    }

    @Override
    public void run() {
        for(int i = 0; i < bulkSize; i++) {
            if (queue.isEmpty()) { return; }

            var chunk = queue.poll();
            if (chunk != null) {
                DatabaseManager.loadPlots(chunk);
            }
        }
    }

}
