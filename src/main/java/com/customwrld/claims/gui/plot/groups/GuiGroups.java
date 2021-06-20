package com.customwrld.claims.gui.plot.groups;

import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.gui.plot.GuiManagePlot;
import com.customwrld.claims.permissions.groups.PlotGroup;
import com.customwrld.claims.utils.ChatUtils;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.smartinvs.content.SlotPos;
import com.customwrld.customlib.util.CC;
import com.customwrld.customlib.util.ItemFactory;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class GuiGroups implements InventoryProvider {

    public static SmartInventory getInventory(Plot plot) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-groups")
                .provider(new GuiGroups(plot))
                .size(5, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.BLUE + ChatColor.BOLD + "Browsing Groups")
                .build();
    }

    private final Plot plot;

    //Items
    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(GuiMain.getBorderItem()));

        contents.set(0, 4, ClickableItem.empty(GuiMain.getPlotInformationItem(plot)));

        contents.set(3, 4, ClickableItem.of(GuiMain.getBackItem(), e -> GuiManagePlot.getInventory(plot).open(player)));

        for (PlotGroup group : PlotGroup.values()) {
            List<String> lore = new ArrayList<>();
            lore.add("" + CC.RGB(128, 200, 255) + ChatColor.ITALIC + "Click to change!");
            lore.add("");
            lore.add("" + CC.RGB(0, 170, 255) + ChatColor.BOLD + "Members:");

            SlotPos slot = group.getSlot();

            Set<OfflinePlayer> members = plot.getGroupsHolder().getMembers(group);

            var membersLore = ChatUtils.getGroupMembersLore(members);
            if(!membersLore.isEmpty())
                lore.addAll(membersLore);

            int total = members.size();
            long online = members.stream().filter(OfflinePlayer::isOnline).count();

            var groupString = group.getColor().toString() + ChatColor.BOLD + group.getDisplayName();
            var membersString = ChatColor.WHITE + "(" + online + "/" + total + ")";

            var name = groupString + ChatColor.RESET + " " + membersString;

            contents.set(slot.getRow(), slot.getColumn(), ClickableItem.of(
                    new ItemFactory(group.getItem())
                            .setName(name)
                            .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                            .setLore(lore)
                            .build(),
                    e -> GuiManageGroup.getInventory(plot, group).open(player)
            ));
        }
    }

}