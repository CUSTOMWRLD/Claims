package com.customwrld.claims.gui.plot.modals;

import com.customwrld.claims.gui.GuiMain;
import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.util.ItemFactory;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class ModalConfirmation implements InventoryProvider {
    public static SmartInventory createModal(String title, ItemStack info,
                                             ClickableItem confirm,
                                             ClickableItem decline){
        return SmartInventory.builder().manager(CustomLib.getCustomLib().getInvManager())
                .id("claims-modal-confirmation")
                .provider(new ModalConfirmation(title, info, confirm, decline))
                .size(5, 9)
                .title(title)
                .build();
    }

    private final String title;
    private final ItemStack info;

    private final ClickableItem confirm;
    private final ClickableItem decline;

    boolean cur;
    int i;

    public void init(Player player, InventoryContents contents) {
        cur = false;
        i = 0;

        contents.fillBorders(ClickableItem.empty(GuiMain.getBorderItem()));

        contents.set(1, 4, ClickableItem.empty(info));
        contents.set(3, 3, confirm);
        contents.set(3, 5, decline);
    }


    public void update(Player player, InventoryContents contents) {
        if(i >= 20){
            cur = !cur;
            i = 0;

            contents.fillBorders(ClickableItem.empty(new ItemFactory(cur ? Material.LIGHT_BLUE_STAINED_GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE).setName("").build()));
        }
        i++;
    }
}
