package com.customwrld.claims.commands;

import com.customwrld.claims.managers.PreviewManager;
import dev.jorel.commandapi.CommandAPICommand;

public class DisplayCommand {

    public static CommandAPICommand command() {
        return new CommandAPICommand("display")
                .withPermission("claims.display")
                .executesPlayer((sender, args) -> {
                    PreviewManager.toggleAutoPreview(sender);
                });
    }

}
