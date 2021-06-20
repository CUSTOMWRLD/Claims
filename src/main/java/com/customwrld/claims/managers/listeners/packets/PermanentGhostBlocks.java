package com.customwrld.claims.managers.listeners.packets;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.customwrld.claims.ClaimingSession;
import com.customwrld.claims.Claims;
import com.customwrld.claims.managers.SessionsManager;
import com.customwrld.claims.utils.packets.WrapperPlayServerBlockChange;
import com.customwrld.claims.utils.packets.WrapperPlayServerExplosion;
import com.customwrld.claims.utils.packets.WrapperPlayServerMapChunk;
import com.customwrld.claims.utils.packets.WrapperPlayServerMultiBlockChange;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class PermanentGhostBlocks extends PacketAdapter {
    public PermanentGhostBlocks() {
        super(Claims.get(), ListenerPriority.HIGHEST,
                PacketType.Play.Server.BLOCK_CHANGE,
                PacketType.Play.Server.MULTI_BLOCK_CHANGE,
                PacketType.Play.Server.EXPLOSION,
                PacketType.Play.Server.MAP_CHUNK);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketType packetType = event.getPacket().getType();
        if (packetType == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(event.getPacket());
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            for (ClaimingSession session : SessionsManager.getClaimingSessions()) {
                Player owner = session.getOwner();
                var ownerUUID = owner.getUniqueId();
                if(!ownerUUID.equals(uuid)) { continue; }

                if(!session.isSelected()) { continue; }
                if(session.getHologramArea() == null || session.getHologramArea().isUpdating()) { continue; }

                Location loc = packet.getBukkitLocation(player.getWorld());
                if(session.isInHighestOnOutline(loc)) {
                    WrappedBlockData retainedBlock = session.getHologramArea().retainBlock(owner, loc, packet.getBlockData().deepClone());
                    if(retainedBlock != null) { packet.setBlockData(retainedBlock); }
                }
            }
        } else if (packetType == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(event.getPacket());
            BlockPosition[] blockPositions = packet.getRelativePositions();
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (ClaimingSession session : SessionsManager.getClaimingSessions()) {
                        if (!session.isSelected()) { continue; }

                        Player owner = session.getOwner();
                        UUID ownerUUID = owner.getUniqueId();

                        for (BlockPosition blockPosition : blockPositions) {
                            Location location = blockPosition.toLocation(player.getWorld());
                            if (ownerUUID.equals(uuid) && session.isSelected() && !session.getHologramArea().isUpdating()) {
                                if(session.isInHighestOnOutline(location)) {
                                    session.getHologramArea().drawBlock(player, location);
                                }
                            }
                        //if(session.isInHighestOnOutline(loc)) { session.getHologramArea().retainBlock(player, packet); }
//                                PlotCornerType corner = session.isInHighestOnCorner(loc);
//                                if (corner != null) {
//                                    player.sendBlockChange(loc, corner.getMaterial().createBlockData());
//                                } else if (session.isInHighestOnOutline(loc)) {
//                                    player.sendBlockChange(loc, Material.YELLOW_CONCRETE.createBlockData());
//                                }
//                                System.out.println("V");
//                                session.createHolograms(null, null);
                        }
                    }
                }
            }.runTaskLater(Claims.get(), 2);
        } else if (packetType == PacketType.Play.Server.EXPLOSION) {
            WrapperPlayServerExplosion packet = new WrapperPlayServerExplosion(event.getPacket());
            List<BlockPosition> blockPositions = packet.getRecords();
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (ClaimingSession session : SessionsManager.getClaimingSessions()) {
                        if (!session.isSelected()) { continue; }

                        Player owner = session.getOwner();
                        UUID ownerUUID = owner.getUniqueId();

                        for (BlockPosition blockPosition : blockPositions) {
                            Location location = blockPosition.toLocation(player.getWorld());
                            if (ownerUUID.equals(uuid) && session.isSelected() && !session.getHologramArea().isUpdating()) {
                                if(session.isInHighestOnOutline(location)) {
                                    session.getHologramArea().drawBlock(player, location);
                                }
                            }
                            //if(session.isInHighestOnOutline(loc)) { session.getHologramArea().retainBlock(player, packet); }
//                                PlotCornerType corner = session.isInHighestOnCorner(loc);
//                                if (corner != null) {
//                                    player.sendBlockChange(loc, corner.getMaterial().createBlockData());
//                                } else if (session.isInHighestOnOutline(loc)) {
//                                    player.sendBlockChange(loc, Material.YELLOW_CONCRETE.createBlockData());
//                                }
//                                System.out.println("V");
//                                session.createHolograms(null, null);
                        }
                    }
                }
            }.runTaskLater(Claims.get(), 2);
        } else if (packetType == PacketType.Play.Server.MAP_CHUNK) {
            WrapperPlayServerMapChunk packet = new WrapperPlayServerMapChunk(event.getPacket());
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            int x = packet.getChunkX();
            int z = packet.getChunkZ();
            for (ClaimingSession session : SessionsManager.getClaimingSessions()) {
                if (session.isSelected()) {
                    PaperLib.getChunkAtAsync(session.getFirstCorner()).thenAccept(firstChunk -> {
                        PaperLib.getChunkAtAsync(session.getSecondCorner()).thenAccept(secondChunk -> {
                            boolean isValid = false;
                            //X
                            if (z == firstChunk.getZ() || z == secondChunk.getZ()) {
                                if (firstChunk.getX() < secondChunk.getX() && (x >= firstChunk.getX() && x <= secondChunk.getX())) {
                                    isValid = true;
                                } else if (firstChunk.getX() > secondChunk.getX() && (x >= secondChunk.getX() && x <= firstChunk.getX())) {
                                    isValid = true;
                                } else if (firstChunk.getX() == secondChunk.getX() && (x == firstChunk.getX())) {
                                    isValid = true;
                                }
                            }

                            //Z
                            if (!isValid) {
                                if (x == firstChunk.getX() || x == secondChunk.getX()) {
                                    if (firstChunk.getZ() < secondChunk.getZ() && (x >= firstChunk.getZ() && x <= secondChunk.getZ())) {
                                        isValid = true;
                                    } else if (firstChunk.getZ() > secondChunk.getZ() && (x >= secondChunk.getZ() && x <= firstChunk.getZ())) {
                                        isValid = true;
                                    } else if (firstChunk.getZ() == secondChunk.getZ() && (x == firstChunk.getZ())) {
                                        isValid = true;
                                    }
                                }
                            }

                            if (isValid) {
                                //session.clearOutline();
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        //session.drawOutline();
                                        //session.drawCorners(false, true);
                                        //System.out.println("N");
                                        session.getHologramArea().drawBlock(player, player.getLocation());
                                        //session.createHolograms(null, null);
                                    }
                                }.runTaskLater(Claims.get(), 0);
                            }
                        });
                    });
                }
            }
        }
    }
}