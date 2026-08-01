package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.model.User;

import java.util.Scanner;

public class StaffDashboard {
    private final Scanner scanner;
    private final ResidentManagementView residentManagementView;
    private final HouseholdManagementView householdManagementView;

    public StaffDashboard(Scanner scanner,
                          ResidentManagementView residentManagementView,
                          HouseholdManagementView householdManagementView) {
        this.scanner = scanner;
        this.residentManagementView = residentManagementView;
        this.householdManagementView = householdManagementView;
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

            switch (choice) {
                case "1" -> residentManagementView.show();
                case "2" -> householdManagementView.show();
                case "3", "4", "5", "6", "7" -> showComingSoon();
                case "0" -> { }
                default -> {
                    ConsoleUI.printError("Please choose a valid menu option.");
                    pause();
                }
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

    private void showComingSoon() {
        System.out.println();
        ConsoleUI.printInfo("This feature is not implemented yet.");
        pause();
    }

    private void pause() {
        System.out.println();
        ConsoleUI.printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
