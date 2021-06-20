package com.customwrld.claims.holograms;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.customwrld.claims.utils.GeometryUtils;
import com.customwrld.customlib.hologram.Hologram;
import com.customwrld.customlib.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

public class HologramArea {

    @Getter @Setter private World world;

    @Getter private List<Player> viewers = new ArrayList<>();

    @Getter private Polygon polygon;
    @Getter private boolean isUpdating;

    @Getter private List<HologramLine> lines;

    @Getter @Setter private boolean highestBlocksOnly;

    @Getter @Setter private Material lineMaterial;
    @Getter @Setter private Material cornerMaterial;

    @Getter @Setter private Material startCornerMaterial;
    @Getter @Setter private Material endCornerMaterial;

    @Getter private Hologram middleHologram;

    @Getter @Setter private BiConsumer<Player, HologramArea> hologramModifier;
    @Getter @Setter private BiConsumer<Player, HologramLine> hologramLineModifier;

    private HologramArea(World world, Polygon polygon, boolean highestBlocksOnly) {
        this.world = world;
        this.polygon = polygon;
        this.highestBlocksOnly = highestBlocksOnly;
    }

    public HologramArea(World world, Polygon polygon, boolean highestBlocksOnly,
                        Material lineMaterial, Material cornerMaterial, Material startCornerMaterial, Material endCornerMaterial,
                        Hologram startHologram, Hologram middleLineHologram, Hologram endHologram) {

        this(world, polygon, highestBlocksOnly);

        this.lineMaterial = lineMaterial;
        this.cornerMaterial = cornerMaterial;

        this.startCornerMaterial = startCornerMaterial;
        this.endCornerMaterial = endCornerMaterial;

        lines = HologramLine.fromPolygon(world, polygon, 0,

                highestBlocksOnly,

                cornerMaterial,
                cornerMaterial,
                lineMaterial,

                startHologram,
                middleLineHologram,
                endHologram);

        if (startCornerMaterial != null) {
            //Don't set it on a beginning of the first line due to that block being drawn over by an ending of the last line.
            //So to fix that just set for the last line.
            lines.get(0).setStartMaterial(startCornerMaterial);
            lines.get(lines.size() - 1).setEndMaterial(startCornerMaterial);
        }

        if (endCornerMaterial != null) {
            //Almost always use the beginning for changing endings of a line due it's ending block being drawn over the next one's beginning.
            lines.get((lines.size() / 2) - 1).setEndMaterial(endCornerMaterial);
            lines.get(lines.size() / 2).setStartMaterial(endCornerMaterial);
        }

    }

    public HologramArea(World world, Polygon polygon, boolean highestBlocksOnly,
                        Material lineMaterial, Material cornerMaterial,
                        Hologram startHologram, Hologram middleLineHologram, Hologram endHologram) {

        this(world, polygon, highestBlocksOnly,
                lineMaterial, cornerMaterial, cornerMaterial, cornerMaterial,
                startHologram, middleLineHologram, endHologram);

    }

    public HologramArea(World world, Polygon polygon, boolean highestBlocksOnly,
                        Material lineMaterial, Material cornerMaterial,
                        Hologram middleLineHologram) {

        this(world, polygon, highestBlocksOnly,
                lineMaterial, cornerMaterial, cornerMaterial, cornerMaterial,
                null, middleLineHologram, null);

    }

    public HologramArea(World world, Polygon polygon, boolean highestBlocksOnly,
                        Material lineMaterial, Material cornerMaterial, Material startCornerMaterial, Material endCornerMaterial) {

        this(world, polygon, highestBlocksOnly,
                lineMaterial, cornerMaterial, startCornerMaterial, endCornerMaterial,
                null, null, null);
    }

    public HologramArea(World world, Polygon polygon, boolean highestBlocksOnly,
                        Material lineMaterial, Material cornerMaterial, Material startCornerMaterial, Material endCornerMaterial,
                        Hologram middleLineHologram) {

        this(world, polygon, highestBlocksOnly,
                lineMaterial, cornerMaterial, startCornerMaterial, endCornerMaterial,
                null, middleLineHologram, null);
    }

    public HologramArea(World world, Polygon polygon, boolean highestBlocksOnly,
                        Material lineMaterial, Material cornerMaterial) {

        this(world, polygon, highestBlocksOnly,
                lineMaterial, cornerMaterial, cornerMaterial, cornerMaterial);
    }

