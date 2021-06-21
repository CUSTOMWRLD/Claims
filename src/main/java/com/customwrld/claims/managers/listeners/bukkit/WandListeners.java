package com.customwrld.claims.managers.listeners.bukkit;

import com.customwrld.claims.*;
import com.customwrld.claims.managers.SessionsManager;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

//Prevents wands from being interacted with in-game.
public class WandListeners {
    public static class WandEvents implements Listener {
        @EventHandler
        public void disableItemDamage(PlayerItemDamageEvent event) {
            Player player = event.getPlayer();
            if (SessionsManager.isClaiming(player)) { event.setCancelled(true); }
        }

        @EventHandler
        public void disableItemBreak(PlayerItemBreakEvent event) {
            Player player = event.getPlayer();
            if (SessionsManager.isClaiming(player)) { event.getBrokenItem().setAmount(1); }
        }

        @EventHandler
        public void disableEnchantingTable(PrepareItemEnchantEvent event) {

            var player = event.getEnchanter();
            if(!SessionsManager.isClaiming(player)) { return; }

            ItemStack item = event.getItem();

            var session = SessionsManager.getClaimingSession(player);
            if(session == null) { return; }

            ItemStack wand = session.getWand();
            if(item != wand) { return; }

            event.setCancelled(true);

        }

        @EventHandler
        public void disableDropping(PlayerDropItemEvent event) {
            var player = event.getPlayer();
            if (SessionsManager.isClaiming(player)) { event.setCancelled(true); }
        }

        // Could do "PrepareAnvilEvent" and "PrepareGrindstoneEvent" separately,
        // but Paper complains about performance issues when trying to use "PrepareGrindstoneEvent",
        // so to bypass this warning, it's more readable to just use "PrepareResultEvent" as a replacement for both.
        // At least until Paper fixes or removes the warning for "PrepareGrindstoneEvent".
        @EventHandler
        public void disableRepairAndEnchant(PrepareResultEvent event) {

            var player = (Player) event.getView().getPlayer();
            if(!SessionsManager.isClaiming(player)) { return; }

            var session = SessionsManager.getClaimingSession(player);
            if(session == null) { return; }

            ItemStack wand = session.getWand();

            if ((event.getInventory() instanceof AnvilInventory anvilInventory &&
                    (wand == anvilInventory.getFirstItem() || wand == anvilInventory.getSecondItem())) ||

                    (event.getInventory() instanceof GrindstoneInventory grindstoneInventory &&
                    (wand == grindstoneInventory.getUpperItem() || wand == grindstoneInventory.getLowerItem()))) {

                event.setResult(new ItemStack(Material.AIR));

            }

        }

        @EventHandler
        public void disableInteract(PlayerInteractEvent event) {
            var player = event.getPlayer();
            var action = event.getAction();

            if (SessionsManager.isClaiming(player) && action != Action.PHYSICAL) { event.setCancelled(true); }
        }

        @EventHandler
        public void disableSwitchingSlots(PlayerItemHeldEvent event) {

            var player = event.getPlayer();
            if(!SessionsManager.isClaiming(player)) { return; }

            var session = SessionsManager.getClaimingSession(player);
            if(session == null) { return; }

            int slot = session.getWandSlot();

            event.setCancelled(true);

            //Fix wrong slot being selected sometimes after changing
            final PlayerInventory inventory = player.getInventory();

            new BukkitRunnable() {
                @Override public void run() { inventory.setHeldItemSlot(slot); }
            }.runTaskLater(Claims.get(), 5);

        }

        @EventHandler
        public void updateBrokenBlocks(BlockBreakEvent event) {
            new BukkitRunnable() {
                @Override
                public void run() {
//                    Location loc = event.getBlock().getLocation();
//                    for (ClaimingSession session : SessionsManager.getClaimingSessions()) {
//                        if (session.isSelected()) {
//                            PlotCornerType corner = session.getCorner(loc);
//                            if (corner != null) {
//                                session.getOwner().sendBlockChange(loc.getWorld().getHighestBlockAt(loc).getLocation(), corner.getMaterial().createBlockData());
//                            } else if (session.isInOutline(loc)) {
//                                session.getOwner().sendBlockChange(loc.getWorld().getHighestBlockAt(loc).getLocation(), Material.YELLOW_CONCRETE.createBlockData());
//                            }
//                        }
//                    }
                }
            }.runTaskLater(Claims.get(), 2);
        }

