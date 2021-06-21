package com.customwrld.claims.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.*;

/**
 * Wrapper for {@link Polygon} that adds some functionality to work in Spigot/Paper environment and suit different needs.
 */
public class UtilizedPolygon {

    @Getter @Setter private World world;
    @Getter @Setter private Polygon polygon;

    public UtilizedPolygon(World world, Polygon polygon) {
        this.world = world;
        this.polygon = polygon;
    }

    public static UtilizedPolygon fromCoordinates(World world, Coordinate[] coordinates) {
        return new UtilizedPolygon(world, new GeometryFactory().createPolygon(coordinates));
    }

    public static UtilizedPolygon asSquare(World world, Coordinate first, Coordinate second) {
        return new UtilizedPolygon(world, GeometryUtils.createSquare(first, second));
    }

    public static UtilizedPolygon asSquare(@Nullable World world, Location first, Location second) {
        world = world != null ? world : first.getWorld();

        var firstCoord = GeometryUtils.bukkitLocationToJTSCoordinate(first);
        var secondCoord = GeometryUtils.bukkitLocationToJTSCoordinate(second);

        return new UtilizedPolygon(world, GeometryUtils.createSquare(firstCoord, secondCoord));
    }

    public UtilizedPolygon reverse() {
        this.polygon = this.polygon.reverse();
        return this;
    }

    //Intersects / Is in bounds
    public boolean intersects(@NotNull Location loc) {
        if(!locationMatchesWorld(loc)) { return false; }

        var point = GeometryUtils.bukkitLocationToJTSPoint(loc);
        return this.polygon.intersects(point);
    }

    public boolean intersects(@Nullable World world, int x, int y) {
        if(!doWorldsMatch(world)) { return false; }

        var point = new GeometryFactory().createPoint(new Coordinate(x, y));
        return this.polygon.intersects(point);
    }

    public boolean intersects(@Nullable World world, Coordinate coordinate) {
        if(!doWorldsMatch(world)) { return false; }

        var point = new GeometryFactory().createPoint(coordinate);
        return this.polygon.intersects(point);
    }

    public boolean intersects(@Nullable World world, Geometry geometry) {
        if(!doWorldsMatch(world)) { return false; }
        return this.polygon.intersects(geometry);
    }

    public boolean intersects(UtilizedPolygon polygon) {
        var otherWorld = polygon.getWorld();
        var otherPolygon = polygon.getPolygon();

        return doWorldsMatch(otherWorld) && this.polygon.intersects(otherPolygon);
    }

    //Touches / Is on outline
    public boolean touches(@NotNull Location loc) {
        if(!locationMatchesWorld(loc)) { return false; }

        var point = GeometryUtils.bukkitLocationToJTSPoint(loc);
        return this.polygon.touches(point);
    }

    public boolean touches(@Nullable World world, int x, int y) {
        if(!doWorldsMatch(world)) { return false; }

        var point = new GeometryFactory().createPoint(new Coordinate(x, y));
        return this.polygon.touches(point);
    }

    public boolean touches(@Nullable World world, Coordinate coordinate) {
        if(!doWorldsMatch(world)) { return false; }

        var point = new GeometryFactory().createPoint(coordinate);
        return this.polygon.touches(point);
    }

    public boolean touches(@Nullable World world, Geometry geometry) {
        if(!doWorldsMatch(world)) { return false; }
        return this.polygon.touches(geometry);
    }

    public boolean touches(UtilizedPolygon polygon) {
        var otherWorld = polygon.getWorld();
        var otherPolygon = polygon.getPolygon();

        return doWorldsMatch(otherWorld) && this.polygon.touches(otherPolygon);
    }

    //Covers
    public boolean covers(@NotNull Location loc) {
        if(!locationMatchesWorld(loc)) { return false; }

        var point = GeometryUtils.bukkitLocationToJTSPoint(loc);
        return this.polygon.covers(point);
    }

    public boolean covers(@Nullable World world, int x, int y) {
        if(!doWorldsMatch(world)) { return false; }

        var point = new GeometryFactory().createPoint(new Coordinate(x, y));
        return this.polygon.covers(point);
    }

    public boolean covers(@Nullable World world, Coordinate coordinate) {
        if(!doWorldsMatch(world)) { return false; }

        var point = new GeometryFactory().createPoint(coordinate);
        return this.polygon.covers(point);
    }

