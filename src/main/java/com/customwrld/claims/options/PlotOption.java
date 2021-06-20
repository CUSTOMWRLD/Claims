package com.customwrld.claims.options;

import com.customwrld.customlib.smartinvs.content.SlotPos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum PlotOption {

    PVP("PvP",
            "Allows players to attack eachother.",
            "pvp",
            false,
            new SlotPos(1, 0),
            Material.DIAMOND_SWORD),

    EXPLOSION_GRIEF("Explosion Griefing",
            "Allows for explosions to break blocks, such as TNT, Fireballs, etc.",
            "explosion_grief",
            false,
            new SlotPos(1, 2),
            Material.TNT),

    MOB_SPAWN("Mob Spawning",
            "Allows mobs to spawn.",
            "mob_spawn",
            true,
            new SlotPos(2, 3),
            Material.ZOMBIE_HEAD),

    MOB_GRIEF("Mob Griefing",
            "Allows mobs to grief your plot, e.g. endermen stealing blocks.",
            "mob_grief",
            false,
            new SlotPos(1, 3),
            Material.CREEPER_HEAD),

    GRASS_SPREAD("Grass Spread",
            "Allows grass spreading inside your plot.",
            "grass_spread",
            true,
            new SlotPos(1, 5),
            Material.GRASS_BLOCK),

    MYCELIUM_SPREAD("Mycelium Spread",
            "Allows mycelium spreading inside your plot.",
            "mycelium_spread",
            true,
            new SlotPos(2, 5),
            Material.MYCELIUM),

    LEAF_DECAY("Leaf Decay",
            "Allows leaves to decay (disappear after some time).",
            "leaf_decay",
            true,
            new SlotPos(2, 0),
            Material.OAK_LEAVES),

    GROW_FROM_OUTSIDE("Trees from Outside",
            "Allows saplings and mushrooms from outside to grow into your plot.",
            "grow_from_outside",
            false,
            new SlotPos(1, 7),
            Material.OAK_SAPLING),

    PISTON_FROM_OUTSIDE("Pistons from Outside",
            "Allows pistons from outside to push blocks into your plot.",
            "piston_from_outside",
            false,
            new SlotPos(1, 8),
            Material.PISTON),

    FLUIDS_FROM_OUTSIDE("Fluids from Outside",
            "Allows fluids to flow from outside to your plot.",
            "fluids_from_outside",
            false,
            new SlotPos(2, 7),
            Material.WATER_BUCKET),

    DISPENSERS_FROM_OUTSIDE("Dispensers from Outside",
            "Allows dispensers from outside to dispense into your plot.",
            "dispensers_from_outside",
            false,
            new SlotPos(2, 8),
            Material.DISPENSER);

    private final String displayName;
    private final String description;
    private final String id;
    private final Boolean defaultValue;
    private final SlotPos position;
    private final Material material;

    public static PlotOption byId(String id) {
        for (PlotOption perm : PlotOption.values()) {
            if (perm.getId().equals(id)) {
                return perm;
            }
        }
        return null;
    }

}
