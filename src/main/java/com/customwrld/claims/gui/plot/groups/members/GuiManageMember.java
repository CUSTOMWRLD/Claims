package com.customwrld.claims.gui.plot.groups.members;

import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.gui.plot.modals.ModalConfirmation;
import com.customwrld.claims.managers.DatabaseManager;
import com.customwrld.claims.permissions.groups.PlotGroup;
import com.customwrld.claims.permissions.groups.GroupHolder;
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
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class GuiManageMember implements InventoryProvider {
    public static SmartInventory getInventory(SmartInventory prevInventory, Plot plot, OfflinePlayer managedPlayer) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-member")
                .provider(new GuiManageMember(prevInventory, plot, managedPlayer))
                .size(6, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.DARK_GRAY + "Managing " + managedPlayer.getName())
                .build();
    }

    private final SmartInventory prevInventory;
    private final Plot plot;
    private final OfflinePlayer managedPlayer;

    //Items
    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillRow(0, ClickableItem.empty(GuiMain.getBorderItem()));
        contents.fillRow(1, ClickableItem.empty(new ItemFactory(Material.WHITE_STAINED_GLASS_PANE).setName("").build()));

        contents.set(0, 4, ClickableItem.empty(GuiMain.getPlotInformationItem(plot)));

        contents.set(5, 4, ClickableItem.of(GuiMain.getBackItem(),
                e -> {
                    contents.inventory().close(player);
                    prevInventory.open(player);
                })
        );

        contents.set(1, 4, ClickableItem.empty(
                new ItemFactory(Material.PLAYER_HEAD)
                        .setName(managedPlayer.getName())
                        .setOwningPlayer(managedPlayer)
                        .build()));

        GuiManageMember.setGroupItem(contents, plot, managedPlayer);

        contents.set(3, 7, ClickableItem.of(
                new ItemFactory(Material.BARRIER)
                        .setName(CC.RGB(0xFF0000) + "Banish")
                        .build(),
                e -> ModalConfirmation.createModal("Are you sure?",
                        new ItemFactory(Material.PLAYER_HEAD)
                                .setName(ChatColor.WHITE + "Do you want to remove " + ChatColor.GOLD + managedPlayer.getName() + ChatColor.RESET + ChatColor.WHITE + " from your plot?")
                                .setOwningPlayer(managedPlayer.getPlayer())
                                .build(),

                        ClickableItem.of(
                                new ItemFactory(Material.LIME_CONCRETE)
                                        .setName(ChatColor.GREEN + "Yes")
                                        .build(),
                                acceptClickEvent -> {
                                    plot.getGroupsHolder().kick(player);
                                    plot.setLastModifiedAt(System.currentTimeMillis());
                                    prevInventory.open(player);
                                    DatabaseManager.savePlot(plot);
                                }),

                        ClickableItem.of(
                                new ItemFactory(Material.RED_CONCRETE)
                                        .setName(ChatColor.RED + "No")
                                        .build(),
                                declineClickEvent -> contents.inventory().open(player))
                ).open(player)
        ));
    }

    private static void setGroupItem(InventoryContents contents, Plot plot, OfflinePlayer player){
        PlotGroup currentGroup = plot.getGroupsHolder().getGroup(player);

        List<String> lore = new ArrayList<>();
        lore.add(CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Click to change!");
        lore.add("");
        for (PlotGroup group : PlotGroup.values()) {
            if (!group.equals(currentGroup)) {
                lore.add(ChatColor.GRAY + group.getDisplayName());
            } else {
                lore.add(group.getColor() + "➤ " + group.getDisplayName());
            }
        }

        contents.set(3, 1, ClickableItem.of(
                new ItemFactory(currentGroup.getItem())
                        .setName(CC.RGB(0, 170, 255) + "Player's Group")
                        .setLore(lore)
                        .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build(),
                e -> {
                    GroupHolder groupHolder = plot.getGroupsHolder();
                    groupHolder.setGroup(player, groupHolder.getGroup(player).getNext());
                    plot.setLastModifiedAt(System.currentTimeMillis());
                    DatabaseManager.savePlot(plot);

                    GuiManageMember.setGroupItem(contents, plot, player);
                })
        );
    }

}