    public boolean covers(@Nullable World world, Geometry geometry) {
        if(!doWorldsMatch(world)) { return false; }
        return this.polygon.covers(geometry);
    }

    public boolean covers(UtilizedPolygon polygon) {
        var otherWorld = polygon.getWorld();
        var otherPolygon = polygon.getPolygon();

        return doWorldsMatch(otherWorld) && this.polygon.covers(otherPolygon);
    }

    //Union / Join / Merge
    public UtilizedPolygon union(@NotNull World world, Geometry geometry) {
        if(!doWorldsMatch(world)) { return null; }
        this.polygon = new GeometryFactory().createPolygon(this.polygon.union(geometry).getCoordinates());
        return this;
    }

    public UtilizedPolygon union(UtilizedPolygon polygon) {
        var otherWorld = polygon.getWorld();
        var otherPolygon = polygon.getPolygon();

        if(!doWorldsMatch(otherWorld)) { return null; }

        this.polygon = new GeometryFactory().createPolygon(this.polygon.union(otherPolygon).getCoordinates());

        return this;
    }

    //Difference / Cut / Trim
    public UtilizedPolygon difference(@NotNull World world, Geometry geometry) {
        if(!doWorldsMatch(world)) { return null; }

        this.polygon = new GeometryFactory().createPolygon(this.polygon.difference(geometry).getCoordinates());

        return this;
    }

    public UtilizedPolygon difference(UtilizedPolygon polygon) {
        var otherWorld = polygon.getWorld();
        var otherPolygon = polygon.getPolygon();

        if(!doWorldsMatch(otherWorld)) { return null; }

        this.polygon = new GeometryFactory().createPolygon(this.polygon.difference(otherPolygon).getCoordinates());

        return this;
    }

    public boolean isHighestInBounds(Location loc) {
        return locationMatchesWorld(loc) && intersects(loc) && loc.toHighestLocation().equals(loc);
    }

    public boolean isHighestOnOutline(Location loc) {
        return locationMatchesWorld(loc) && touches(loc) && world.getHighestBlockYAt(loc) == loc.getBlockY();
    }

    public UtilizedPolygon toAccurate() {
        Coordinate[] prevCoordinates = this.polygon.getCoordinates();
        Coordinate[] coordinates = new Coordinate[prevCoordinates.length];

        Coordinate prevVertex = prevCoordinates[prevCoordinates.length - 2];

        AbsoluteTurnDirection turnDirection;

        Coordinate previous = prevVertex;
        for(var i = 0; i < prevCoordinates.length - 1; i++) {
            turnDirection = GeometryUtils.getTurnDirection(prevCoordinates[i], previous, prevCoordinates[i + 1]);

            Coordinate current = prevCoordinates[i].copy();
            Coordinate next = prevCoordinates[i + 1].copy();

            Coordinate fullBlockOffset = turnDirection.getOffsetToFullBlock();

            current.setX(current.getX() + fullBlockOffset.getX());
            current.setY(current.getY() + fullBlockOffset.getY());

            coordinates[i] = current;
            coordinates[i + 1] = next;

            previous = prevCoordinates[i];
        }

        turnDirection = GeometryUtils.getTurnDirection(
                prevCoordinates[prevCoordinates.length - 1],
                prevCoordinates[prevCoordinates.length - 2],
                prevCoordinates[1]);

        Coordinate last = prevCoordinates[prevCoordinates.length - 1].copy();

        Coordinate fullBlockOffset = turnDirection.getOffsetToFullBlock();

        last.setX(last.getX() + fullBlockOffset.getX());
        last.setY(last.getY() + fullBlockOffset.getY());

        coordinates[coordinates.length - 1] = last;

        return new UtilizedPolygon(this.world, new GeometryFactory().createPolygon(coordinates));
    }

    public boolean locationMatchesWorld(Location loc) {
        return doWorldsMatch(loc.getWorld());
    }

    public boolean doWorldsMatch(@Nullable World otherWorld) {
        if(this.world == null || otherWorld == null) {
            return true;
        }

        var worldUID = this.world.getUID();
        var otherWorldUID = otherWorld.getUID();

        return worldUID.equals(otherWorldUID);
    }

    public boolean isEmpty() {
        return this.polygon.isEmpty();
    }

    public double getArea() {
        return this.polygon.getArea();
    }

    public Point getCentroid() {
        return this.polygon.getCentroid();
    }

    public Coordinate[] getCoordinates() {
        return this.polygon.getCoordinates();
    }

}
