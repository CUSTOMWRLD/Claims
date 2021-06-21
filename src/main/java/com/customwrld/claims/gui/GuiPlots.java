package com.customwrld.claims.gui;

import com.customwrld.claims.Claims;
import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.plot.GuiManagePlot;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.smartinvs.content.SlotIterator;
import com.customwrld.customlib.util.CC;
import com.customwrld.customlib.util.ItemFactory;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.locationtech.jts.geom.Point;

import java.util.Set;

public class GuiPlots implements InventoryProvider {

    public static final SmartInventory INVENTORY = SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
            .id("claims-list")
            .provider(new GuiPlots())
            .size(6, 9)
            .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.BLUE + ChatColor.BOLD + "Plots List")
            .build();

    //Items
    private static final ItemStack availableItem = new ItemFactory(Material.ORANGE_CONCRETE_POWDER).setName(ChatColor.GOLD + "Free Slot").build();
    private static final ItemStack unavailableItem = new ItemFactory(Material.BLACK_CONCRETE).setName(ChatColor.DARK_GRAY + "Unavailable Slot").build();

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(GuiMain.getBorderItem()));

        var pagination = contents.pagination();

        Set<Plot> plots = Claims.plotsManager.getPlayerPlots(player);

        var items = new ClickableItem[7 + (Math.floorDiv(Claims.plotsManager.getMaxPlotsForPlayer(player), 7) * 7)];

        var i = 0;
        for (Plot plot : plots) {
            Point center = plot.getPolygon().getCentroid();
            int size = (int) plot.getPolygon().toAccurate().getArea(); //TODO Generate the accurate polygon once for plot
            items[i] = ClickableItem.of(
                    new ItemFactory(GuiMain.getPlotInformationItem(plot))
                            .setType(plot.isOwner(player) ? Material.LIME_CONCRETE : Material.LIGHT_BLUE_CONCRETE)
                            .addLoreLines("", CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Click to manage!")
                            .build(),
                    e -> GuiManagePlot.getInventory(plot).open(player));
            i++;
        }

        for (; i < Claims.plotsManager.getMaxPlotsForPlayer(player); i++)
            items[i] = ClickableItem.empty(availableItem);

        for (; i < items.length; i++)
            items[i] = ClickableItem.empty(unavailableItem);

        pagination.setItems(items);
        pagination.setItemsPerPage(7);

        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1));

        contents.set(3, 3, ClickableItem.of(GuiMain.getPreviousPageItem(), e -> INVENTORY.open(player, pagination.previous().getPage())));

        contents.set(3, 5, ClickableItem.of(GuiMain.getNextPageItem(), e -> INVENTORY.open(player, pagination.next().getPage())));

        contents.set(4, 4, ClickableItem.of(GuiMain.getBackItem(), e -> GuiMain.INVENTORY.open(player)));
    }

}
