package com.customwrld.claims.gui.plot.groups.members;

import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.gui.plot.groups.GuiManageGroup;
import com.customwrld.claims.gui.plot.modals.ModalConfirmation;
import com.customwrld.claims.managers.DatabaseManager;
import com.customwrld.claims.permissions.groups.PlotGroup;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.*;
import com.customwrld.customlib.util.ItemFactory;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;

@AllArgsConstructor
public class GuiNearPlayers implements InventoryProvider {

    public static SmartInventory getInventory(Plot plot, PlotGroup group) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-members-near")
                .provider(new GuiNearPlayers(plot, group))
                .size(5, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.BLUE + ChatColor.BOLD + "Browsing Nearby Players")
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

        Collection<Player> nearPlayers = player.getLocation().getNearbyPlayers(64);

        Pagination pagination = contents.pagination();

        ClickableItem[] items = new ClickableItem[nearPlayers.size()];

        int i = 0;
        for(Player nearPlayer : nearPlayers){
            if(plot.isAssociated(nearPlayer))
                continue;

            items[i] = ClickableItem.of(new ItemFactory(Material.PLAYER_HEAD)
                            .setName(nearPlayer.getDisplayName())
                            .setOwningPlayer(nearPlayer)
                            .build(),
                    (e) -> ModalConfirmation.createModal("Are you sure?", new ItemFactory(Material.PLAYER_HEAD)
                                    .setName(ChatColor.WHITE + "Do you want to add " + ChatColor.GOLD + nearPlayer.getDisplayName() + ChatColor.RESET + ChatColor.WHITE + " to " + ChatColor.RESET + group.getColor() + group.getDisplayName() + ChatColor.RESET + ChatColor.WHITE + "?")
                                    .setOwningPlayer(nearPlayer)
                                    .build(),

                            ClickableItem.of(new ItemFactory(Material.LIME_CONCRETE).setName(ChatColor.GREEN + "Yes").build(), acceptClickEvent -> {
                                plot.getGroupsHolder().setGroup(nearPlayer, group);
                                plot.setLastModifiedAt(System.currentTimeMillis());
                                player.closeInventory();
                                contents.inventory().open(player);
                                DatabaseManager.savePlot(plot);
                            }),

                            ClickableItem.of(new ItemFactory(Material.RED_CONCRETE).setName(ChatColor.RED + "No").build(), declineClickEvent -> {
                                player.closeInventory();
                                contents.inventory().open(player);
                            })).open(player)
            );
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(25);

        pagination.addToIterator(
                new MultiLineSlotIterator(contents, contents.inventory(),
                        0, 0,
                        5, 5,
                        SlotIterator.Type.HORIZONTAL,
                        EnumIteratorXDirection.RIGHT, EnumIteratorYDirection.DOWN));

        contents.set(0, 6, ClickableItem.of(GuiMain.getPreviousPageItem(),
                e -> getInventory(plot, group).open(player, pagination.previous().getPage())));

        contents.set(4, 6, ClickableItem.of(GuiMain.getNextPageItem(),
                e -> getInventory(plot, group).open(player, pagination.next().getPage())));

        contents.fillColumn(5, ClickableItem.empty(GuiMain.getBorderItem()));
    }

}