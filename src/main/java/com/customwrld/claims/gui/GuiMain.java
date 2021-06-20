package com.customwrld.claims.gui;

import com.customwrld.claims.Claims;
import com.customwrld.claims.Plot;
import com.customwrld.claims.managers.PlotsManager;
import com.customwrld.claims.managers.PreviewManager;
import com.customwrld.claims.permissions.groups.GroupHolder;
import com.customwrld.claims.permissions.groups.PlotGroup;
import com.customwrld.claims.utils.BalanceUtils;
import com.customwrld.claims.utils.GeometryUtils;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.util.CC;
import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.flame.profile.Profile;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GuiMain implements InventoryProvider {

    public static final SmartInventory INVENTORY = SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
            .id("claims-main")
            .provider(new GuiMain())
            .size(5, 9)
            .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.DARK_GRAY+ "Claims")
            .build();

    //Items
    private static final ItemFactory listItem = new ItemFactory(Material.HEART_OF_THE_SEA).setName(CC.RGB(0, 170, 255) + "Manage Plots");

    @Getter private static final ItemStack previousPageItem = new ItemFactory(Material.SPECTRAL_ARROW).setName(ChatColor.RED + "Prev. Page").build();

    @Getter private static final ItemStack nextPageItem = new ItemFactory(Material.SPECTRAL_ARROW).setName(ChatColor.GREEN + "Next Page").build();

    @Getter private static final ItemStack backItem = new ItemFactory(Material.REDSTONE).setName(ChatColor.RED + "Back").build();

    @Getter private static final ItemStack borderItem = new ItemFactory(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setName("").build();

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(borderItem));

        contents.set(2, 3, ClickableItem.of(getStartClaimingItem(Profile.getByUuid(player.getUniqueId())), event -> {
            if(Claims.sessionsManager.startClaimingSession(event)) { player.closeInventory(); }
        }));

        PlotsManager plotsManager = Claims.plotsManager;

        contents.set(2, 5, ClickableItem.of(
                listItem
                        .setLore(ChatColor.WHITE + "Plots: " + CC.RGB(0, 170, 255) + plotsManager.getPlayerPlots(player).size() + "/" + plotsManager.getMaxPlotsForPlayer(player))
                        .build(),
                e -> GuiPlots.INVENTORY.open(player)));

        setTogglePreviewItem(player, contents);
    }

    public static ItemStack getStartClaimingItem(Profile profile) {
        return new ItemFactory(Material.GOLDEN_HOE)
                .setName(CC.RGB(0, 170, 255) + "Start Claiming")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setLore(Arrays.asList(
                        ChatColor.WHITE + "Claimable blocks left: " + CC.RGB(0, 170, 255) + BalanceUtils.getBlocks(profile),
                        CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Hint: You get 100 claimable blocks every 24 hours of gameplay!"))
                .build();
    }

    public static ItemStack getTogglePreviewItem(Player player) {
        var enabled = PreviewManager.hasAutoPreviewEnabled(player);
        return new ItemFactory(enabled ? Material.ENDER_EYE : Material.ENDER_PEARL)
                .setName(CC.RGB(0, 170, 255) + "Toggle Plot Preview")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setLore(Arrays.asList(
                        CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Allows you to view plots around you.",
                        CC.RGB(128, 200, 255) + "Status: " + ChatColor.BOLD + (enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();
    }

    private void setTogglePreviewItem(Player player, InventoryContents contents) {
        contents.set(3, 7, ClickableItem.of(
                getTogglePreviewItem(player),
                e -> {
                    PreviewManager.toggleAutoPreview(player);
                    setTogglePreviewItem(player, contents);
                })
        );
    }

    public static ItemStack getPlotInformationItem(Plot plot){
        Point center = plot.getPolygon().getCentroid();
        int size = (int) GeometryUtils.getAccuratePolygon(plot.getPolygon()).getArea(); //TODO Generate the accurate polygon once for plot
        return new ItemFactory(Material.NOTE_BLOCK)
                .setName(ChatColor.BLUE + plot.getName())
                .setLore(
                        ChatColor.DARK_GRAY + "Created: " + new Date(plot.getCreatedAt()),
                        ChatColor.DARK_GRAY + "Last Modified: " + new Date(plot.getLastModifiedAt()),
                        "",
                        CC.RGB(128, 200, 255) + "Location: " + CC.RGB(0, 170, 255) + "X: " + (int) center.getX() + " | Z: " + (int) center.getY(),
                        CC.RGB(128, 200, 255) + "Size: " + CC.RGB(0, 170, 255) + size)
                .build();
    }

    public static ItemStack getGroupInformationItem(GroupHolder holder, PlotGroup group){
        List<String> lore = new ArrayList<>();
        //TODO Fill with something

        return new ItemFactory(Material.PAPER).setName(group.getColor() + group.getDisplayName()).setLore(lore).build();
    }
}