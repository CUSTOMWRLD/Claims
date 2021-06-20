package com.customwrld.claims.permissions.groups;

import com.customwrld.customlib.smartinvs.content.SlotPos;
import com.customwrld.customlib.util.ItemFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
public enum PlotGroup {
    BUILDERS(ChatColor.GOLD, "Builders",  "builders", new ItemFactory(Material.PLAYER_HEAD)
            .setSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNhNGY5NGQ1ZjMyYjMxYzlmZmMyNjY4NDZkNmY1ZmFhNWJlZjRlMGEwZDk5NDIxZDNhY2IxOGZmOWQxN2EifX19",
                    "4ca4f94d5f32b31c9ffc266846d6f5faa5bef4e0a0d99421d3acb18ff9d17a").build(), new SlotPos(2, 3)),
    TRUSTED(ChatColor.AQUA, "Trusted",  "trusted", new ItemFactory(Material.PLAYER_HEAD)
            .setSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODJkYmQwNmY5YzkyN2RiNmM3OWZjOWY1ZWYyMzk3YWIwNjU0MTkxMzY0ZDBmNWM4OWY5MjRlNWVkOGQzYWYifX19",
                    "82dbd06f9c927db6c79fc9f5ef2397ab0654191364d0f5c89f924e5ed8d3af").build(), new SlotPos(2, 5));

    private final ChatColor color;
    private final String displayName;
    private final String id;
    private final ItemStack item;
    private final SlotPos slot;

    public static PlotGroup byId(String id) {
        for (PlotGroup group : PlotGroup.values()) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    public PlotGroup getNext() {
        return PlotGroup.values()[(this.ordinal() + 1) % PlotGroup.values().length];
    }
}
