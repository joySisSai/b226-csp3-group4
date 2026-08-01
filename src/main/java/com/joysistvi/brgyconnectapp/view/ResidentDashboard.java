package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ResidentController;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.User;

import java.util.Scanner;

public class ResidentDashboard {
    private final Scanner scanner;
    private final ResidentController residentController;

    public ResidentDashboard(Scanner scanner) {
        this.scanner = scanner;
        this.residentController = new ResidentController();
    }

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
            ConsoleUI.printPrompt("Select option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewMyProfile(user);
                    break;
                case "2":
                    //new SubmitServiceRequestView(scanner).show(user);
                    break;
                case "3":
                    ConsoleUI.printInfo("Feature coming soon");
                    break;
                case "0":
                    ConsoleUI.printSuccess("Logged out");
                    break;
                default:
                    ConsoleUI.printError("Invalid choice");
            }
            if (!choice.equals("0")) {
                ConsoleUI.printPrompt("\nPress Enter to continue...");
                scanner.nextLine();
            }
        } while (!choice.equals("0"));
    }

    private void printHeader(User user) {
        ConsoleUI.printHeader("Resident Dashboard");
        System.out.println("Welcome, " + user.getDisplayName() + "\n");
    }

    // Load full resident record from database using logged-in user ID
    private void viewMyProfile(User user) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("My Profile");

        Integer residentId = user.getResidentId();

        if (residentId == null) {
            ConsoleUI.printError("No resident record is linked to this account.");
            return;
        }

        Resident resident = residentController.getById(residentId);

        if (resident == null) {
            ConsoleUI.printError("Resident profile not found.");
            return;
        }

        System.out.printf("Resident Code   : %s%n", resident.getResidentCode());
        System.out.printf("Full Name       : %s %s %s %s%n",
                resident.getFirstName(),
                resident.getMiddleName() == null ? "" : resident.getMiddleName(),
                resident.getLastName(),
                resident.getSuffix() == null ? "" : resident.getSuffix());
        System.out.printf("Birth Date      : %s%n", resident.getBirthDate());
        System.out.printf("Sex             : %s%n", resident.getSex());
        System.out.printf("Civil Status    : %s%n", resident.getCivilStatus());
        System.out.printf("Contact Number  : %s%n",
                resident.getContactNumber() == null ? "N/A" : resident.getContactNumber());
        System.out.printf("Email Address   : %s%n",
                resident.getEmail() == null ? "N/A" : resident.getEmail());
        System.out.printf("Occupation      : %s%n",
                resident.getOccupation() == null ? "N/A" : resident.getOccupation());
        System.out.printf("Registered Voter: %s%n",
                resident.isRegisteredVoter() ? "Yes" : "No");
        System.out.printf("Household Head  : %s%n",
                resident.isHouseholdHead() ? "Yes" : "No");
        System.out.printf("Residency Status: %s%n", resident.getResidencyStatus());
        System.out.printf("Account Status  : %s%n", user.getAccountStatus());
    }
}