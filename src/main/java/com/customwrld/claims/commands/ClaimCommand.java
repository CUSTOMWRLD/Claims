package com.customwrld.claims.commands;

import com.customwrld.claims.gui.GuiMain;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;

public class ClaimCommand {

    public static CommandAPICommand command() {
        return new CommandAPICommand("claim")
                .withPermission("claims.claim")
                .executesPlayer((sender, args) -> {
                    GuiMain.INVENTORY.open((Player) sender);
                });
    }

}
