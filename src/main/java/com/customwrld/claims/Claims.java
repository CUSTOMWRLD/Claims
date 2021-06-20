package com.customwrld.claims;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.customwrld.claims.commands.ClaimCommand;
import com.customwrld.claims.commands.DisplayCommand;
import com.customwrld.claims.managers.DatabaseManager;
import com.customwrld.claims.managers.PlotsManager;
import com.customwrld.claims.managers.PreviewManager;
import com.customwrld.claims.managers.SessionsManager;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class Claims extends JavaPlugin {
    public static final String permsPrefix = "customwrld.claims";
    public static SessionsManager sessionsManager;
    public static DatabaseManager databaseManager;
    public static PlotsManager plotsManager;
    public static PreviewManager previewManager;
    public static ProtocolManager protocolManager;
    private static Claims claims;

    public static Claims get() {
        return claims;
    }

    @Override
    public void onEnable() {
        claims = this;

        databaseManager = new DatabaseManager();
        databaseManager.init();

        protocolManager = ProtocolLibrary.getProtocolManager();
        if (protocolManager == null) {
            getLogger().severe("ProtocolLib couldn't be loaded, or isn't installed on the server!");
            this.setEnabled(false);
            return;
        }

        sessionsManager = new SessionsManager();
        sessionsManager.registerListeners();

        plotsManager = new PlotsManager();
        plotsManager.registerListeners();
        plotsManager.startPlotLoader();

//        previewManager = new PreviewManager();
//        previewManager.startTask();

        Arrays.asList(
                ClaimCommand.command(),
                DisplayCommand.command()
        ).forEach(CommandAPICommand::register);

        Bukkit.getPluginManager().registerEvents(new PluginListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
