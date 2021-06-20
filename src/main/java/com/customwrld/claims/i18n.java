package com.customwrld.claims;

import com.customwrld.customlib.util.CC;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class i18n {
    //Universal
    public static String separator = CC.gradient("                                                                                 ",
            ChatColor.STRIKETHROUGH.toString(),
            new Color(0, 170, 255),
            new Color(128, 200, 255));

    public enum EN implements Language {
        GIVE_WAND(Arrays.asList(separator,
                CC.RGB(128, 200, 255).toString() + ChatColor.BOLD + "  You've been given the claiming wand!",
                "   Select your first corner using either LMB or RMB on a block.",
                "   To make a select a region you need to select two corners.",
                "",
                "",
                CC.RGB(0, 255, 0) + "   \u258d Confirm Selection  -> Crouch + RMB",
                CC.RGB(0, 255, 255) + "   \u258d Change Mode      " + ChatColor.BOLD + " " + ChatColor.RESET + CC.RGB(0, 255, 255) + "-> Crouch + LMB",
                CC.RGB(255, 0, 0) + "   \u258d Cancel               -> 2x Drop Wand",
                separator
        ), false, null),
        NO_AVAILABLE_SLOT(Arrays.asList(separator,
                CC.RGB(255, 0, 0).toString() + ChatColor.BOLD + "  You don't have a free slot on your hotbar!",
                "   Make sure you have one available and try again.",
                separator
        ), false, null),
        CONFIRMED_SELECTION(Arrays.asList(separator,
                CC.RGB(128, 200, 255).toString() + ChatColor.BOLD + "  You claimed your selection!",
                separator
        ), false, null),
        SELECTION_TOO_SMALL(Arrays.asList(separator,
                CC.RGB(255, 0, 0).toString() + ChatColor.BOLD + "  Unable to process your selection!",
                "   Your selection is too small. It should be at least 6x6.",
                separator
        ), false, null),
        PLOT_IN_WAY(Arrays.asList(separator,
                CC.RGB(255, 0, 0).toString() + ChatColor.BOLD + "  Unable to process your selection!",
                "   There is someone else's plot in the way.",
                separator
        ), false, null),
        NOT_ENOUGH_SPACE(Arrays.asList(separator,
                CC.RGB(255, 0, 0).toString() + ChatColor.BOLD + "  Unable to process your selection!",
                "   You can't make any new plots.",
                separator
        ), false, null),
        NOT_ENOUGH_BLOCKS(Arrays.asList(separator,
                CC.RGB(255, 0, 0).toString() + ChatColor.BOLD + "  Unable to process your selection!",
                "   You don't have enough claimable blocks.",
                separator
        ), false, null),
        CANCEL_WARNING(Arrays.asList(separator,
                CC.RGB(255, 0, 0).toString() + ChatColor.BOLD + "  Watch out!",
                ChatColor.RED + "   Dropping the wand again will cancel your selection!",
                "   If you don't want the claiming wand, you can drop it again.",
                separator
        ), false, null),
        CANCELLED_SESSION(Arrays.asList(separator,
                CC.RGB(128, 200, 255).toString() + ChatColor.BOLD + "  Your wand has been taken away!",
                "   You can get it again from the claims menu.",
                separator
        ), false, null),
        SELECTED_REGION(null, true, (data) -> {
            int sizeX = (int) data.getOrDefault("sizeX", 0);
            int sizeZ = (int) data.getOrDefault("sizeZ", 0);
            return Arrays.asList(separator,
                    CC.RGB(128, 200, 255).toString() + ChatColor.BOLD + "  You've selected a region!",
                    "   You can claim this selection, but you can still change it.",
                    "   After confirming the selection, you'll have 20 blocks left.",
                    "",
                    "   " + sizeX + "x" + sizeZ + " \u258d Total: " + sizeX * sizeZ,
                    "",
                    CC.RGB(0, 255, 0) + "   \u258d Confirm Selection -> Crouch + RMB",
                    CC.RGB(255, 0, 0) + "   \u258d Cancel              -> 2x Drop Wand",
                    separator
            );
        }),
        RESELECTED_REGION(null, true, (data) -> {
            int sizeX = (int) data.getOrDefault("sizeX", 0);
            int sizeZ = (int) data.getOrDefault("sizeZ", 0);
            return Arrays.asList(separator,
                    CC.RGB(128, 200, 255).toString() + ChatColor.BOLD + "  You've reselected a region!",
                    "   You can claim this selection, but you can still change it.",
                    "   After confirming the selection, you'll have 20 blocks left.",
                    "",
                    "   " + sizeX + "x" + sizeZ + " \u258d Total: " + sizeX * sizeZ,
                    "",
                    CC.RGB(0, 255, 0) + "   \u258d Confirm Selection -> Crouch + RMB",
                    CC.RGB(255, 0, 0) + "   \u258d Cancel              -> 2x Drop Wand",
                    separator
            );
        }),
        OPERATION_CHANGE(null, true, (data) -> {
            WandAction operation = (WandAction) data.get("operation");
            String operationString = "";
            String operationDesc = null;
            switch (operation) {
                case ADD:
                    operationString = CC.RGB(0, 255, 0) + "ADD";
                    operationDesc = "" + ChatColor.GRAY + ChatColor.ITALIC + "You can create new plots, extend plots,\n   or merge plots into one.";
                    break;
                case CUT:
                    operationString = CC.RGB(255, 0, 60) + "CUT";
                    operationDesc = "" + ChatColor.GRAY + ChatColor.ITALIC + "You can cut plots, but watch out!\n   " + CC.RGB(255, 80, 120) + ChatColor.ITALIC + "You can also remove plots by making a selection over them!";
            }
            return Arrays.asList(separator,
                    CC.RGB(0, 255, 255).toString() + ChatColor.BOLD + "  You've changed your mode to",
                    "   " + operationString,
                    "",
                    "   " + operationDesc,
                    separator
            );
        });

        private final boolean isDynamic; //Can the message change?
        private List<String> msg;
        private Function<Map<String, Object>, List<String>> executor; //If it can change, what it should be changed with?

        EN(final List<String> msg, final boolean isDynamic, final Function<Map<String, Object>, List<String>> executor) {
            this.isDynamic = isDynamic;

            this.msg = null;
            this.executor = null;
            if (!isDynamic) {
                this.msg = msg;
            } else {
                this.executor = executor;
            }
        }

        public static String getLanguageCode() {
            return "EN";
        }

        public static String getDisplayName() {
            return "English";
        }

        public List<String> getMessage() {
            if (!this.isDynamic) {
                return this.msg;
            }
            return Collections.emptyList();
        }

        public List<String> getMessage(Map<String, Object> data) {
            if (this.isDynamic) {
                return this.executor.apply(data);
            }
            return Collections.emptyList();
        }
    }

    public interface Language {
        static String getLanguageCode() {
            return null;
        }

        static String getDisplayName() {
            return null;
        }
    }
}
