package com.customwrld.claims.gui.plot.groups;

import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.gui.plot.groups.members.GuiMembers;
import com.customwrld.claims.gui.plot.groups.members.GuiNearPlayers;
import com.customwrld.claims.permissions.groups.PlotGroup;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.util.CC;
import com.customwrld.customlib.util.ItemFactory;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class GuiManageGroup implements InventoryProvider {

    public static SmartInventory getInventory(Plot plot, PlotGroup group) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-groups-settings")
                .provider(new GuiManageGroup(plot, group))
                .size(5, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.BLUE + ChatColor.BOLD + "Editing a Group")
                .build();
    }

    private final Plot plot;
    private final PlotGroup group;

    //Items
    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(GuiMain.getBorderItem()));

        contents.set(0, 4, ClickableItem.empty(GuiMain.getGroupInformationItem(plot.getGroupsHolder(), group)));

        contents.set(0, 3, ClickableItem.empty(GuiMain.getPlotInformationItem(plot)));

        contents.set(3, 4, ClickableItem.of(GuiMain.getBackItem(), e -> GuiGroups.getInventory(plot).open(player)));

        contents.set(2, 3, ClickableItem.of(
                new ItemFactory(Material.COMPASS)
                        .setName(CC.RGB(0, 170, 255) + "Search Nearby Players")
                        .setLore(CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Click to open!")
                        .build(),
                e -> GuiNearPlayers.getInventory(plot, group).open(player))
        );

        contents.set(2, 5, ClickableItem.of(
                new ItemFactory(Material.BOOKSHELF)
                        .setName(CC.RGB(0, 170, 255) + "Manage Members")
                        .setLore(CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Click to open!")
                        .build(),
                e -> GuiMembers.getInventory(plot, group).open(player))
        );
    }

}