package com.customwrld.claims.gui.plot.permissions;

import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.gui.plot.GuiManagePlot;
import com.customwrld.claims.permissions.PlotPermission;
import com.customwrld.claims.permissions.PlotPermissionCategory;
import com.customwrld.claims.permissions.PlotPermissionLevel;
import com.customwrld.claims.permissions.PermissionHolder;
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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class GuiPermissionsCategories implements InventoryProvider {

    public static SmartInventory getInventory(Plot plot) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-perms-categories")
                .provider(new GuiPermissionsCategories(plot))
                .size(5, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.BLUE + ChatColor.BOLD + "Plot Permissions")
                .build();
    }

    private final Plot plot;

    //Items
    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(GuiMain.getBorderItem()));

        contents.set(0, 4, ClickableItem.empty(GuiMain.getPlotInformationItem(plot)));

        contents.set(2, 8, ClickableItem.empty(GuiPermissions.getPermissionLevelHint()));

        contents.set(3, 4, ClickableItem.of(GuiMain.getBackItem(), e -> GuiManagePlot.getInventory(plot).open(player)));

        PermissionHolder permissionHolder = plot.getPermissionsHolder();

        for (PlotPermissionCategory category : PlotPermissionCategory.values()) {
            SlotPos slot = category.getSlot();

            List<String> lore = new ArrayList<>();
            lore.add("" + CC.RGB(128, 200, 255) + ChatColor.ITALIC + "Click to change!");
            lore.add("");

            for (PlotPermission permission : category.getPermissions()) {
                PlotPermissionLevel level = permissionHolder.getPermissionLevel(permission);
                lore.add(ChatColor.WHITE + permission.getDisplayName() + ": " + level.getColor() + level.getDisplayName());
            }

            contents.set(slot.getRow(), slot.getColumn(), ClickableItem.of(
                    new ItemFactory(category.getMaterial())
                            .setName(CC.RGB(0, 170, 255) + category.getDisplayName())
                            .setLore(lore)
                            .build(),
                    e -> GuiPermissions.getInventory(plot, category).open(player)
            ));
        }
    }

}
