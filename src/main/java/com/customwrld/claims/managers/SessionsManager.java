package com.customwrld.claims.managers;

import com.comphenix.protocol.ProtocolManager;
import com.customwrld.claims.ClaimingSession;
import com.customwrld.claims.Claims;
import com.customwrld.claims.Plot;
import com.customwrld.claims.i18n;
import com.customwrld.claims.managers.listeners.bukkit.WandListeners;
import com.customwrld.claims.managers.listeners.packets.PermanentGhostBlocks;
import com.customwrld.claims.utils.BalanceUtils;
import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.customlib.util.MetadataUtil;
import com.customwrld.flame.profile.Profile;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SessionsManager {

    private final Set<ClaimingSession> claimingSessions = new HashSet<>();
    private final Map<Chunk, Set<ClaimingSession>> BY_CHUNK = new HashMap<>();
    //TODO Implement map BY_CHUNK
    public static final String METADATA_KEY = "claimingSession";

    //Utils
    public static boolean isClaiming(HumanEntity player) {
        return player.hasMetadata(METADATA_KEY);
    }

    public static ClaimingSession getClaimingSession(HumanEntity player) {
        return MetadataUtil.getValue(player, METADATA_KEY, Claims.get(), null, ClaimingSession.class, false);
    }

    public static Set<ClaimingSession> getClaimingSessions() {
        return Claims.sessionsManager.claimingSessions;
    }

    public boolean startClaimingSession(InventoryClickEvent event) {
        Player player = (Player) event.getViewers().get(0);

        //TODO Change title and lore
        if (player.hasPermission("minetale.claims.claim")) {

            int slot = player.getInventory().firstEmpty();
            if(slot > 8) {
                i18n.EN.NO_AVAILABLE_SLOT.getMessage().forEach(player::sendMessage);
                return false;
            }

            ItemStack i = createSessionWand(player, slot);

            ClaimingSession session = new ClaimingSession(player, i, slot);
            claimingSessions.add(session);

            player.setMetadata(METADATA_KEY, new FixedMetadataValue(Claims.get(), session));

            i18n.EN.GIVE_WAND.getMessage().forEach(player::sendMessage);

            PreviewManager.enableAutoPreview(player);

            return true;
        }

        return false;
    }

    public void confirmClaimingSession(ClaimingSession session) {

        Player owner = session.getOwner();
        var profile = Profile.getByUuid(owner.getUniqueId());

        PlotsManager plotsManager = Claims.plotsManager;

        Location firstCorner = session.getFirstCorner();
        Location secondCorner = session.getSecondCorner();

        int sizeX = Math.abs(firstCorner.getBlockX() - secondCorner.getBlockX()) + 1;
        int sizeZ = Math.abs(firstCorner.getBlockZ() - secondCorner.getBlockZ()) + 1;

        //Minimum of 6x6 selections
        if (sizeX < 6 || sizeZ < 6) {
            i18n.EN.SELECTION_TOO_SMALL.getMessage().forEach(owner::sendMessage);
            return;
        }

        //There can't be any other plots at the same spot
        for (Plot plot : plotsManager.getPlotsIntersectingSession(session)) {
            if (!owner.getUniqueId().equals(plot.getOwner().getUniqueId())) {
                i18n.EN.PLOT_IN_WAY.getMessage().forEach(owner::sendMessage);
                return;
            }
        }

        int totalBlocks = sizeX * sizeZ;

        if (!BalanceUtils.canAffordWithBlocks(profile, totalBlocks)) {
            i18n.EN.NOT_ENOUGH_BLOCKS.getMessage().forEach(owner::sendMessage);
            return;
        }

        BalanceUtils.removeBlocks(profile, totalBlocks);

        plotsManager.convertSession(session);

        i18n.EN.CONFIRMED_SELECTION.getMessage().forEach(owner::sendMessage);

        removeSessionWand(session);

        session.reset();

        owner.playSound(owner.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1f, 1f);

        removeClaimingSession(session);

    }

    public void cancelClaimingSession(ClaimingSession session, boolean silent) {

        if(!silent) {
            Player owner = session.getOwner();
            i18n.EN.CANCELLED_SESSION.getMessage().forEach(owner::sendMessage);
        }

        removeSessionWand(session);

        session.reset();

        removeClaimingSession(session);

    }

    public void removeClaimingSession(ClaimingSession session) {
        claimingSessions.remove(session);

        Player owner = session.getOwner();
        owner.removeMetadata(METADATA_KEY, Claims.get());

        PreviewManager.disableAutoPreview(owner);
    }

    public ItemStack createSessionWand(Player player, int slot) {
        ItemStack i = new ItemFactory(Material.GOLDEN_HOE)
                .setName("Claiming Wand")
                //.setLore("AAAAAAAAAAAA")
                .addEnchantment(Enchantment.getByKey(NamespacedKey.minecraft("glow")))
                .build();

        PlayerInventory inventory = player.getInventory();
        inventory.setItem(slot, i);
        inventory.setHeldItemSlot(slot);

        return i;
    }

    public void removeSessionWand(ClaimingSession session) {
        Player owner = session.getOwner();

        ItemStack wand = session.getWand();

        owner.getInventory().remove(wand);

        wand.setAmount(0);
        wand.setType(Material.AIR);
    }

    //Events
    public void registerListeners() {
        //Event Listeners
        Bukkit.getPluginManager().registerEvents(new WandListeners.WandActions(), Claims.get());
        Bukkit.getPluginManager().registerEvents(new WandListeners.WandEvents(), Claims.get());

        //Packet Listeners
        ProtocolManager protocolManager = Claims.protocolManager;

        protocolManager.addPacketListener(new PermanentGhostBlocks());
    }
}