        @EventHandler
        public void updateExplodedBlocks(EntityExplodeEvent event) {
            Object[] blocks = event.blockList().toArray();
            new BukkitRunnable() {
                @Override
                public void run() {
//                    for (Object obj : blocks) {
//                        Block block = (Block) obj;
//                        Location loc = block.getLocation();
//                        for (ClaimingSession session : SessionsManager.getClaimingSessions()) {
//                            if (session.isSelected()) {
//                                PlotCornerType corner = session.getCorner(loc);
//                                if (corner != null) {
//                                    session.getOwner().sendBlockChange(loc.getWorld().getHighestBlockAt(loc).getLocation(), corner.getMaterial().createBlockData());
//                                } else if (session.isInOutline(loc)) {
//                                    session.getOwner().sendBlockChange(loc.getWorld().getHighestBlockAt(loc).getLocation(), Material.YELLOW_CONCRETE.createBlockData());
//                                }
//                            }
//                        }
//                    }
                }
            }.runTaskLater(Claims.get(), 2);
        }
    }

    //Left-clicking and right-clicking on blocks using a wand
    public static class WandActions implements Listener {
        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            Action action = event.getAction();
            //Change operation
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                Player player = event.getPlayer();
                if(!SessionsManager.isClaiming(player)) { return; }

                ClaimingSession session = SessionsManager.getClaimingSession(player);
                if (session != null && session.getOwner().isSneaking()) {
                    switch (session.getOperation()) {
                        case ADD:
                            WandAction op = WandAction.CUT;
                            session.setOperation(op);

                            HashMap<String, Object> temp = new HashMap<>();
                            temp.put("operation", op);
                            i18n.EN.OPERATION_CHANGE.getMessage(temp).forEach(session.getOwner()::sendMessage);
                            break;
                        case CUT:
                            op = WandAction.ADD;
                            session.setOperation(op);

                            temp = new HashMap<>();
                            temp.put("operation", op);
                            i18n.EN.OPERATION_CHANGE.getMessage(temp).forEach(session.getOwner()::sendMessage);
                            break;
                    }
                    return;
                }
            }

            //Confirm selection
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                Player player = event.getPlayer();
                if(!SessionsManager.isClaiming(player)) { return; }

                ClaimingSession session = SessionsManager.getClaimingSession(player);
                if (session != null && session.getOwner().isSneaking() && session.isSelected()) {
                    Claims.sessionsManager.confirmClaimingSession(session);
                    return;
                }
            }

            //Select corner
            if (event.getClickedBlock() != null) {
                Player player = event.getPlayer();
                if(!SessionsManager.isClaiming(player)) { return; }

                ClaimingSession session = SessionsManager.getClaimingSession(player);
                //TODO Warn about too small of a selection
                if (session != null) {
                    Location location = event.getClickedBlock().getLocation();
                    if (action == Action.LEFT_CLICK_BLOCK && !location.equals(session.getFirstCorner())) {
                        session.setCorner(PlotCornerType.FIRST, location);
                    } else if (action == Action.RIGHT_CLICK_BLOCK && !location.equals(session.getSecondCorner())) {
                        session.setCorner(PlotCornerType.SECOND, location);
                    }
                }
            }
        }

        @EventHandler
        public void cancelSession(PlayerDropItemEvent event) {
            Item item = event.getItemDrop();
            Player player = event.getPlayer();
            ClaimingSession session = SessionsManager.getClaimingSession(player);
            if (session != null) {
                if (session.isPreparedToCancel()) {
                    //TODO Exit session
                    Claims.sessionsManager.cancelClaimingSession(session, false);
                    item.remove();
                } else {
                    session.setCancelPreparation();
                    i18n.EN.CANCEL_WARNING.getMessage().forEach(session.getOwner()::sendMessage);
                }
            }
        }

        @EventHandler
        public void cancelOnQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            ClaimingSession session = SessionsManager.getClaimingSession(player);
            if (session != null) {
                Claims.sessionsManager.cancelClaimingSession(session, true);
            }
        }
    }
}