package com.customwrld.claims.gui.plot;

import com.customwrld.claims.Claims;
import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.gui.GuiPlots;
import com.customwrld.claims.gui.plot.groups.GuiGroups;
import com.customwrld.claims.gui.plot.modals.ModalConfirmation;
import com.customwrld.claims.gui.plot.permissions.GuiPermissionsCategories;
import com.customwrld.claims.managers.DatabaseManager;
import com.customwrld.claims.permissions.PermissionHolder;
import com.customwrld.claims.permissions.PlotPermission;
import com.customwrld.claims.permissions.PlotPermissionCategory;
import com.customwrld.claims.permissions.PlotPermissionLevel;
import com.customwrld.claims.permissions.groups.GroupHolder;
import com.customwrld.claims.permissions.groups.PlotGroup;
import com.customwrld.claims.utils.ChatUtils;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class GuiManagePlot implements InventoryProvider {

    public static SmartInventory getInventory(Plot plot) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-manage")
                .provider(new GuiManagePlot(plot))
                .size(4, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.DARK_GRAY + "Managing a Plot")
                .build();
    }

    private final Plot plot;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillRow(0, ClickableItem.empty(GuiMain.getBorderItem()));

        contents.set(0, 4, ClickableItem.empty(GuiMain.getPlotInformationItem(plot)));

        contents.set(3, 4, ClickableItem.of(GuiMain.getBackItem(), e -> GuiPlots.INVENTORY.open(player)));

        PermissionHolder permissionHolder = plot.getPermissionsHolder();

        List<String> lore = new ArrayList<>();
        lore.add(CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Click to edit!");
        lore.add("");
        for (var i = 0; i < PlotPermissionCategory.values().length; i++) {
            PlotPermissionCategory category = PlotPermissionCategory.values()[i];

            lore.add(CC.RGB(130, 230, 255).toString() + ChatColor.STRIKETHROUGH + "                                             ");
            lore.add(CC.RGB(0, 170, 255).toString() + ChatColor.BOLD + category.getDisplayName() + ":");
            lore.add("");
            for (PlotPermission permission : category.getPermissions()) {
                PlotPermissionLevel level = permissionHolder.getPermissionLevel(permission);
                lore.add(ChatColor.WHITE + permission.getDisplayName() + ": " + level.getColor() + level.getDisplayName());
            }
        }

        contents.set(2, 1, ClickableItem.of(
                new ItemFactory(Material.WRITABLE_BOOK)
                        .setName(CC.RGB(0, 170, 255) + "Permissions")
                        .setLore(lore)
                        .build(),
                e -> GuiPermissionsCategories.getInventory(plot).open(player)));

        GroupHolder groupHolder = plot.getGroupsHolder();

        lore = new ArrayList<>();
        lore.add(CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Click to edit!");
        lore.add("");
        for (var i = 0; i < PlotGroup.values().length; i++) {
            PlotGroup group = PlotGroup.values()[i];

            Set<OfflinePlayer> members = groupHolder.getMembers(group);

            lore.add(CC.RGB(130, 230, 255).toString() + ChatColor.STRIKETHROUGH + "                                             ");

            int total = members.size();
            long online = members.stream().filter(OfflinePlayer::isOnline).count();

            var groupString = group.getColor().toString() + ChatColor.BOLD + group.getDisplayName();
            var membersString = ChatColor.WHITE + "(" + online + "/" + total + ")";

            lore.add(groupString + ChatColor.RESET + " " + membersString);

            var membersLore = ChatUtils.getGroupMembersLore(members);
            if(!membersLore.isEmpty())
                lore.addAll(membersLore);
        }

        contents.set(2, 3, ClickableItem.of(
                new ItemFactory(Material.KNOWLEDGE_BOOK)
                        .setName(CC.RGB(0, 170, 255) + "Groups")
                        .setLore(lore)
                        .build(),
                e -> GuiGroups.getInventory(plot).open(player)
        ));

        contents.set(2, 5, ClickableItem.of(
                new ItemFactory(Material.NETHER_STAR)
                        .setName(CC.RGB(0, 170, 255) + "Options")
                        .setLore(CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Click to change!")
                        .build(),
                e -> GuiPlotOptions.getInventory(plot).open(player)));

        contents.set(2, 7, ClickableItem.of(
                new ItemFactory(Material.BARRIER)
                        .setName(CC.RGB(0xFF0000) + "Remove")
                        .setLore(ChatColor.RED.toString() + ChatColor.ITALIC + "Click to remove the plot.")
                        .build(),
                e -> ModalConfirmation.createModal("Are you sure?",
                        new ItemFactory(Material.NOTE_BLOCK)
                                .setName(ChatColor.WHITE + "Do you want to remove plot \"" + plot.getName() + "\"?")
                                .build(),

                        ClickableItem.of(
                                new ItemFactory(Material.LIME_CONCRETE)
                                        .setName(ChatColor.GREEN + "Yes")
                                        .build(),
                                acceptClickEvent -> {
                                    DatabaseManager.deletePlot(plot);
                                    Claims.plotsManager.removePlot(plot);
                                    GuiPlots.INVENTORY.open(player);
                                }),

                        ClickableItem.of(
                                new ItemFactory(Material.RED_CONCRETE)
                                        .setName(ChatColor.RED + "No")
                                        .build(),
                                declineClickEvent -> GuiManagePlot.getInventory(plot).open(player))
                ).open(player)
        ));
    }

}
