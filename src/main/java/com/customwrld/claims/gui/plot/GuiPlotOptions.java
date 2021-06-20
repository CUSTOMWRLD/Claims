package com.customwrld.claims.gui.plot;

import com.customwrld.claims.Plot;
import com.customwrld.claims.gui.GuiMain;
import com.customwrld.claims.managers.DatabaseManager;
import com.customwrld.claims.options.OptionsHolder;
import com.customwrld.claims.options.PlotOption;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.util.CC;
import com.customwrld.customlib.util.ItemFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class GuiPlotOptions implements InventoryProvider {

    @Getter private static final ItemStack renameItem = new ItemFactory(Material.NAME_TAG)
            .setName(CC.RGB(0, 170, 255) + "Rename")
            .setLore(CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + "Click to change!").build();

    @Getter private static final ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

    @Getter private static final Map<Player, Plot> renamingRequired = new HashMap<>();

    public static SmartInventory getInventory(Plot plot) {
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-options")
                .provider(new GuiPlotOptions(plot))
                .size(5, 9)
                .title("" + ChatColor.GOLD + ChatColor.BOLD + "》" + ChatColor.DARK_GRAY + "Editing Plot's Options")
                .build();
    }

    private final Plot plot;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillRow(0, ClickableItem.empty(GuiMain.getBorderItem()));
        contents.fillRow(3, ClickableItem.empty(separator));

        contents.set(0, 4, ClickableItem.empty(GuiMain.getPlotInformationItem(plot)));

        contents.set(4, 8, ClickableItem.of(GuiMain.getBackItem(), e -> GuiManagePlot.getInventory(plot).open(player)));

        for(Map.Entry<PlotOption, Boolean> ent : plot.getOptionsHolder().getOptions().entrySet()) {
            setOptionItem(contents, ent.getKey());
        }

        contents.set(4, 0, ClickableItem.of(renameItem,
                e -> {
                    contents.inventory().close(player);
                    GuiPlotOptions.getRenamingRequired().put(player, plot);
                    player.sendMessage(CC.RGB(0x33B859) + "Please type the new name for your plot:");
                }
        ));
    }

    private void setOptionItem(InventoryContents contents, PlotOption option) {
        OptionsHolder optionsHolder = plot.getOptionsHolder();

        contents.set(option.getPosition(), ClickableItem.of(
                new ItemFactory(option.getMaterial())
                        .setName(CC.RGB(0, 170, 255) + option.getDisplayName())
                        .setLore(
                                CC.RGB(128, 200, 255).toString() + ChatColor.ITALIC + option.getDescription(),
                                CC.RGB(128, 200, 255) + "Status: " + (optionsHolder.getOptionState(option) ?
                                        ChatColor.GREEN.toString() + ChatColor.BOLD + "Enabled" :
                                        ChatColor.RED.toString() + ChatColor.BOLD + "Disabled")
                        )
                        .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build(),
                e -> {
                    optionsHolder.setOption(option, !optionsHolder.getOptionState(option));
                    plot.setLastModifiedAt(System.currentTimeMillis());
                    DatabaseManager.savePlot(plot);
                    setOptionItem(contents, option);
                }));
    }

}
