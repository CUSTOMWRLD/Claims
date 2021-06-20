package com.customwrld.claims;

import com.customwrld.claims.holograms.HologramArea;
import com.customwrld.claims.managers.PreviewManager;
import com.customwrld.claims.options.OptionsHolder;
import com.customwrld.claims.options.PlotOption;
import com.customwrld.claims.permissions.PermissionHolder;
import com.customwrld.claims.permissions.PlotPermission;
import com.customwrld.claims.permissions.PlotPermissionLevel;
import com.customwrld.claims.permissions.groups.GroupHolder;
import com.customwrld.claims.permissions.groups.PlotGroup;
import com.customwrld.claims.utils.GeometryUtils;
import com.customwrld.customlib.hologram.Hologram;
import com.customwrld.customlib.hologram.component.HologramTextComponent;
import com.customwrld.customlib.util.MetadataUtil;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class Plot {
    private String name;
    private ObjectId id;

    private OfflinePlayer owner;
    private World world;

    @Setter private long createdAt;
    @Setter private long lastModifiedAt;

    private Polygon polygon;

    private Set<Chunk> chunks;

    private HologramArea preview;

    private OptionsHolder optionsHolder;

    private PermissionHolder permissionsHolder;
    private GroupHolder groupsHolder;

    public Plot(Document document, Consumer<Plot> callback) {
        name = document.getString("name");
        id = document.getObjectId("_id");

        owner = Bukkit.getOfflinePlayer(UUID.fromString(document.getString("owner")));
        world = Bukkit.getWorld(UUID.fromString(document.getString("world")));

        createdAt = document.getLong("createdAt");
        lastModifiedAt = document.getLong("lastModifiedAt");

        List<Document> vertices = document.getList("vertices", Document.class);

        var coordinates = new Coordinate[vertices.size()];
        for(int i = 0; i < vertices.size(); i++) {
            var vertexDocument = vertices.get(i);
            coordinates[i] = new Coordinate(vertexDocument.getInteger("x", 0), (int) vertexDocument.get("z", 0));
        }

        polygon = new GeometryFactory().createPolygon(coordinates);

        chunks = new HashSet<>();

        List<Document> chunksDocument = document.getList("chunks", Document.class);
        for(Document chunkDocument : chunksDocument) {
            PaperLib.getChunkAtAsync(this.world,
                    chunkDocument.getInteger("x", 0),
                    chunkDocument.getInteger("z", 0))
                    .thenAccept(chunk -> {
                        chunks.add(chunk);
                        callback.accept(this);
                    });
        }

        createPreview();

        optionsHolder = new OptionsHolder(document.get("options"));

        permissionsHolder = new PermissionHolder(document.get("permissions"));
        groupsHolder = new GroupHolder(document.get("groups"));
    }

    public Plot(ClaimingSession session) {
        var currentTime = System.currentTimeMillis();

        name = "New Plot";
        id = new ObjectId();

        owner = session.getOwner();
        world = session.getWorld();

        createdAt = currentTime;
        lastModifiedAt = currentTime;

        polygon = session.createPolygon(null, null);

        Location firstCorner = session.getFirstCorner();
        Location secondCorner = session.getSecondCorner();

        if((firstCorner.getX() > secondCorner.getX() && secondCorner.getZ() > firstCorner.getZ()) ||
                (firstCorner.getX() < secondCorner.getX() && secondCorner.getZ() < firstCorner.getX())) {

            polygon = polygon.reverse();
        }

        chunks = session.getChunks();

        createPreview();

        optionsHolder = new OptionsHolder();

        permissionsHolder = new PermissionHolder();
        groupsHolder = new GroupHolder();
    }

    public void setName(String name) {
        this.name = name;

        if(preview == null) { return; }

        var middleHologram = preview.getMiddleHologram();
        HologramTextComponent textComponent = ((HologramTextComponent) middleHologram.getChildren().get(0));

        textComponent.setText(ChatColor.BLUE.toString() + ChatColor.BOLD + name);
    }

    //Previews
    public void createPreview() {

        var lineHologram = new Hologram(new Location(world, 0.5, 2, 0.5));
        lineHologram.append(HologramTextComponent.empty());

        preview = new HologramArea(world, polygon, true,
                Material.LIME_CONCRETE, Material.WHITE_CONCRETE,
                lineHologram);

        var middleHologram = new Hologram(new Location(world, 0, 4, 0));
        middleHologram.append(new HologramTextComponent(ChatColor.BLUE.toString() + ChatColor.BOLD + name));
        middleHologram.append(HologramTextComponent.empty());
        middleHologram.append(new HologramTextComponent(ChatColor.WHITE.toString() + ChatColor.BOLD + "Size: " + NumberConversions.round(GeometryUtils.getAccuratePolygon(polygon).getArea()) + " Blocks"));

        preview.setMiddleHologram(middleHologram);

        preview.render();

        preview.setHologramModifier((player, area) -> {

                var hologram = area.getMiddleHologram();
                HologramTextComponent textComponent = ((HologramTextComponent) hologram.getChildren().get(1));

                if(isOwner(player)) {
                    textComponent.setText(ChatColor.WHITE.toString() + ChatColor.BOLD + "Owner: " + ChatColor.GREEN + owner.getName(), player);
                } else if(isAssociated(player)) {
                    textComponent.setText(ChatColor.WHITE.toString() + ChatColor.BOLD + "Owner: " + ChatColor.AQUA + owner.getName(), player);
                } else {
                    textComponent.setText(ChatColor.WHITE.toString() + ChatColor.BOLD + "Owner: " + ChatColor.RED + owner.getName(), player);
                }

        });

        preview.setHologramLineModifier((player, line) -> {

            var hologram = line.getMiddleHologram();
            HologramTextComponent textComponent = ((HologramTextComponent) hologram.getChildren().get(0));

            if(isOwner(player)) {
                line.setLineMaterial(Material.LIME_CONCRETE);
                textComponent.setText(ChatColor.GREEN + String.valueOf(line.getLength() + 1), player);
            } else if(isAssociated(player)) {
                line.setLineMaterial(Material.LIGHT_BLUE_CONCRETE);
                textComponent.setText(ChatColor.AQUA + String.valueOf(line.getLength() + 1), player);
            } else {
                line.setLineMaterial(Material.RED_CONCRETE);
                textComponent.setText(ChatColor.RED + String.valueOf(line.getLength() + 1), player);
            }

        });

    }

    public void removePreview() {
        if(preview == null) { return; }
        Iterator<Player> iterator = preview.getViewers().iterator();
        while(iterator.hasNext()) {
            Player viewer = iterator.next();
            if(!viewer.hasMetadata(PreviewManager.ACTIVE_PREVIEWS_KEY)) { return; }
            MetadataUtil.removeFromListAndDeleteIfEmpty(viewer, PreviewManager.ACTIVE_PREVIEWS_KEY, Claims.get(), preview);
        }
        preview.delete();
        preview = null;
    }

    //Permissions
    public PlotPermissionLevel getPermissionLevel(PlotPermission permission) {
        return permissionsHolder.getPermissionLevel(permission);
    }

    public boolean hasPermission(Player player, PlotPermission permission) {

        switch (permissionsHolder.getPermissionLevel(permission)) {

            case ALL:
                return true;

            case BUILDERS:

                //Owner/Trusted Check
                if(isOwner(player) || groupsHolder.getGroup(player) == PlotGroup.TRUSTED) { return true; }

                if(groupsHolder.getGroup(player) != PlotGroup.BUILDERS) { return false; }

                //Owner Online?
                if(owner != null && owner.isOnline()) { return true; }

                //Any Trusted Online?
                return groupsHolder.getMembers(PlotGroup.TRUSTED)
                        .stream()
                        .anyMatch(OfflinePlayer::isOnline);

            case TRUSTED:

                //Owner Check
                if(isOwner(player)) { return true; }

                PlotGroup group = groupsHolder.getGroup(player);
                if(group == null) { return false; }

                return group == PlotGroup.TRUSTED;

            case OWNER:

                if(isOwner(player)) { return true; }

            default:

                return false;

        }

    }

    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(owner.getUniqueId());
    }

    public boolean isPlotMember(Player player) {
        return groupsHolder.getGroup(player) != null;
    }

    public boolean isAssociated(Player player) {
        return isOwner(player) || isPlotMember(player);
    }

    //Options
    public boolean isOptionEnabled(PlotOption option) {
        return optionsHolder.getOptionState(option);
    }

    //Polygon - Modifiers
    public void mergeWith(Plot other) {
        if (owner != other.getOwner() || world != other.getWorld() || !polygon.intersects(other.getPolygon()))
            return;

        polygon = GeometryUtils.dissolvePoints(polygon.union(other.getPolygon()));
        generateChunks();
    }

    public void trimWith(Plot trimmer) {
        if (owner != trimmer.getOwner() || world != trimmer.getWorld() || !polygon.intersects(trimmer.getPolygon()))
            return;

        polygon = new GeometryFactory().createPolygon(polygon.difference(trimmer.getPolygon()).getCoordinates());
    }

    //Polygon - Boundaries
    public boolean isInside(World world, int x, int z) {
        return this.world == world && polygon.intersects(new GeometryFactory().createPoint(new Coordinate(x, z)));
    }

    public boolean isInside(Location location) {
        return isInside(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    public boolean isOnOutline(World world, int x, int z) {
        return this.world == world && polygon.touches(new GeometryFactory().createPoint(new Coordinate(x, z)));
    }

    public boolean isOnOutline(Location location) {
        return isOnOutline(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    //Data Transformers
    public List<Document> getVertices() {
        List<Document> vertices = new ArrayList<>();
        for (Coordinate coordinate : polygon.getCoordinates()) {
            vertices.add(new Document()
                    .append("x", NumberConversions.floor(coordinate.getX()))
                    .append("z", NumberConversions.floor(coordinate.getY())));
        }
        return vertices;
    }

    public List<Document> getChunkPositions() {
        List<Document> chunkPositions = new ArrayList<>();
        for (Chunk chunk : chunks) {
            chunkPositions.add(new Document()
                    .append("x", chunk.getX())
                    .append("z", chunk.getZ()));
        }
        return chunkPositions;
    }

    private void generateChunks() {
        Set<Chunk> chunks = new HashSet<>();

        List<Coordinate> coordinates = GeometryUtils.getGridCoordinatesInPolygon(polygon, 16);

        for(Coordinate coordinate : coordinates) {
            chunks.add(world.getChunkAt(Math.floorDiv((int) Math.floor(coordinate.x), 16), Math.floorDiv((int) Math.floor(coordinate.y), 16)));
        }

        this.chunks = chunks;
    }
}
