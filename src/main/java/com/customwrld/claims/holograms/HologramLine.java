package com.customwrld.claims.holograms;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.customwrld.claims.utils.GeometryUtils;
import com.customwrld.customlib.hologram.Hologram;
import com.customwrld.customlib.util.LocationUtil;
import lombok.*;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class HologramLine {

    @Getter private final int id;

    @Getter @Setter private boolean highestBlocksOnly;

    @Getter @Setter @NotNull private World world;

    @Getter @Setter @NotNull private Coordinate start;
    @Getter @Setter @NotNull private Coordinate end;

    @Getter @Setter @Nullable private Material startMaterial;
    @Getter @Setter @Nullable private Material endMaterial;

    @Getter @Setter @Nullable private Material lineMaterial;

    @Getter @Setter @Nullable private Hologram startHologram;
    @Getter @Setter @Nullable private Hologram endHologram;

    @Getter @Setter @Nullable private Hologram middleHologram;

    //Precalculated (Recalculate only when changing coordinates instead of every draw call)
    private Axis axis;
    private boolean ascending;

    private int originPos;
    private int endPos;

    public HologramLine(int id, boolean highestBlocksOnly, @NotNull World world, @NotNull Coordinate start, @NotNull Coordinate end) {
        this.id = id;
        this.highestBlocksOnly = highestBlocksOnly;
        this.world = world;
        this.start = start;
        this.end = end;

        recalculate();
    }

    @Builder
    public HologramLine(int id,
                        boolean highestBlocksOnly,
                        @NotNull World world,
                        @NotNull Coordinate start,
                        @NotNull Coordinate end,

                        Material startMaterial, Material endMaterial, Material lineMaterial,

                        Hologram startHologram, Hologram middleHologram, Hologram endHologram) {

        this(id, highestBlocksOnly, world, start, end);

        this.startMaterial = startMaterial;
        this.endMaterial = endMaterial;

        this.lineMaterial = lineMaterial;

        if(startHologram != null) {
            this.startHologram = startHologram.clone();

            var originalLocation = this.startHologram.location;

            this.startHologram.location =
                    LocationUtil.toHighestLocationAccurate(
                            GeometryUtils.JTSCoordinateToBukkitLocation(world, start).add(originalLocation)
                    ).add(0, originalLocation.getY(), 0);
        }

        if(middleHologram != null) {
            this.middleHologram = middleHologram.clone();

            var originalLocation = this.middleHologram.location;

            this.middleHologram.location =
                    LocationUtil.toHighestLocationAccurate(
                            GeometryUtils.JTSCoordinateToBukkitLocation(world, GeometryUtils.lerp(start, end, 0.5)).add(originalLocation)
                    ).add(0, originalLocation.getY(), 0);
        }

        if(endHologram != null) {
            this.endHologram = endHologram.clone();

            var originalLocation = this.endHologram.location;
            this.endHologram.location =
                    LocationUtil.toHighestLocationAccurate(
                            GeometryUtils.JTSCoordinateToBukkitLocation(world, end).add(originalLocation)
                    ).add(0, originalLocation.getY(), 0);
        }

    }

    //TODO Maybe find a better way instead of having a ton of arguments?
    public static List<HologramLine> fromPolygon(@NotNull World world,
                                                 @NotNull Polygon polygon,

                                                 int startingId,
                                                 boolean highestBlocksOnly,

                                                 @Nullable Material startMaterial, @Nullable Material endMaterial, @Nullable Material lineMaterial,

                                                 @Nullable Hologram startHologram, @Nullable Hologram middleHologram, @Nullable Hologram endHologram) {

        List<HologramLine> lines = new ArrayList<>();

        Coordinate[] coordinates = polygon.getCoordinates();

        for(int i = 0; i < coordinates.length - 1; i++) {

            Coordinate current = coordinates[i];
            Coordinate next = coordinates[i + 1];

            lines.add(
                    new HologramLine(
                            startingId + i,
                            highestBlocksOnly,

                            world,

                            current,
                            next,

                            startMaterial, endMaterial, lineMaterial,

                            startHologram, middleHologram, endHologram)
            );

        }

        return lines;

    }

    public static void updateLinesFromPolygon(@NotNull List<HologramLine> lines,
                                              @NotNull Polygon newPolygon) {

        Coordinate[] coordinates = newPolygon.getCoordinates();

        lines.subList(coordinates.length - 1, lines.size()).clear();

        for(int i = 0; i < coordinates.length - 1; i++) {

            Coordinate current = coordinates[i];
            Coordinate next = coordinates[i + 1];

            HologramLine line = lines.get(i);

            World world = line.world;

            Location oldStartLocation = GeometryUtils.JTSCoordinateToBukkitLocation(world, line.start).toHighestLocation();
            Location newStartLocation = GeometryUtils.JTSCoordinateToBukkitLocation(world, current).toHighestLocation();

            Hologram startHologram = line.getStartHologram();
            if(startHologram != null) {
                Location offset = LocationUtil.offsetLocation(newStartLocation, oldStartLocation);
                startHologram.setLocation(startHologram.location.add(offset));
            }

            Location oldEndLocation = GeometryUtils.JTSCoordinateToBukkitLocation(world, line.end).toHighestLocation();
            Location newEndLocation = GeometryUtils.JTSCoordinateToBukkitLocation(world, next).toHighestLocation();

            Hologram endHologram = line.getEndHologram();
            if(endHologram != null) {
                Location offset = LocationUtil.offsetLocation(newEndLocation, oldEndLocation);
                endHologram.setLocation(endHologram.location.add(offset));
            }

            Hologram middleHologram = line.getMiddleHologram();
            if(middleHologram != null) {
                Location oldMiddle = LocationUtil.offsetLocation(oldEndLocation, oldStartLocation);
                Location newMiddle = LocationUtil.offsetLocation(newEndLocation, newStartLocation);

                Location offset = LocationUtil.offsetLocation(newMiddle, oldMiddle);

                middleHologram.setLocation(middleHologram.location.add(offset));
            }

            line.start = current;
            line.end = next;
            line.recalculate();

        }

    }

    public void render(Player player, boolean includeHolograms) {

        drawLine(player, lineMaterial, startMaterial, endMaterial);

        if(includeHolograms) {
            if (startHologram != null) { startHologram.render(player); }
            if (endHologram != null) { endHologram.render(player); }
            if (middleHologram != null) { middleHologram.render(player); }
        }

    }

    public void render() {
        if (startHologram != null) { startHologram.render(); }
        if (endHologram != null) { endHologram.render(); }
        if (middleHologram != null) { middleHologram.render(); }
    }

    public void clear(Player player, boolean removeHolograms) {

        drawLine(player, null, null, null);

        if(removeHolograms) {
            if (startHologram != null) { startHologram.delete(player); }
            if (endHologram != null) { endHologram.delete(player); }
            if (middleHologram != null) { middleHologram.delete(player); }
        }

    }

    public void drawLine(Player player, Material lineMaterial, Material startMaterial, Material endMaterial) {

        int startX = (int) start.getX();
        int startY = (int) start.getY();

        int endX = (int) end.getX();
        int endY = (int) end.getY();

        BlockData data = lineMaterial != null ? lineMaterial.createBlockData() : null;
        for (int i = originPos; (ascending && i < endPos) || (!ascending && i > endPos); ) {
            if (ascending) { i++; } else { i--; }

            int x = axis == Axis.X ? i : startX;
            int z = axis == Axis.Z ? i : startY;

            if(data != null) {
                player.sendBlockChange(world.getHighestBlockAt(x, z).getLocation(), data);
            } else {
                Block block = world.getHighestBlockAt(x, z);
                Location location = block.getLocation();
                player.sendBlockChange(location, block.getBlockData());
            }

        }

        drawCorners(player, startMaterial, endMaterial);

    }

    public void drawCorners(Player player, Material startMaterial, Material endMaterial) {

        int startX = (int) start.getX();
        int startY = (int) start.getY();

        int endX = (int) end.getX();
        int endY = (int) end.getY();

        if (startMaterial != null) {
            player.sendBlockChange(world.getHighestBlockAt(startX, startY).getLocation(), startMaterial.createBlockData());
        } else {
            Block block = world.getHighestBlockAt(startX, startY);
            Location otherLocation = block.getLocation();
            player.sendBlockChange(otherLocation, block.getBlockData());
        }

        if (endMaterial != null) {
            player.sendBlockChange(world.getHighestBlockAt(endX, endY).getLocation(), endMaterial.createBlockData());
        } else {
            Block block = world.getHighestBlockAt(endX, endY);
            Location otherLocation = block.getLocation();
            player.sendBlockChange(otherLocation, block.getBlockData());
        }

    }

    public WrappedBlockData retain(Location location, WrappedBlockData blockData) {

        if(!inBounds(location)) { return null; }

        if(!isOnCorner(location)) {
            blockData.setType(lineMaterial);
            return blockData;
        } else {
            return retainCorners(location, blockData);
        }

    }

    public WrappedBlockData retainCorners(Location location, WrappedBlockData blockData) {

        if(!isOnCorner(location)) { return null; }

        Coordinate coordinate = GeometryUtils.bukkitBlockLocationToJTSCoordinate(location);

        int startX = (int) start.getX();
        int startZ = (int) start.getY();

        int endX = (int) end.getX();
        int endZ = (int) end.getY();

        if(startMaterial != null && coordinate.equals(start)) {
            int highestY = world.getHighestBlockYAt(startX, startZ);
            if(location.getY() == highestY) {
                blockData.setType(startMaterial);
                return blockData;
            }
        }

        if(endMaterial != null && coordinate.equals(end)) {
            int highestY = world.getHighestBlockYAt(endX, endZ);
            if(location.getY() == highestY) {
                blockData.setType(endMaterial);
                return blockData;
            }
        }

        return null;

    }

    public boolean isOnCorner(Location location) {

        int startX = (int) start.getX();
        int startZ = (int) start.getY();

        int endX = (int) end.getX();
        int endZ = (int) end.getY();

        return ((location.getZ() >= startZ && location.getZ() < startZ + 1) && (location.getX() >= startX && location.getX() < startX + 1)) ||
                ((location.getZ() >= endZ && location.getZ() < endZ + 1) && (location.getX() >= endX && location.getX() < endX + 1));

    }

    public boolean inBounds(Location location) {

        int startX = (int) start.getX();
        int startZ = (int) start.getY();

        int endX = (int) end.getX();
        int endZ = (int) end.getY();

        return ( axis == Axis.X &&
                        (location.getZ() >= startZ && location.getZ() < startZ + 1) &&
                        ( ascending ?
                                (location.getX() >= startX && location.getX() < endX + 1) :
                                (location.getX() < startX + 1 && location.getX() >= endX) ) ) ||
                ( axis == Axis.Z &&
                        (location.getX() >= startX && location.getX() < startX + 1) &&
                        ( ascending ?
                                (location.getZ() >= startZ && location.getZ() < endZ + 1) :
                                (location.getZ() < startZ + 1 && location.getZ() >= endZ) ) );

    }

    public int getLength() {
        int startX = (int) start.getX();
        int startZ = (int) start.getY();

        int endX = (int) end.getX();
        int endZ = (int) end.getY();

        return axis == Axis.X ? Math.abs(endX - startX) : Math.abs(endZ - startZ);
    }

    private void recalculate() {

        int currentX = (int) start.getX();
        int currentZ = (int) start.getY();

        int nextX = (int) end.getX();
        int nextZ = (int) end.getY();

        this.axis = currentX != nextX ? Axis.X : Axis.Z;
        this.ascending = (axis == Axis.X && currentX < nextX) || currentZ < nextZ;

        this.originPos = axis == Axis.X ? currentX : currentZ;
        this.endPos = axis == Axis.X ? nextX : nextZ;

    }

}
