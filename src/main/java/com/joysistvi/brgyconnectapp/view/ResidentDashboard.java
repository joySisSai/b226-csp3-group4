package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ResidentController;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.service.ResidentService;

import java.util.Scanner;

public class ResidentDashboard {
    private final Scanner scanner;
    private final ResidentController residentController;

    public ResidentDashboard(Scanner scanner) {
        this.scanner = scanner;
        ResidentService residentService = new ResidentService();
        this.residentController = new ResidentController(residentService);
    }

    // Show main menu for resident users only
    public void show(User user) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            printHeader(user);
            ConsoleUI.printSubHeader("My Services");
            ConsoleUI.printMenuOption("1", "View My Profile");
            ConsoleUI.printMenuOption("2", "Submit Service Request");
            ConsoleUI.printMenuOption("3", "Check Request Status");
            ConsoleUI.printMenuOption("0", "Log Out");
            System.out.println();
            ConsoleUI.printPrompt("Select an option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": viewMyProfile(user); break;
                case "2": ConsoleUI.printInfo("Service Request form — ready"); break;
                case "3": ConsoleUI.printInfo("View my requests — ready"); break;
                case "0": ConsoleUI.printSuccess("Logged out successfully."); break;
                default: ConsoleUI.printError("Invalid choice — please try again.");
            }

            if (!choice.equals("0")) {
                System.out.println();
                ConsoleUI.printPrompt("Press Enter to return to menu...");
                scanner.nextLine();
            }
        } while (!choice.equals("0"));
    }

    // Display welcome header
    private void printHeader(User user) {
        ConsoleUI.printHeader("Resident Dashboard");
        System.out.println(ConsoleUI.CYAN + " Welcome, " + ConsoleUI.BOLD + user.getDisplayName() + "!" + ConsoleUI.RESET);
        System.out.println();
    }

    // Placeholder for profile view — will pull from logged-in resident data
    private void viewMyProfile(User user) {
        ConsoleUI.printSubHeader("My Profile");
        System.out.println("User ID  : " + user.getUserId());
        System.out.println("Full Name: " + user.getDisplayName());
        System.out.println("Role     : " + user.getRole());
        System.out.println("Status   : Active");
    }
}