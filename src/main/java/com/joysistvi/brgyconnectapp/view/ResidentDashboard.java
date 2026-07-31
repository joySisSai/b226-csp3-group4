package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ResidentController;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.Sex;
import com.joysistvi.brgyconnectapp.model.CivilStatus;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.service.ResidentService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;


// Dashboard for Resident users.

public class ResidentDashboard {
    private final Scanner scanner;
    private final ResidentController residentController;

    public ResidentDashboard(Scanner scanner) {
        this.scanner = scanner;
        ResidentService residentService = new ResidentService();
        this.residentController = new ResidentController(residentService);
    }

    public void show(User user) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            printHeader(user);
            ConsoleUI.printSubHeader("Resident Operations");
            ConsoleUI.printMenuOption("1", "View All Residents");
            ConsoleUI.printMenuOption("2", "Register New Resident");
            ConsoleUI.printMenuOption("3", "Update Resident Record");
            ConsoleUI.printMenuOption("4", "Delete Resident Record");
            ConsoleUI.printMenuOption("0", "Log out");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllResidents();
                    break;
                case "2":
                    addNewResident();
                    break;
                case "3":
                    ConsoleUI.printInfo("Update feature — ready for further work");
                    break;
                case "4":
                    ConsoleUI.printInfo("Delete feature — ready for further work");
                    break;
                case "0":
                    ConsoleUI.printSuccess("Logged out successfully.");
                    break;
                default:
                    ConsoleUI.printError("Invalid choice. Please try again.");
            }

            if (!choice.equals("0")) {
                System.out.println();
                ConsoleUI.printPrompt("Press Enter to continue...");
                scanner.nextLine();
            }
        } while (!choice.equals("0"));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }

    private void printHeader(User user) {
        ConsoleUI.printHeader("Resident Dashboard");
        System.out.println(ConsoleUI.CYAN + " Welcome, " + ConsoleUI.BOLD + user.getDisplayName() + "!" + ConsoleUI.RESET);
        System.out.println();
    }

    private void viewAllResidents() {
        ConsoleUI.printSubHeader("LIST OF RESIDENTS");
        List<Resident> list = residentController.getAllResidents();

        if (list.isEmpty()) {
            ConsoleUI.printInfo("No resident records found.");
            return;
        }

        for (Resident r : list) {
            System.out.printf("ID: %d | Code: %s | Name: %s %s %s%n",
                    r.getResidentId(),
                    r.getResidentCode(),
                    r.getFirstName(),
                    r.getMiddleName() == null ? "" : r.getMiddleName(),
                    r.getLastName());
        }
    }

    private void addNewResident() {
        ConsoleUI.printSubHeader("REGISTER NEW RESIDENT");
        Resident r = new Resident();

        ConsoleUI.printPrompt("Resident Code: ");
        r.setResidentCode(scanner.nextLine().trim());

        ConsoleUI.printPrompt("First Name: ");
        r.setFirstName(scanner.nextLine().trim());

        ConsoleUI.printPrompt("Middle Name (optional): ");
        r.setMiddleName(scanner.nextLine().trim());

        ConsoleUI.printPrompt("Last Name: ");
        r.setLastName(scanner.nextLine().trim());

        ConsoleUI.printPrompt("Birth Date (YYYY-MM-DD): ");
        r.setBirthDate(LocalDate.parse(scanner.nextLine().trim()));

        ConsoleUI.printPrompt("Sex (MALE/FEMALE): ");
        r.setSex(Sex.valueOf(scanner.nextLine().trim().toUpperCase()));

        ConsoleUI.printPrompt("Civil Status (SINGLE/MARRIED/WIDOWED/SEPARATED): ");
        r.setCivilStatus(CivilStatus.valueOf(scanner.nextLine().trim().toUpperCase()));

        ConsoleUI.printPrompt("Residency Status (ACTIVE/INACTIVE/TRANSFERRED): ");
        r.setResidencyStatus(ResidencyStatus.valueOf(scanner.nextLine().trim().toUpperCase()));

        ConsoleUI.printPrompt("Contact Number: ");
        r.setContactNumber(scanner.nextLine().trim());

        String result = residentController.addResident(r);
        System.out.println();
        if (result.contains("successfully")) {
            ConsoleUI.printSuccess(result);
        } else {
            ConsoleUI.printError(result);
        }
    }
}