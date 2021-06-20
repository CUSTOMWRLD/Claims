package com.customwrld.claims.gui.plot.groups.members;

import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.gui.plot.groups.GuiManageGroup;
import com.customwrld.claims.permissions.groups.PlotGroup;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.*;
import com.customwrld.customlib.util.ItemFactory;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Set;

@AllArgsConstructor
public class GuiMembers implements InventoryProvider {

    public static SmartInventory getInventory(Plot plot, PlotGroup group) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-members")
                .provider(new GuiMembers(plot, group))
                .size(5, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.BLUE + ChatColor.BOLD + "Browsing Group's Members")
                .build();
    }

    private final Plot plot;
    private final PlotGroup group;

    //Items
    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(2, 8, ClickableItem.empty(GuiMain.getGroupInformationItem(plot.getGroupsHolder(), group)));

        contents.set(3, 8, ClickableItem.empty(GuiMain.getPlotInformationItem(plot)));

        contents.set(2, 6, ClickableItem.of(GuiMain.getBackItem(), e -> GuiManageGroup.getInventory(plot, group).open(player)));

        Set<OfflinePlayer> members = plot.getGroupsHolder().getMembers(group);

        Pagination pagination = contents.pagination();

        ClickableItem[] items = new ClickableItem[members.size()];

        int i = 0;
        for(OfflinePlayer member : members){
            items[i] = ClickableItem.of(new ItemFactory(Material.PLAYER_HEAD)
                            .setName(member.getName())
                            .setOwningPlayer(member)
                            .build(),
                    e -> GuiManageMember.getInventory(contents.inventory(), plot, member).open(player)
            );
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(25);

        pagination.addToIterator(new MultiLineSlotIterator(contents, contents.inventory(), 0, 0, 5, 5, SlotIterator.Type.HORIZONTAL, EnumIteratorXDirection.RIGHT, EnumIteratorYDirection.DOWN));

        contents.set(0, 6, ClickableItem.of(GuiMain.getPreviousPageItem(),
                e -> getInventory(plot, group).open(player, pagination.previous().getPage())));

        contents.set(4, 6, ClickableItem.of(GuiMain.getNextPageItem(),
                e -> getInventory(plot, group).open(player, pagination.next().getPage())));

        contents.fillColumn(5, ClickableItem.empty(GuiMain.getBorderItem()));
    }

}