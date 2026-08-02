package com.joysistvi.brgyconnectapp.view;

public class ConsoleUI {
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String CYAN = "\033[36m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String RED = "\033[31m";
    public static final String BLUE = "\033[34m";
    public static final String WHITE = "\033[97m";
    public static final String BG_BLACK = "\033[40m";

    public static void clearScreen() {
        System.out.print(BG_BLACK + "\033[H\033[2J");
        System.out.flush();
    }

    public static void resetTerminal() {
        System.out.print("\033[0m" + BG_BLACK);
        System.out.flush();
    }

    public static void printMainBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("  ======================================================================");
        System.out.println("                                                                        ");
        System.out.println(BLUE + "  ██████╗  █████╗ ██████╗  █████╗ ███╗   ██╗ ██████╗  █████╗ ██╗   ██╗  ");
        System.out.println("  ██╔══██╗██╔══██╗██╔══██╗██╔══██╗████╗  ██║██╔════╝ ██╔══██╗╚██╗ ██╔╝  ");
        System.out.println("  ██████╔╝███████║██████╔╝███████║██╔██╗ ██║██║  ███╗███████║ ╚████╔╝   ");
        System.out.println(CYAN + "  ██╔══██╗██╔══██║██╔══██╗██╔══██║██║╚██╗██║██║   ██║██╔══██║  ╚██╔╝    ");
        System.out.println("  ██████╔╝██║  ██║██║  ██║██║  ██║██║ ╚████║╚██████╔╝██║  ██║   ██║     ");
        System.out.println("  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝     ");
        System.out.println("                                                                        ");
        System.out.println(WHITE + "               M A N A G E M E N T   S Y S T E M                        ");
        System.out.println("                                                                        ");
        System.out.println(CYAN + "  ======================================================================");
        System.out.println(RESET + BG_BLACK);
    }

    public static void printHeader(String title) {
        int width = 50;
        System.out.println(BLUE + "╔" + "═".repeat(width - 2) + "╗" + RESET + BG_BLACK);
        int paddingLeft = (width - 2 - title.length()) / 2;
        int paddingRight = width - 2 - title.length() - paddingLeft;
        System.out.println(BLUE + "║" + " ".repeat(Math.max(0, paddingLeft)) + CYAN + BOLD + title + RESET + BG_BLACK + " ".repeat(Math.max(0, paddingRight)) + BLUE + "║" + RESET + BG_BLACK);
        System.out.println(BLUE + "╚" + "═".repeat(width - 2) + "╝" + RESET + BG_BLACK);
    }

    public static void printSubHeader(String title) {
        System.out.println(YELLOW + " ── " + title + " ──" + RESET);
    }

    public static void printMenuOption(String key, String description) {
        if (key.equals("0")) {
            System.out.println(RED + " [" + key + "] " + RESET + description);
        } else {
            System.out.println(GREEN + " [" + key + "] " + RESET + description);
        }
    }

    public static void printPrompt(String prompt) {
        System.out.print(CYAN + BOLD + " » " + RESET + prompt);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + " ✔ " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + " ✘ " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(BLUE + " ℹ " + message + RESET);
    }
}
