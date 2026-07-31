package com.joysistvi.brgyconnectapp.view;

public class ConsoleUI {
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String CYAN = "\033[36m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String RED = "\033[31m";
    public static final String BLUE = "\033[34m";

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void resetTerminal() {
        System.out.print("\033[0m");
        System.out.flush();
    }

    public static void printMainBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("  ======================================================================");
        System.out.println("                                                                        ");
        System.out.println("  ██████╗  █████╗ ██████╗  █████╗ ███╗   ██╗ ██████╗  █████╗ ██╗   ██╗  ");
        System.out.println("  ██╔══██╗██╔══██╗██╔══██╗██╔══██╗████╗  ██║██╔════╝ ██╔══██╗╚██╗ ██╔╝  ");
        System.out.println("  ██████╔╝███████║██████╔╝███████║██╔██╗ ██║██║  ███╗███████║ ╚████╔╝   ");
        System.out.println("  ██╔══██╗██╔══██║██╔══██╗██╔══██║██║╚██╗██║██║   ██║██╔══██║  ╚██╔╝    ");
        System.out.println("  ██████╔╝██║  ██║██║  ██║██║  ██║██║ ╚████║╚██████╔╝██║  ██║   ██║     ");
        System.out.println("  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝     ");
        System.out.println("                                                                        ");
        System.out.println("               M A N A G E M E N T   S Y S T E M                        ");
        System.out.println("                                                                        ");
        System.out.println("  ======================================================================");
        System.out.println(RESET);
    }

    public static void printHeader(String title) {
        int width = 50;
        System.out.println(CYAN + BOLD + "╔" + "═".repeat(width - 2) + "╗" + RESET);
        int paddingLeft = (width - 2 - title.length()) / 2;
        int paddingRight = width - 2 - title.length() - paddingLeft;
        System.out.println(CYAN + BOLD + "║" + " ".repeat(Math.max(0, paddingLeft)) + title + " ".repeat(Math.max(0, paddingRight)) + "║" + RESET);
        System.out.println(CYAN + BOLD + "╚" + "═".repeat(width - 2) + "╝" + RESET);
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