    public void render() {
        if(!viewers.isEmpty()) {
            viewers.forEach(this::render);
            return;
        }

        if(hologramModifier != null) { hologramModifier.accept(null, this); }
        if(middleHologram != null) { middleHologram.render(); }
        lines.forEach(line -> {
            if(hologramLineModifier != null) { hologramLineModifier.accept(null, line); }
            line.render();
        });
    }

    public void render(Player player) {
        viewers.add(player);

        if(hologramModifier != null) { hologramModifier.accept(player, this); }
        if(middleHologram != null) { middleHologram.render(player); }
        lines.forEach(line -> {
            if(hologramLineModifier != null) { hologramLineModifier.accept(player, line); }
            line.render(player, true);
        });
    }

    public void delete() {
        Iterator<Player> iterator = viewers.iterator();
        while(iterator.hasNext()) {
            Player viewer = iterator.next();

            this.isUpdating = true;

            lines.forEach(line -> line.clear(viewer, true));

            if(middleHologram != null) {
                middleHologram.delete(viewer);
            }

            iterator.remove();

            this.isUpdating = false;

        }
    }

    public void delete(Player player) {

        this.isUpdating = true;

        lines.forEach(line -> line.clear(player, true));

        if(middleHologram != null) {
            middleHologram.delete(player);
        }

        viewers.remove(player);

        this.isUpdating = false;

    }

    public void updateShape(Polygon polygon) {

        this.isUpdating = true;

        clearLines();

        Location oldMiddle = getMiddleLocation();

        this.polygon = polygon;

        Location newMiddle = getMiddleLocation();

        HologramLine.updateLinesFromPolygon(this.lines, polygon);

        if(middleHologram != null) {
            Location offset = LocationUtil.offsetLocation(newMiddle, oldMiddle);
            middleHologram.setLocation(middleHologram.location.add(offset));
        }

        if(hologramModifier != null) { viewers.forEach(viewer -> hologramModifier.accept(viewer, this)); }

        drawLines();

        this.isUpdating = false;

    }

    public void clearLines() {
        viewers.forEach(viewer -> {
            lines.forEach(line -> {
                line.clear(viewer, false);
            });
        });
    }

    public void drawLines() {
        viewers.forEach(viewer -> {
            lines.forEach(line -> {
                if(hologramLineModifier != null) { hologramLineModifier.accept(viewer, line); }
                line.render(viewer, false);
            });
        });
    }

    public WrappedBlockData retainBlock(Player viewer, Location location, WrappedBlockData blockData) {

        if(!this.polygon.touches(GeometryUtils.bukkitLocationToJTSPoint(location))) { return null; }

        if(hologramModifier != null) { hologramModifier.accept(viewer, this); }

        for(HologramLine line : this.lines) {
            WrappedBlockData newBlockData = line.retain(location, blockData);
            if(newBlockData != null) { return newBlockData; }
        }

        return null;

    }

    public void drawBlock(Player viewer, Location location) {

        Point point = GeometryUtils.bukkitLocationToJTSPoint(location);
        if(!this.polygon.touches(point)) { return; }

        if(hologramModifier != null) { hologramModifier.accept(viewer, this); }

        if(GeometryUtils.isPointAVertexOfGeometry(this.polygon, point)) {
            this.lines.forEach(line -> {
                if(hologramLineModifier != null) { hologramLineModifier.accept(viewer, line); }
                line.drawCorners(viewer, startCornerMaterial, endCornerMaterial);
            });
        } else {
            this.lines.forEach(line -> {
                if(hologramLineModifier != null) { hologramLineModifier.accept(viewer, line); }
                line.drawLine(viewer, lineMaterial, startCornerMaterial, endCornerMaterial);
            });
        }

    }

    public void setMiddleHologram(Hologram middleHologram) {

        Location originalLocation = middleHologram.location;

        middleHologram.location = getMiddleLocation().add(originalLocation);

        this.middleHologram = middleHologram;

    }

    public Location getMiddleLocation() {

        Location location = GeometryUtils.JTSPointToBukkitLocation(world, polygon.getCentroid());
        location.setY(world.getHighestBlockYAt(location));
        location.add(0.5, 0, 0.5);

        return location;

    }

}
