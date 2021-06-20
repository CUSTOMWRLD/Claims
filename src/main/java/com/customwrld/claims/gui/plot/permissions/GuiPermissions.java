package com.customwrld.claims.gui.plot.permissions;

import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.permissions.PlotPermission;
import com.customwrld.claims.permissions.PlotPermissionCategory;
import com.customwrld.claims.permissions.PlotPermissionLevel;
import com.customwrld.claims.permissions.PermissionHolder;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.smartinvs.content.Pagination;
import com.customwrld.customlib.smartinvs.content.SlotIterator;
import com.customwrld.customlib.util.CC;
import com.customwrld.customlib.util.ItemFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class GuiPermissions implements InventoryProvider {

    @Getter private static final ItemStack permissionLevelHint = new ItemFactory(Material.PLAYER_HEAD)
            .setName(CC.RGB(0, 170, 255) + "Permission Levels")
            .setSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmM4ZWExZjUxZjI1M2ZmNTE0MmNhMTFhZTQ1MTkzYTRhZDhjM2FiNWU5YzZlZWM4YmE3YTRmY2I3YmFjNDAifX19",
                    "bc8ea1f51f253ff5142ca11ae45193a4ad8c3ab5e9c6eec8ba7a4fcb7bac40")
            .setLore(
                    ChatColor.GREEN + "Everyone" + ChatColor.DARK_GRAY + " — As the name suggests, everyone is granted",
                    ChatColor.DARK_GRAY + "this permission no matter what.",
                    ChatColor.GOLD + "Builders" + ChatColor.DARK_GRAY + " — Builders are players who are under \"supervision\".",
                    ChatColor.DARK_GRAY + "You need to both belong to this group AND either",
                    ChatColor.DARK_GRAY + "at least one trusted member or an owner needs to be online.",
                    ChatColor.AQUA + "Trusted" + ChatColor.DARK_GRAY + " — Trusted people are the \"supervisors\".",
                    ChatColor.DARK_GRAY + "You aren't restricted by anything else other than",
                    ChatColor.DARK_GRAY + "having to be in the said group.",
                    ChatColor.RED + "Owner" + ChatColor.DARK_GRAY + " — The only requirement is being an owner."
            ).build(); //TODO Use PermissionLevel and add a description field to that

    public static SmartInventory getInventory(Plot plot, PlotPermissionCategory category) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-perms")
                .provider(new GuiPermissions(plot, category))
                .size(6, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.BLUE + ChatColor.BOLD + "Plot Permissions - " + category.getDisplayName())
                .build();
    }

    private final Plot plot;
    private final PlotPermissionCategory category;

    //Items
    @Override
    public void init(Player player, InventoryContents contents) {

        contents.fillBorders(ClickableItem.empty(GuiMain.getBorderItem()));

        contents.set(0, 4, ClickableItem.empty(GuiMain.getPlotInformationItem(plot)));

        contents.set(2, 8, ClickableItem.empty(permissionLevelHint));

        var pagination = contents.pagination();

        PermissionHolder permissionHolder = plot.getPermissionsHolder();

        List<PlotPermission> permissions = category.getPermissions();
        var items = new ClickableItem[permissions.size()];

        var i = 0;
        for (PlotPermission permission : permissions) {
            setPermissionItem(pagination, contents, items, i, plot, permissionHolder, permission);
            i++;
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(7);

        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1));

        contents.set(3, 3, ClickableItem.of(GuiMain.getPreviousPageItem(),
                e -> getInventory(plot, category).open(player, pagination.previous().getPage())));

        contents.set(3, 5, ClickableItem.of(GuiMain.getNextPageItem(),
                e -> getInventory(plot, category).open(player, pagination.next().getPage())));

        contents.set(4, 4, ClickableItem.of(GuiMain.getBackItem(), e -> GuiPermissionsCategories.getInventory(plot).open(player)));

    }

    private static void setPermissionItem(Pagination pagination, InventoryContents contents, ClickableItem[] items, int pos, Plot plot, PermissionHolder permissionHolder, PlotPermission permission) {

        PlotPermissionLevel currentLevel = permissionHolder.getPermissionLevel(permission);

        List<String> lore = new ArrayList<>();
        lore.add("" + CC.RGB(128, 200, 255) + ChatColor.ITALIC + "Click to change!");
        lore.add("");
        for (PlotPermissionLevel level : PlotPermissionLevel.values()) {
            if (!level.equals(currentLevel)) {
                lore.add(ChatColor.GRAY + level.getDisplayName());
            } else {
                lore.add(level.getColor() + "➤ " + level.getDisplayName());
            }
        }

        items[pos] = ClickableItem.of(new ItemFactory(currentLevel.getMaterial()).setName("" + CC.RGB(0, 170, 255) + ChatColor.BOLD + permission.getDisplayName()).setLore(lore).build(),
                e -> {
                    permissionHolder.setPermissionLevel(permission, currentLevel.getNext());
                    plot.setLastModifiedAt(System.currentTimeMillis());
                    setPermissionItem(pagination, contents, items, pos, plot, permissionHolder, permission);
                    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1));
                }
        );

        pagination.setItems(items);

    }

}