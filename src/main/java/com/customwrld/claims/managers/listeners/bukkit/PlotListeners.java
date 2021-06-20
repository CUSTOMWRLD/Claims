package com.customwrld.claims.managers.listeners.bukkit;

import com.customwrld.claims.Claims;
import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.plot.GuiPlotOptions;
import com.customwrld.claims.gui.plot.modals.ModalConfirmation;
import com.customwrld.claims.managers.DatabaseManager;
import com.customwrld.claims.options.PlotOption;
import com.customwrld.claims.permissions.PlotPermission;
import com.customwrld.claims.permissions.PlotPermissionLevel;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.util.ItemFactory;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlotListeners {

    public static class PlotEvents implements Listener {

        // Permission-based (and other) Events

        @EventHandler
        public void blockBroken(BlockBreakEvent event) {

            var block = event.getBlock();

            var plot = Claims.plotsManager.getPlotOnLocation(block.getWorld(), block.getX(), block.getZ());
            if (plot == null) { return; }

            var player = event.getPlayer();
            if (!plot.hasPermission(player, PlotPermission.BLOCK_BREAK)) {
                player.playEffect(block.getLocation().add(0, 1, 0), Effect.SMOKE, BlockFace.UP);
                event.setCancelled(true);
            }

        }

        @EventHandler
        public void blockPlaced(BlockPlaceEvent event) {

            var block = event.getBlock();

            var plot = Claims.plotsManager.getPlotOnLocation(block.getWorld(), block.getX(), block.getZ());
            if (plot == null) { return; }

            var player = event.getPlayer();
            if (!plot.hasPermission(player, PlotPermission.BLOCK_PLACE)) {
                player.playEffect(block.getLocation(), Effect.SMOKE, BlockFace.UP);
                event.setCancelled(true);
            }

        }

        //TODO Experiment, Possible use without the need for packets?
//        @EventHandler
//        public void blockPlace(BlockMultiPlaceEvent event) {
//            Block block = event.getBlock();
//            Plot plot = Claims.plotsManager.getPlotOnCoordinates(block.getWorld(), block.getX(), block.getZ());
//            if (plot != null) {
//                if (!plot.hasPermission(event.getPlayer(), EnumPermission.BLOCK_PLACE)) {
//                    event.setCancelled(true);
//                }
//            }
//        }

        @EventHandler
        public void blockInteracted(PlayerInteractEvent event) {

            var block = event.getClickedBlock();
            if(block == null) { return; }

            var plot = Claims.plotsManager.getPlotOnLocation(block.getWorld(), block.getX(), block.getZ());
            if(plot == null) { return; }

            var player = event.getPlayer();
            if (!plot.hasPermission(player, PlotPermission.BLOCK_BREAK)) {
                player.playEffect(block.getLocation(), Effect.SMOKE, BlockFace.UP);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setCancelled(true);
            }

        }

        @EventHandler
        public void potionSplashed(PotionSplashEvent event) {

            ThrownPotion potion = event.getEntity();
            ProjectileSource source = potion.getShooter();

            Location location = potion.getLocation();

            Plot plot = Claims.plotsManager.getPlotOnLocation(potion.getWorld(), location.getBlockX(), location.getBlockZ());
            if (plot == null) { return; }

            if (source instanceof Player) {

                Player player = (Player) source;

                if (!plot.hasPermission(player, PlotPermission.PLAYER_SPLASH_POTIONS)) { event.setCancelled(true); }

            } else if (source instanceof BlockProjectileSource) {

                BlockProjectileSource blockSource = (BlockProjectileSource) source;
                Block block = blockSource.getBlock();
                Location blockLocation = block.getLocation();

                if (!plot.isInside(blockLocation)) { event.setCancelled(true); }

            }

        }

        @EventHandler
        public void animalDamagedByEntity(EntityDamageByEntityEvent event) {

            Entity victim = event.getEntity();
            Entity damager = event.getDamager();

            if(!((victim instanceof Animals || victim instanceof NPC) && damager instanceof Player)) { return; }

            Location location = victim.getLocation();

            Plot plot = Claims.plotsManager.getPlotOnLocation(location.getWorld(), location.getBlockX(), location.getBlockZ());
            if (plot == null) { return; }

            Player player = (Player) damager;

            if (!plot.hasPermission(player, PlotPermission.PLAYER_ATTACK_ANIMALS)) { event.setCancelled(true); }

        }

        @EventHandler
        public void animalDamagedByBlock(EntityDamageByBlockEvent event) {

            Entity victim = event.getEntity();

            Block damager = event.getDamager();
            if(damager == null) { return; }

            if(victim instanceof Animals || victim instanceof NPC) {

                Location location = victim.getLocation();

                Plot plot = Claims.plotsManager.getPlotOnLocation(location);
                if (plot == null) { return; }

                Location blockLocation = damager.getLocation();

                if (!plot.isInside(blockLocation)) { event.setCancelled(true); }

            }

        }

        @EventHandler
        public void containerOpened(InventoryOpenEvent event) {

            Inventory inventory = event.getInventory();
            Player player = (Player) event.getPlayer();

            Location location = inventory.getLocation();
            if(location == null) { return; }

            switch(inventory.getType()) {
                case CHEST:
                case SHULKER_BOX:
                case BARREL:
                case DROPPER:
                case DISPENSER:
                case BREWING:
                case BEACON:
                case HOPPER:
                case LECTERN:

                    Plot plot = Claims.plotsManager.getPlotOnLocation(location);
                    if (plot == null) { return; }

                    if(!plot.hasPermission(player, PlotPermission.STORAGE_CHESTS)) { event.setCancelled(true); }

                    break;

                case ANVIL:

                    plot = Claims.plotsManager.getPlotOnLocation(location);
                    if (plot == null) { return; }

                    if(!plot.hasPermission(player, PlotPermission.STORAGE_ANVILS)) { event.setCancelled(true); }

                    break;

                case FURNACE:
                case SMOKER:
                case BLAST_FURNACE:

                    plot = Claims.plotsManager.getPlotOnLocation(location);
                    if (plot == null) { return; }

                    if(!plot.hasPermission(player, PlotPermission.STORAGE_FURNACES)) { event.setCancelled(true); }

                    break;

                case ENCHANTING:

                    plot = Claims.plotsManager.getPlotOnLocation(location);
                    if (plot == null) { return; }

                    if(!plot.hasPermission(player, PlotPermission.STORAGE_ENCHANTMENT_TABLE)) { event.setCancelled(true); }

                    break;

            }

            if(event.isCancelled())
                player.playEffect(inventory.getLocation(), Effect.SMOKE, BlockFace.UP);
        }

        @EventHandler
        public void movedItemsBetweenInventories(InventoryMoveItemEvent event) {

            Inventory source = event.getSource();
            Inventory destination = event.getDestination();

            if(destination.getType() != InventoryType.HOPPER || destination.getHolder() instanceof HopperMinecart) { return; }

            Location sourceLocation = source.getLocation();
            if(sourceLocation == null) { return; }

            Plot plot = Claims.plotsManager.getPlotOnLocation(sourceLocation);
            if (plot == null) { return; }

            Location destinationLocation = destination.getLocation();
            if(destinationLocation == null) { return; }

            if (plot.getPermissionLevel(PlotPermission.STORAGE_CHESTS) == PlotPermissionLevel.ALL) { return; }

            Block hopper = destinationLocation.getBlock();

            Hopper hopperData = (Hopper) hopper.getBlockData();
            Location hopperDestination = destinationLocation.clone().add(hopperData.getFacing().getDirection());

            if(!plot.isInside(hopperDestination)) { event.setCancelled(true); }

        }

        // Options-based Events

        @EventHandler
        public void playerDamagedByEntity(EntityDamageByEntityEvent event) {

            Entity victim = event.getEntity();
            Entity damager = event.getDamager();

            if(!(victim instanceof Player && damager instanceof Player)) { return; }

            Location location = victim.getLocation();

            Plot plot = Claims.plotsManager.getPlotOnLocation(location.getWorld(), location.getBlockX(), location.getBlockZ());
            if (plot == null) { return; }

            Player player = (Player) damager;

            if (!plot.isOptionEnabled(PlotOption.PVP)) { event.setCancelled(true); }

        }

        //Explosion Option

        @EventHandler public void blockExplosion(BlockExplodeEvent event) { handleExplosion(null, event.blockList()); }
        @EventHandler public void entityExplosion(EntityExplodeEvent event) { handleExplosion(event.getEntity(), event.blockList()); }

        private void handleExplosion(@Nullable Entity entity, List<Block> affectedBlocks) {

            affectedBlocks.removeIf(block -> {

                Location location = block.getLocation();

                Plot plot = Claims.plotsManager.getPlotOnLocation(location);
                if (plot == null) { return false; }

                return ((entity instanceof Explosive || entity instanceof EnderCrystal) && !plot.isOptionEnabled(PlotOption.EXPLOSION_GRIEF)) ||
                        (entity instanceof Mob && !plot.isOptionEnabled(PlotOption.MOB_GRIEF));

            });

        }

        @EventHandler
        public void mobSpawned(CreatureSpawnEvent event) {

            Entity entity = event.getEntity();

            if(!(entity instanceof Mob)) { return; }

            Location location = entity.getLocation();

            Plot plot = Claims.plotsManager.getPlotOnLocation(location.getWorld(), location.getBlockX(), location.getBlockZ());
            if (plot == null) { return; }

            if (!plot.isOptionEnabled(PlotOption.MOB_SPAWN)) { event.setCancelled(true); }

        }

        //Mob Griefing Options

        @EventHandler
        public void entityChangedBlock(EntityChangeBlockEvent event) {

            Block block = event.getBlock();
            Location location = block.getLocation();

            Plot plot = Claims.plotsManager.getPlotOnLocation(location);
            if (plot == null) { return; }

            if (!plot.isOptionEnabled(PlotOption.MOB_GRIEF)) { event.setCancelled(true); }

        }

        //Grass Spread & Mycelium Spread Options

        @EventHandler
        public void blockSpread(BlockSpreadEvent event) {

            Block block = event.getBlock();
            Location location = block.getLocation();

            Plot plot = Claims.plotsManager.getPlotOnLocation(location);
            if (plot == null) { return; }

            Material material = block.getType();

            if ((material == Material.MYCELIUM && !plot.isOptionEnabled(PlotOption.MYCELIUM_SPREAD)) ||
                    (material == Material.GRASS_BLOCK && !plot.isOptionEnabled(PlotOption.GRASS_SPREAD))) { event.setCancelled(true); }

        }

        @EventHandler
        public void leafDecayed(LeavesDecayEvent event) {

            Block block = event.getBlock();
            Location location = block.getLocation();

            Plot plot = Claims.plotsManager.getPlotOnLocation(location);
            if (plot == null) { return; }

            if (!plot.isOptionEnabled(PlotOption.LEAF_DECAY)) { event.setCancelled(true); }

        }

        @EventHandler
        public void structureGrew(StructureGrowEvent event) {

            Location origin = event.getLocation();
            List<BlockState> blocks = event.getBlocks();

            blocks.removeIf(block -> {

                Location location = block.getLocation();

                Plot plot = Claims.plotsManager.getPlotOnLocation(location);
                if(plot == null) { return false; }

                return !plot.isInside(origin) && !plot.isOptionEnabled(PlotOption.GROW_FROM_OUTSIDE);

            });

        }

        @EventHandler
        public void pistonExtended(BlockPistonExtendEvent event) {

            Block piston = event.getBlock();
            Location pistonLocation = piston.getLocation();

            List<Block> blocks = event.getBlocks();
            BlockFace direction = event.getDirection();

            Location headLocation = pistonLocation.clone().add(direction.getDirection());

            var plot = Claims.plotsManager.getPlotOnLocation(headLocation);

            for(Block block : blocks) {

                Location location = block.getLocation();
                Location movedLocation = location.clone().add(direction.getDirection());

                plot = Claims.plotsManager.getPlotOnLocation(movedLocation);

                if(plot.isInside(pistonLocation)) { continue; }

                if (!plot.isOptionEnabled(PlotOption.PISTON_FROM_OUTSIDE)) {
                    var world = plot.getWorld();
                    world.playEffect(headLocation, Effect.SMOKE, BlockFace.UP);
                    event.setCancelled(true);
                    break;
                }

            }

        }

        @EventHandler
        public void pistonRetracted(BlockPistonRetractEvent event) {

            Block piston = event.getBlock();
            Location pistonLocation = piston.getLocation();

            List<Block> blocks = event.getBlocks();
            BlockFace direction = event.getDirection();

            Location headLocation = pistonLocation.clone().add(direction.getDirection());

            for(Block block : blocks) {

                Location location = block.getLocation();

                Plot plot = Claims.plotsManager.getPlotOnLocation(location);
                if(plot.isInside(pistonLocation)) { continue; }

                Location movedLocation = location.add(direction.getDirection());

                if(!plot.isOptionEnabled(PlotOption.PISTON_FROM_OUTSIDE) && !plot.isInside(movedLocation)) {
                    var world = plot.getWorld();
                    world.playEffect(headLocation, Effect.SMOKE, BlockFace.UP);
                    event.setCancelled(true);
                    break;
                }

            }

        }

        @EventHandler
        public void fluidFlow(BlockFromToEvent event) {

            Block toBlock = event.getToBlock();
            Location destination = toBlock.getLocation();

            Plot plot = Claims.plotsManager.getPlotOnLocation(destination);
            if (plot == null) { return; }

            Block fromBlock = event.getBlock();
            Location source = fromBlock.getLocation();

            if (!plot.isInside(source) && !plot.isOptionEnabled(PlotOption.FLUIDS_FROM_OUTSIDE)) { event.setCancelled(true); }

        }

        @EventHandler
        public void dispenserActivated(BlockDispenseEvent event) {

            Block dispenser = event.getBlock();
            Location location = dispenser.getLocation();

            Dispenser dispenserData = (Dispenser) dispenser.getBlockData();
            Location destination = location.clone().add(dispenserData.getFacing().getDirection());

            Plot plot = Claims.plotsManager.getPlotOnLocation(destination);
            if (plot == null) { return; }

            if (plot.isOptionEnabled(PlotOption.DISPENSERS_FROM_OUTSIDE)) { return; }

            if(!plot.isInside(location)) {
                var world = plot.getWorld();
                world.playEffect(destination, Effect.SMOKE, BlockFace.UP);
                event.setCancelled(true);
            }

        }

    }

    public static class PlotLoaderEvents implements Listener {

        @EventHandler
        public void loadPlot(ChunkLoadEvent event) {
            Claims.plotsManager.getPlotLoader().addChunk(event.getChunk());
        }

        //Bulks
        @EventHandler
        public void increaseBulkSize(PlayerJoinEvent event) {
            Claims.plotsManager.getPlotLoader().bulkSize = 30 + (Bukkit.getOnlinePlayers().size() * 60);
        }

        @EventHandler
        public void decreaseBulkSize(PlayerQuitEvent event) {
            Claims.plotsManager.getPlotLoader().bulkSize = 30 + (Bukkit.getOnlinePlayers().size() * 60);
        }

    }

    public static class PlotGUIs implements Listener {

        @EventHandler
        public void renamePlot(AsyncPlayerChatEvent event) {

            Player player = event.getPlayer();

            Plot plot = GuiPlotOptions.getRenamingRequired().get(player);
            if(plot == null) { return; }

            String newName = ChatColor.stripColor(event.getMessage());

            //TODO Figure out if this is actually necessary?
            new BukkitRunnable() {
                @Override
                public void run() {

                    ModalConfirmation.createModal("Are you sure?", new ItemFactory(Material.NOTE_BLOCK)
                                    .setName(ChatColor.WHITE + "Do you want to rename plot \"" + plot.getName() + "\" to \"" + newName + "\"?")
                                    .build(),

                            ClickableItem.of(new ItemFactory(Material.LIME_CONCRETE).setName(ChatColor.GREEN + "Yes").build(), acceptClickEvent -> {
                                plot.setName(newName);
                                DatabaseManager.savePlot(plot);
                                player.closeInventory();
                                GuiPlotOptions.getInventory(plot).open(player);
                            }),

                            ClickableItem.of(new ItemFactory(Material.RED_CONCRETE).setName(ChatColor.RED + "No").build(), declineClickEvent -> {
                                player.closeInventory();
                                GuiPlotOptions.getInventory(plot).open(player);
                            })).open(player);

                    GuiPlotOptions.getRenamingRequired().remove(player);
                }

            }.runTask(Claims.get());

            event.setCancelled(true);

        }

    }
}
