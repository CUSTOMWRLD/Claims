package com.customwrld.claims;

import com.customwrld.claims.holograms.HologramArea;
import com.customwrld.claims.managers.PreviewManager;
import com.customwrld.claims.utils.GeometryUtils;
import com.customwrld.claims.utils.UtilizedPolygon;
import com.customwrld.customlib.hologram.Hologram;
import com.customwrld.customlib.hologram.component.AbstractHologramComponent;
import com.customwrld.customlib.hologram.component.HologramItemComponent;
import com.customwrld.customlib.hologram.component.HologramTextComponent;
import com.customwrld.customlib.util.CC;
import com.customwrld.customlib.util.LocationUtil;
import com.customwrld.customlib.util.MetadataUtil;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimingSession {

    @Getter private final Player owner;

    @Getter private final ItemStack wand;
    @Getter private final int wandSlot;

    @Getter private Location firstCorner;
    @Getter private Location secondCorner;

    @Getter private UtilizedPolygon polygon;

    @Getter private World world;
    @Getter @Setter private WandAction operation;

    @Getter HologramArea hologramArea;

    @Getter Hologram firstCornerHologram;
    @Getter Hologram secondCornerHologram;

    @Getter Hologram middleHologram;

    //Cancelling
    private int prepareCancelTime;

    public ClaimingSession(Player owner, ItemStack wand, int wandSlot) {
        this.owner = owner;

        this.wand = wand;
        this.wandSlot = wandSlot;

        this.firstCorner = null;
        this.secondCorner = null;

        this.world = owner.getWorld();

        this.operation = WandAction.ADD;

        this.prepareCancelTime = 0;
    }

    private static Hologram setHologramArea(Hologram hologram, Player player, Location loc, List<String> labels, ItemStack item) {
        if (hologram != null) {
            hologram.delete(player);
        }

        hologram = new Hologram(loc);

        for (String label : labels) {
            hologram.append(new HologramTextComponent(label));
        }

        if (item != null) {
            hologram.append(new HologramItemComponent(item));
        }

        hologram.render(player);

        return hologram;
    }

    //Utils
    public void reset() {
        prepareCancelTime = 0;

        removeHolograms();

        if (!isSelected()) {
            if (firstCorner != null) {
                Block block = world.getHighestBlockAt(firstCorner);
                Location location = block.getLocation();
                owner.sendBlockChange(location, block.getBlockData());
            }

            if (secondCorner != null) {
                Block block = world.getHighestBlockAt(secondCorner);
                Location location = block.getLocation();
                owner.sendBlockChange(location, block.getBlockData());
            }
        }

        firstCorner = null;
        secondCorner = null;
    }

    public void setCorner(PlotCornerType cornerType, Location loc) {

        if(cornerType == PlotCornerType.OTHER) { throw new IllegalArgumentException("cornerType cannot be PlotCornerType.OTHER"); }

        boolean reselected = false;

        if (isSelected()) {
            reselected = true;
            this.owner.playSound(this.owner.getLocation(), Sound.ITEM_BOOK_PUT, 100f, 1f);
        }

        Location firstCorner = (cornerType == PlotCornerType.FIRST) ? loc : this.firstCorner;
        Location secondCorner = (cornerType == PlotCornerType.SECOND) ? loc : this.secondCorner;

        if (firstCorner != null && secondCorner != null) {

            if (firstCorner.getWorld() == secondCorner.getWorld()) { //Selected a region

                if (reselected) {

                    World world = firstCorner.getWorld();

                    hologramArea.updateShape(createPolygon(world, firstCorner, secondCorner));

                    int sizeX = Math.abs(firstCorner.getBlockX() - secondCorner.getBlockX()) + 1;
                    int sizeZ = Math.abs(firstCorner.getBlockZ() - secondCorner.getBlockZ()) + 1;

                    HologramTextComponent textComponent = ((HologramTextComponent) middleHologram.getChildren().get(0));
                    textComponent.setText(ChatColor.WHITE.toString() + ChatColor.BOLD + "Size: " + sizeX + "x" + sizeZ + " \u258d " + "Total: " + (sizeX * sizeZ));

                    Hologram cornerHologram = (cornerType == PlotCornerType.FIRST) ? firstCornerHologram : secondCornerHologram;

                    textComponent = ((HologramTextComponent) cornerHologram.getChildren().get(1));
                    textComponent.setText(ChatColor.GREEN + "(" + loc.getBlockX() + ", " + loc.getBlockZ() + ")");

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("sizeX", sizeX);
                    data.put("sizeZ", sizeZ);

                    i18n.EN.RESELECTED_REGION.getMessage(data).forEach(this.owner::sendMessage);

                }

            } else {
                if(cornerType == PlotCornerType.FIRST) {
                    secondCorner = null;
                } else {
                    firstCorner = null;
                }

                this.world = loc.getWorld();
            }

        }

        this.firstCorner = firstCorner;
        this.secondCorner = secondCorner;

        if(!reselected) {

            if(isSelected()) {

                polygon = createPolygon(null, null, null);
                createHolograms(cornerType, loc);

                this.owner.playSound(this.owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

                int sizeX = Math.abs(firstCorner.getBlockX() - secondCorner.getBlockX()) + 1;
                int sizeZ = Math.abs(firstCorner.getBlockZ() - secondCorner.getBlockZ()) + 1;

                HashMap<String, Object> data = new HashMap<>();
                data.put("sizeX", sizeX);
                data.put("sizeZ", sizeZ);

                i18n.EN.SELECTED_REGION.getMessage(data).forEach(this.owner::sendMessage);

            } else {
                drawCorners(true);
            }

        }

    }
    
    public boolean isSelected() {
        return (this.firstCorner != null && this.secondCorner != null) && this.firstCorner.getWorld() == this.secondCorner.getWorld();
    }

    public Set<Chunk> getChunks() {
        Set<Chunk> chunks = new HashSet<>();

        //TODO Check if it's right?
        List<Coordinate> coordinates = GeometryUtils.getGridCoordinatesInPolygon(UtilizedPolygon.asSquare(world, firstCorner, secondCorner), 16);

        for(Coordinate coordinate : coordinates) {
            chunks.add(world.getChunkAt(Math.floorDiv((int) Math.floor(coordinate.x), 16), Math.floorDiv((int) Math.floor(coordinate.y), 16)));
        }

        return chunks;
    }

    //Cancellation
    public boolean isPreparedToCancel() {
        return Bukkit.getCurrentTick() >= this.prepareCancelTime && Bukkit.getCurrentTick() < this.prepareCancelTime + (20 * 10);
    }

    public void setCancelPreparation() {
        this.prepareCancelTime = Bukkit.getCurrentTick();
    }

    //Holograms
    //TODO Fix side holograms dying when side they're supposed to represent is 1 block wide
    public void createHolograms(PlotCornerType reselectedCorner, Location loc) {

        var lineHologram = new Hologram(new Location(world, 0.5, 2, 0.5));
        lineHologram.append(HologramTextComponent.empty());

        hologramArea = new HologramArea(world, createPolygon(null, firstCorner, secondCorner), true,
                Material.YELLOW_CONCRETE,
                Material.ORANGE_CONCRETE,
                Material.REDSTONE_BLOCK,
                Material.LAPIS_BLOCK,
                lineHologram);

        Location firstCorner = (reselectedCorner == PlotCornerType.FIRST) ? loc : this.firstCorner;
        Location secondCorner = (reselectedCorner == PlotCornerType.SECOND) ? loc : this.secondCorner;

        int sizeX = Math.abs(firstCorner.getBlockX() - secondCorner.getBlockX()) + 1;
        int sizeZ = Math.abs(firstCorner.getBlockZ() - secondCorner.getBlockZ()) + 1;

        middleHologram = new Hologram(new Location(world, 0, 4, 0));
        middleHologram.append(new HologramTextComponent(ChatColor.WHITE.toString() + ChatColor.BOLD + "Size: " + sizeX + "x" + sizeZ + " \u258d " + "Total: " + (sizeX * sizeZ)));

        hologramArea.setMiddleHologram(middleHologram);

        hologramArea.render();

        hologramArea.setHologramLineModifier((player, line) -> {

            if(line.getId() == 0) {

                Location location = firstCorner.toHighestLocation().add(0.5, 2, 0.5);

                if(line.getStartHologram() == null) {
                    firstCornerHologram = new Hologram(location);
                    firstCornerHologram.append(
                            new HologramTextComponent(CC.RGB(255, 0, 0) + "First Corner (LMB)"));
                    firstCornerHologram.append(
                            new HologramTextComponent(ChatColor.GREEN + "(" + firstCorner.getBlockX() + ", " + firstCorner.getBlockZ() + ")"));

                    line.setStartHologram(firstCornerHologram);
                } else {
                    List<AbstractHologramComponent> children = line.getStartHologram().getChildren();
                    HologramTextComponent textComponent = ((HologramTextComponent) children.get(1));
                    textComponent.setText(ChatColor.GREEN + "(" + firstCorner.getBlockX() + ", " + firstCorner.getBlockZ() + ")");
                }

            } else if(line.getId() == 2) {

                Location location = secondCorner.toHighestLocation().add(0.5, 2, 0.5);

                if(line.getStartHologram() == null) {
                    secondCornerHologram = new Hologram(location);
                    secondCornerHologram.append(
                            new HologramTextComponent(CC.RGB(0, 0, 255) + "Second Corner (RMB)"));
                    secondCornerHologram.append(
                            new HologramTextComponent(ChatColor.GREEN + "(" + secondCorner.getBlockX() + ", " + secondCorner.getBlockZ() + ")"));

                    line.setStartHologram(secondCornerHologram);
                } else {
                    List<AbstractHologramComponent> children = secondCornerHologram.getChildren();
                    HologramTextComponent textComponent = ((HologramTextComponent) children.get(1));
                    textComponent.setText(ChatColor.GREEN + "(" + secondCorner.getBlockX() + ", " + secondCorner.getBlockZ() + ")");
                }

            }

            var hologram = line.getMiddleHologram();
            HologramTextComponent textComponent = ((HologramTextComponent) hologram.getChildren().get(0));

            textComponent.setText(ChatColor.GOLD + String.valueOf(line.getLength() + 1), player);
            hologram.setLocation(   //How the fuck do you format this
                    LocationUtil.toHighestLocationAccurate(
                            GeometryUtils.JTSCoordinateToBukkitLocation(world, GeometryUtils.lerp(line.getStart(), line.getEnd(), 0.5)).add(.5, 0, .5)
                    ).add(0, 2, 0));

        });

        hologramArea.render(owner);

    }

    public void removeHolograms() {
        if(hologramArea == null) { return; }

        hologramArea.delete(owner);
        hologramArea = null;

        MetadataUtil.removeFromListAndDeleteIfEmpty(owner, PreviewManager.ACTIVE_PREVIEWS_KEY, Claims.get(), this);
    }

    //Previews
    public void drawCorners(boolean onlyHighest) {
        var firstCorner = this.firstCorner;
        var secondCorner = this.secondCorner;

        var owner = this.owner;

        var world = this.world;
        new BukkitRunnable() {
            @Override
            public void run() {
                    if (firstCorner != null) {
                        owner.sendBlockChange(world.getHighestBlockAt(firstCorner).getLocation(), PlotCornerType.FIRST.getMaterial().createBlockData());
                    }

                    if (secondCorner != null) {
                        owner.sendBlockChange(world.getHighestBlockAt(secondCorner).getLocation(), PlotCornerType.SECOND.getMaterial().createBlockData());
                    }
            }
        }.runTaskLater(Claims.get(), 0); //Doesn't change the block if there isn't any delay
    }

    //Boundaries
    public UtilizedPolygon createPolygon(@Nullable World world, @Nullable Location first, @Nullable Location second) {
        world = world != null ? world : this.world;

        first = (first == null) ? firstCorner : first;
        second = (second == null) ? secondCorner : second;

        return UtilizedPolygon.asSquare(world, first, second);

    }

    public PlotCornerType getCorner(Location loc) {
        //First corner
        if (firstCorner != null) {
            if ((loc.getX() >= firstCorner.getBlockX() && loc.getX() < firstCorner.getBlockX() + 1) &&
                    (loc.getZ() >= firstCorner.getBlockZ() && loc.getZ() < firstCorner.getBlockZ() + 1)) {
                return PlotCornerType.FIRST;
            }
        }

        //Second corner
        if (secondCorner != null) {
            if ((loc.getX() >= secondCorner.getBlockX() && loc.getX() < secondCorner.getBlockX() + 1) &&
                    (loc.getZ() >= secondCorner.getBlockZ() && loc.getZ() < secondCorner.getBlockZ() + 1)) {
                return PlotCornerType.SECOND;
            }
        }

        //Other
        if (isSelected()) {
            Location other = new Location(world, firstCorner.getBlockX(), 0, secondCorner.getBlockZ());
            if ((loc.getX() >= other.getBlockX() && loc.getX() < other.getBlockX() + 1) &&
                    (loc.getZ() >= other.getBlockZ() && loc.getZ() < other.getBlockZ() + 1)) {
                return PlotCornerType.OTHER;
            }

            other = new Location(world, secondCorner.getBlockX(), 0, firstCorner.getBlockZ());
            if ((loc.getX() >= other.getBlockX() && loc.getX() < other.getBlockX() + 1) &&
                    (loc.getZ() >= other.getBlockZ() && loc.getZ() < other.getBlockZ() + 1)) {
                return PlotCornerType.OTHER;
            }
        }

        return null;
    }

    public PlotCornerType isInHighestOnCorner(Location loc) {
        if (world.getHighestBlockAt(loc).getLocation().equals(loc)) {
            return getCorner(loc);
        }
        return null;
    }

}
