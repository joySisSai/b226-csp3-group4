package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ResidentController;
import com.joysistvi.brgyconnectapp.controller.ServiceRequestController;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.service.DataAccessException;

import java.util.Scanner;

public class ResidentDashboard {
    private final Scanner scanner;
    private final ResidentController residentController;
    private final ResidentServiceRequestView requestView;
    private final com.joysistvi.brgyconnectapp.controller.HouseholdController householdController;

    public ResidentDashboard(Scanner scanner, ResidentController residentController, ServiceRequestController requestController, com.joysistvi.brgyconnectapp.controller.HouseholdController householdController) {
        this.scanner = scanner;
        this.residentController = residentController;
        this.requestView = new ResidentServiceRequestView(scanner, requestController);
        this.householdController = householdController;
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

            try {
                switch (choice) {
                    case "1":
                        viewMyProfile(user);
                        break;
                    case "2":
                        requestView.showSubmit(user);
                        break;
                    case "3":
                        requestView.showCheckStatus(user);
                        break;
                    case "0":
                        ConsoleUI.printSuccess("Logged out");
                        break;
                    default:
                        ConsoleUI.printError("Invalid choice");
                }
            } catch (DataAccessException exception) {
                ConsoleUI.printError(exception.getMessage());
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

        int actingUserId = user.getUserId() == null ? 0 : user.getUserId();
        Resident resident = residentController.getOwnProfile(residentId, actingUserId);

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
                
        String address = "N/A";
        if (resident.getHouseholdId() != null) {
            com.joysistvi.brgyconnectapp.model.Household h = householdController.getOwnHousehold(
                    resident.getHouseholdId(), resident.getResidentId(), actingUserId);
            if (h != null) {
                address = h.getAddressLine() + ", " + h.getPurok();
            }
        }
        System.out.printf("Address         : %s%n", address);
        
        System.out.printf("Registered Voter: %s%n",
                resident.isRegisteredVoter() ? "Yes" : "No");
        System.out.printf("Household Head  : %s%n",
                resident.isHouseholdHead() ? "Yes" : "No");
        System.out.printf("Residency Status: %s%n", resident.getResidencyStatus());
        System.out.printf("Account Status  : %s%n", user.getAccountStatus());
    }
}
