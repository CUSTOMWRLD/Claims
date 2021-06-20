package com.customwrld.claims.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GeometryUtils {

    public static boolean isPointAVertexOfGeometry(Geometry geometry, Point point) {
        if(!geometry.touches(point)) { return false; } //The point needs to be either on border or in the corner

        for(Coordinate coordinate : geometry.getCoordinates()) {
            if(coordinate.equals(point.getCoordinate())) { return true; }
        }

        return false;
    }

    public static Polygon createSquare(Coordinate first, Coordinate second) {
        return new GeometryFactory().createPolygon(new Coordinate[]{
                first,
                new Coordinate(first.x, second.y),
                second,
                new Coordinate(second.x, first.y),
                first
        });
    }

    public static Polygon dissolvePoints(Geometry polygon) {
        List<Coordinate> coordinates = new ArrayList<>(Arrays.asList(polygon.getCoordinates()));
        for(int i = 1; i < coordinates.size(); i++) {
            if(isVertex(coordinates, i)) {
                coordinates.add(coordinates.get(i));
                break;
            }
        }
        boolean isOriginOnEdge = false;
        for (int i = 0; i < coordinates.size(); i++) {
            if (i + 2 < coordinates.size()) {
                Coordinate current = coordinates.get(i);
                Coordinate next = coordinates.get(i + 1);
                Coordinate last = coordinates.get(i + 2);
                if ((current.getX() != next.getX() && current.getY() != next.getY()) || (next.getX() != last.getX() && next.getY() != last.getY())) {
                    return null; //Weird shit happening, this is a diagonal line??
                }

                if ((current.getX() == next.getX() && next.getX() == last.getX()) || (current.getY() == next.getY() && next.getY() == last.getY())) {
                    coordinates.remove(i + 1);
                    if (i + 2 >= coordinates.size()) { //Origin vertex was lying on an edge
                        coordinates.remove(0);
                        isOriginOnEdge = true;
                        break;
                    }
                    i--;
                }
            }
        }
        if (!isOriginOnEdge) { //Origin vertex wasn't lying on an edge
            coordinates.remove(coordinates.size() - 1);
        }
        Coordinate[] l = coordinates.toArray(new Coordinate[0]);
        return new GeometryFactory().createPolygon(l);
    }

    public static boolean isVertex(List<Coordinate> coordinates, int coordinateId) {
        Coordinate coordinate = coordinates.get(coordinateId);

        Coordinate prev;
        Coordinate next;

        if(coordinateId == 0) {
            prev = coordinates.get(coordinates.size() - 1);
            next = coordinates.get(coordinateId + 1);
        } else if(coordinateId == coordinates.size() - 1) {
            prev = coordinates.get(coordinateId - 1);
            next = coordinates.get(0);
        } else {
            prev = coordinates.get(coordinateId - 1);
            next = coordinates.get(coordinateId + 1);
        }

        return !((prev.getX() == coordinate.getX() && coordinate.getX() == next.getX()) || (prev.getY() == coordinate.getY() && coordinate.getY() == next.getY()));
    }

    //TODO Broken
    public static List<Coordinate> getGridCoordinatesInPolygon(Polygon polygon, int gridSize) {
        var envelope = polygon.getEnvelopeInternal();

        double minX = envelope.getMinX();
        double minY = envelope.getMinY();

        double maxX = envelope.getMaxX();
        double maxY = envelope.getMaxY();

        List<Coordinate> coordinates = new ArrayList<>();

        var factory = new GeometryFactory();
        for(var x = minX; x < maxX; x += Math.min(gridSize, maxX)) {
            for(var y = minY; y < maxY; y += Math.min(gridSize, maxY)) {
                var coordinate = new Coordinate(x, y);
                if(polygon.covers(factory.createPoint(coordinate))) {
                    coordinates.add(coordinate);
                }
            }
        }

        forEachBlock(polygon.getCoordinates(), 16, coordinates::add);

        return coordinates;
    }

    //TODO Doesn't preserve current block. Even if the next block is going to be outside of the current segment,
    // the next block will be on the start of the next segment when it should be somewhere along it.
    public static void forEachBlock(Coordinate[] coordinates, int spacing, Consumer<Coordinate> action) {
        for(var i = 0; i < coordinates.length - 1; i++) {
            Coordinate current = coordinates[i];
            Coordinate next = coordinates[i + 1];

            boolean direction = current.x != next.x;  //Changes on what direction (true - x ; false - z)
            boolean ascending = (direction && current.x < next.x) || (!direction && current.y < next.y);

            double origin = direction ? current.x : current.y;
            double end = direction ? next.x : next.y;

            for(var j = origin; (ascending && j < end) || (!ascending && j > end);) {
                action.accept(new Coordinate(direction ? j : current.x, !direction ? j : current.y));

                if(ascending) { j += spacing; } else { j -= spacing; }
            }
        }
    }

    public static Polygon getAccuratePolygon(Polygon polygon) {
        Coordinate[] prevCoordinates = polygon.getCoordinates();
        Coordinate[] coordinates = new Coordinate[prevCoordinates.length];

        Coordinate firstVertex = prevCoordinates[0];

        Coordinate prevVertex = prevCoordinates[prevCoordinates.length - 2];
        Coordinate nextVertex = prevCoordinates[1];

        AbsoluteTurnDirection turnDirection;

        BlockFace outwardsDirection;
        boolean isInside;

        Coordinate previous = prevVertex;
        for(var i = 0; i < prevCoordinates.length - 1; i++) {
            turnDirection = getTurnDirection(prevCoordinates[i], previous, prevCoordinates[i + 1]);

            Coordinate current = prevCoordinates[i].copy();
            Coordinate next = prevCoordinates[i + 1].copy();

            Coordinate fullBlockOffset = turnDirection.getOffsetToFullBlock();

            current.setX(current.getX() + fullBlockOffset.getX());
            current.setY(current.getY() + fullBlockOffset.getY());

            coordinates[i] = current;
            coordinates[i + 1] = next;

            previous = prevCoordinates[i];
        }

        turnDirection = getTurnDirection(prevCoordinates[prevCoordinates.length - 1], prevCoordinates[prevCoordinates.length - 2], prevCoordinates[1]);
        Coordinate last = prevCoordinates[prevCoordinates.length - 1].copy();

        Coordinate fullBlockOffset = turnDirection.getOffsetToFullBlock();

        last.setX(last.getX() + fullBlockOffset.getX());
        last.setY(last.getY() + fullBlockOffset.getY());

        coordinates[coordinates.length - 1] = last;

        return new GeometryFactory().createPolygon(coordinates);
    }

    public static boolean isNextFragmentInsideBlock(AbsoluteTurnDirection turnDirection) {
        return switch(turnDirection) {
            case SOUTH_WEST, EAST_SOUTH, EAST_NORTH, SOUTH_EAST -> true;
            case WEST_NORTH, NORTH_EAST, NORTH_WEST, WEST_SOUTH -> false;

            case STRAIGHT -> true;
        };
    }

    public static boolean isVertexConcave(AbsoluteTurnDirection turnDirection) {
        return turnDirection.isVertexConcave();
    }

    public static AbsoluteTurnDirection getTurnDirection(Coordinate pivot, Coordinate before, Coordinate after) {
        if(before.getX() == after.getX() || before.getY() == after.getY()) { return AbsoluteTurnDirection.STRAIGHT; }

        if(before.getX() == pivot.getX()) { //Vertical -> Horizontal

            //Clockwise Rotations
            //S -> W (Concave -> Convex)
            if(before.getY() < pivot.getY() && pivot.getX() > after.getX()) {
                return AbsoluteTurnDirection.SOUTH_WEST;
            }

            //N -> E (Convex -> Concave)
            if(before.getY() > pivot.getY() && pivot.getX() < after.getX()) {
                return AbsoluteTurnDirection.NORTH_EAST;
            }

            //Counterclockwise Rotations
            //N -> W (Stays Concave)
            if(before.getY() > pivot.getY() && pivot.getX() > after.getX()) {
                return AbsoluteTurnDirection.NORTH_WEST;
            }

            //S -> E (Stays Convex)
            if(before.getY() < pivot.getY() && pivot.getX() < after.getX()) {
                return AbsoluteTurnDirection.SOUTH_EAST;
            }
        } else if(before.getY() == pivot.getY()) { //Horizontal -> Vertical

            //Clockwise Rotations
            //W -> N (Stays Convex)
            if(before.getX() > pivot.getX() && pivot.getY() > after.getY()) {
                return AbsoluteTurnDirection.WEST_NORTH;
            }

            //E -> S (Stays Concave)
            if(before.getX() < pivot.getX() && pivot.getY() < after.getY()) {
                return AbsoluteTurnDirection.EAST_SOUTH;
            }

            //Counterclockwise Rotations
            //E -> N (Convex -> Concave)
            if(before.getX() < pivot.getX() && pivot.getY() > after.getY()) {
                return AbsoluteTurnDirection.EAST_NORTH;
            }

            //W -> S (Concave -> Convex)
            if(before.getX() > pivot.getX() && pivot.getY() < after.getY()) {
                return AbsoluteTurnDirection.WEST_SOUTH;
            }
        }

        return AbsoluteTurnDirection.STRAIGHT;
    }

    public static Coordinate bukkitLocationToJTSCoordinate(Location location) {
        return new Coordinate(location.getX(), location.getZ(), location.getY());
    }

    public static Coordinate bukkitBlockLocationToJTSCoordinate(Location location) {
        return new Coordinate(location.getBlockX(), location.getBlockZ(), location.getBlockY());
    }

    public static Location JTSCoordinateToBukkitLocation(World world, Coordinate coordinate) {
        double x = coordinate.x;
        x = !Double.isNaN(x) ? x : 0;
        double y = coordinate.y;
        y = !Double.isNaN(y) ? y : 0;
        double z = coordinate.z;
        z = !Double.isNaN(z) ? z : 0;
        return new Location(world, x, z, y);
    }

    public static Point bukkitLocationToJTSPoint(Location location) {
        return new GeometryFactory().createPoint(bukkitLocationToJTSCoordinate(location));
    }

    public static Location JTSPointToBukkitLocation(World world, Point point) {
        return new Location(world, point.getX(), 0, point.getY());
    }

    public static Coordinate lerp(Coordinate from, Coordinate to, double t) {
        Coordinate interpolated = from.copy();

        double minX = Math.min(from.getX(), to.getX());
        double maxX = Math.max(from.getX(), to.getX());

        double minY = Math.min(from.getY(), to.getY());
        double maxY = Math.max(from.getY(), to.getY());

        double minZ = Math.min(from.getZ(), to.getZ());
        double maxZ = Math.max(from.getZ(), to.getZ());

        interpolated.setX(minX + ((maxX - minX) * t));
        interpolated.setY(minY + ((maxY - minY) * t));
        interpolated.setZ(minZ + ((maxZ - minZ) * t));

        return interpolated;
    }

    public static Point lerp(Point from, Point to, double t) {
        return new GeometryFactory().createPoint(lerp(from.getCoordinate(), to.getCoordinate(), t));
    }

    public static Coordinate add(Coordinate coordinate, double x, double y, double z) {
        coordinate.setX(coordinate.getX() + x);
        coordinate.setY(coordinate.getY() + y);
        coordinate.setZ(coordinate.getZ() + z);
        return coordinate;
    }

    public static Coordinate subtract(Coordinate coordinate, double x, double y, double z) {
        add(coordinate, -x, -y, -z);
        return coordinate;
    }

    public static Point add(Point point, double x, double y) {
        add(point.getCoordinate(), x, y, 0);
        return point;
    }

    public static Point subtract(Point point, double x, double y) {
        add(point, -x, -y);
        return point;
    }

}
