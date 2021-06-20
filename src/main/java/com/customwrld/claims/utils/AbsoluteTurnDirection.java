package com.customwrld.claims.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.BlockFace;
import org.locationtech.jts.geom.Coordinate;

@AllArgsConstructor
public enum AbsoluteTurnDirection {
    SOUTH_WEST(true, true, new Coordinate(0, 0), BlockFace.SOUTH, BlockFace.WEST),
    WEST_NORTH(true, false, new Coordinate(1, 0), BlockFace.WEST, BlockFace.NORTH),
    NORTH_EAST(true, false, new Coordinate(1, 1), BlockFace.NORTH, BlockFace.EAST),
    EAST_SOUTH(true, true, new Coordinate(0, 1), BlockFace.EAST, BlockFace.SOUTH),
    NORTH_WEST(false, false, new Coordinate(1, 0), BlockFace.NORTH, BlockFace.WEST),
    WEST_SOUTH(false, false, new Coordinate(0, 0), BlockFace.WEST, BlockFace.SOUTH),
    SOUTH_EAST(false, true, new Coordinate(0, 1), BlockFace.SOUTH, BlockFace.EAST),
    EAST_NORTH(false, true, new Coordinate(1, 1), BlockFace.EAST, BlockFace.NORTH),

    STRAIGHT(true, false, new Coordinate(1, 1), BlockFace.NORTH, BlockFace.NORTH);

    @Getter private boolean isVertexConcave;
    @Getter private boolean isNextFragmentInsideBlock;

    @Getter private Coordinate offsetToFullBlock;

    @Getter private BlockFace beforeForwardFace;
    @Getter private BlockFace afterForwardFace;
}
