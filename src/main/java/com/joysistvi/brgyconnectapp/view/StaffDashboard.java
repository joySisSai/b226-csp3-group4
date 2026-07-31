package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.model.User;

import java.util.Scanner;

public class StaffDashboard {
    private final Scanner scanner;

    public StaffDashboard(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show(User user) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            printHeader(user);
            ConsoleUI.printSubHeader("Staff Operations");
            ConsoleUI.printMenuOption("1", "Manage resident records");
            ConsoleUI.printMenuOption("2", "Manage household records");
            ConsoleUI.printMenuOption("3", "Search records");
            ConsoleUI.printMenuOption("4", "Create service request");
            ConsoleUI.printMenuOption("5", "Update request status");
            ConsoleUI.printMenuOption("6", "View request history");
            ConsoleUI.printMenuOption("7", "Generate reports");
            ConsoleUI.printMenuOption("0", "Log out");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");
            choice = scanner.nextLine().trim();

            if (!choice.equals("0")) {
                System.out.println();
                ConsoleUI.printInfo("This feature is not implemented yet.");
                System.out.println();
                ConsoleUI.printPrompt("Press Enter to continue...");
                scanner.nextLine();
            }
        } while (!choice.equals("0"));

        System.out.println();
        ConsoleUI.printSuccess("Logged out successfully.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }

    private void printHeader(User user) {
        ConsoleUI.printHeader("Staff Dashboard");
        System.out.println(ConsoleUI.CYAN + " Welcome, " + ConsoleUI.BOLD + user.getDisplayName() + "!" + ConsoleUI.RESET);
        System.out.println();
    }
}