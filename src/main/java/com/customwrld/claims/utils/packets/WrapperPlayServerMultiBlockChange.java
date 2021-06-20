package com.customwrld.claims.utils.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;

public class WrapperPlayServerMultiBlockChange extends AbstractPacket {
    public static final PacketType TYPE =
            PacketType.Play.Server.MULTI_BLOCK_CHANGE;

    public WrapperPlayServerMultiBlockChange() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerMultiBlockChange(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition[] getRelativePositions() {
        short[] positions = handle.getShortArrays().read(0);
        BlockPosition[] wrappedPositions = new BlockPosition[positions.length];
        for(int i = 0; i < positions.length; i++) {
            short position = positions[i];
            byte x = (byte) (position & 0xF00 >> 8);
            byte z = (byte) (position & 0xF0 >> 4);
            byte y = (byte) (position & 0xF);
            wrappedPositions[i] = new BlockPosition(x, y, z);
        }
        return wrappedPositions;
    }

    public void setRelativePositions(BlockPosition[] wrappedPositions) {
        short[] positions = new short[wrappedPositions.length];
        for(int i = 0; i < wrappedPositions.length; i++) {
            BlockPosition position = wrappedPositions[i];
            positions[i] = (short) (position.getX() << 8 | position.getZ() << 4 | position.getY());
        }
        handle.getShortArrays().write(0, positions);
    }

    /**
     * Retrieve the chunk that has been altered.
     *
     * @return The current chunk
     */
    public BlockPosition getChunk() {
        return handle.getSectionPositions().read(0);
    }

    /**
     * Set the chunk that has been altered.
     *
     * @param value - new value
     */
    public void setChunk(BlockPosition value) {
        handle.getSectionPositions().write(0, value);
    }

    /**
     * Retrieve a copy of the record data as a block change array.
     *
     * @return The copied block change array.
     */
    public WrappedBlockData[] getBlockData() {
        return handle.getBlockDataArrays().read(0);
    }

    /**
     * Set the record data using the given helper array.
     *
     * @param value - new value
     */
    public void setBlockData(WrappedBlockData[] value) {
        handle.getBlockDataArrays().write(0, value);
    }
}