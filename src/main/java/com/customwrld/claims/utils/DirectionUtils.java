package com.customwrld.claims.utils;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.block.BlockFace;
import org.locationtech.jts.geom.Coordinate;

public class DirectionUtils {

    //TODO Allow for rotations smaller than 90deg, use trigonometry and account for axis combinations other than just X and Z
    public static BlockFace getFaceAfterRotation(BlockFace currentFace, Coordinate pivot, Coordinate before, Coordinate after) {
        if(before.getX() == after.getX() || before.getZ() == after.getZ()) { return currentFace; }

        if(before.getX() == pivot.getX()) { //Vertical -> Horizontal

            //Clockwise Rotations
            //S -> W
            if(before.getZ() > pivot.getZ() && before.getX() > after.getX()) {
                return rotate(4, currentFace);
            }

            //N -> E
            if(before.getZ() < pivot.getZ() && before.getX() < after.getX()) {
                return rotate(4, currentFace);
            }

            //Counterclockwise Rotations
            //N -> W
            if(before.getZ() < pivot.getZ() && before.getX() > after.getX()) {
                return rotate(-4, currentFace);
            }

            //S -> E
            if(before.getZ() > pivot.getZ() && before.getX() < after.getX()) {
                return rotate(-4, currentFace);
            }
        } else if(before.getZ() == pivot.getZ()) { //Horizontal -> Vertical

            //Clockwise Rotations
            //W -> N
            if(before.getX() > pivot.getX() && before.getZ() < after.getZ()) {
                return rotate(4, currentFace);
            }

            //E -> S
            if(before.getX() < pivot.getX() && before.getZ() > after.getZ()) {
                return rotate(4, currentFace);
            }

            //Counterclockwise Rotations
            //E -> N
            if(before.getX() < pivot.getX() && before.getZ() < after.getZ()) {
                return rotate(-4, currentFace);
            }

            //W -> S
            if(before.getX() > pivot.getX() && before.getZ() > after.getZ()) {
                return rotate(-4, currentFace);
            }
        }

        return currentFace;
    }

    /***
     * 0 - No change
     * <br>1 - 22.5deg
     * <br>2 - 45deg
     * <br>4 - 90deg
     * <br>8 - 180deg
     * <br>
     * <br>Negative values rotate the {@link BlockFace} counterclockwise.
     *
     * @param amount How many times it should rotate the {@link BlockFace} by 22.5 degrees.
     */
    public static BlockFace rotate(int amount, BlockFace blockFace) {
        if(amount > 0) {
            for (int i = amount; i != 0; i--) {
                blockFace = rotate(blockFace);
            }
        } else if (amount < 0) {
            for (int i = amount; i != 0; i++) {
                blockFace = rotateCCW(blockFace);
            }
        }
        return blockFace;
    }

    //TODO Surely there is a better way?
    private static BlockFace rotate(BlockFace face) {
        return switch (face) {
            case NORTH              -> BlockFace.NORTH_NORTH_EAST;  //North -> East
            case NORTH_NORTH_EAST   -> BlockFace.NORTH_EAST;
            case NORTH_EAST         -> BlockFace.EAST_NORTH_EAST;
            case EAST_NORTH_EAST    -> BlockFace.EAST;
            case EAST               -> BlockFace.EAST_SOUTH_EAST;   //East -> South
            case EAST_SOUTH_EAST    -> BlockFace.SOUTH_EAST;
            case SOUTH_EAST         -> BlockFace.SOUTH_SOUTH_EAST;
            case SOUTH_SOUTH_EAST   -> BlockFace.SOUTH;
            case SOUTH              -> BlockFace.SOUTH_SOUTH_WEST;  //South -> West
            case SOUTH_SOUTH_WEST   -> BlockFace.SOUTH_WEST;
            case SOUTH_WEST         -> BlockFace.WEST_SOUTH_WEST;
            case WEST_SOUTH_WEST    -> BlockFace.WEST;
            case WEST               -> BlockFace.WEST_NORTH_WEST;   //West -> North
            case WEST_NORTH_WEST    -> BlockFace.NORTH_WEST;
            case NORTH_WEST         -> BlockFace.NORTH_NORTH_WEST;
            case NORTH_NORTH_WEST   -> BlockFace.NORTH;

            case UP, DOWN, SELF -> null;
        };
    }

    private static BlockFace rotateCCW(BlockFace face) {
        return switch (face) {
            case NORTH              -> BlockFace.NORTH_NORTH_WEST;  //North -> West
            case NORTH_NORTH_WEST   -> BlockFace.NORTH_WEST;
            case NORTH_WEST         -> BlockFace.WEST_NORTH_WEST;
            case WEST_NORTH_WEST    -> BlockFace.WEST;
            case WEST               -> BlockFace.WEST_SOUTH_WEST;   //West -> South
            case WEST_SOUTH_WEST    -> BlockFace.SOUTH_WEST;
            case SOUTH_WEST         -> BlockFace.SOUTH_SOUTH_WEST;
            case SOUTH_SOUTH_WEST   -> BlockFace.SOUTH;
            case SOUTH              -> BlockFace.SOUTH_SOUTH_EAST;  //South -> East
            case SOUTH_SOUTH_EAST   -> BlockFace.SOUTH_EAST;
            case SOUTH_EAST         -> BlockFace.EAST_SOUTH_EAST;
            case EAST_SOUTH_EAST    -> BlockFace.EAST;
            case EAST               -> BlockFace.EAST_NORTH_EAST;   //East -> North
            case EAST_NORTH_EAST    -> BlockFace.NORTH_EAST;
            case NORTH_EAST         -> BlockFace.NORTH_NORTH_EAST;
            case NORTH_NORTH_EAST   -> BlockFace.NORTH;

            case UP, DOWN, SELF -> null;
        };
    }

}
