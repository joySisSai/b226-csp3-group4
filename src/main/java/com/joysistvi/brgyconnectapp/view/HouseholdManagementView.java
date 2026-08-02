package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.HouseholdController;
import com.joysistvi.brgyconnectapp.model.Household;
import com.joysistvi.brgyconnectapp.model.HouseholdStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.User;

import java.util.List;
import java.util.Scanner;

public class HouseholdManagementView {
    private final Scanner scanner;
    private final HouseholdController householdController;

    public HouseholdManagementView(Scanner scanner, HouseholdController householdController) {
        this.scanner = scanner;
        this.householdController = householdController;
    }

    public void show(User actingUser) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Household Management");
            ConsoleUI.printMenuOption("1", "List households");
            ConsoleUI.printMenuOption("2", "Search households");
            ConsoleUI.printMenuOption("3", "View household and members");
            ConsoleUI.printMenuOption("4", "Create household");
            ConsoleUI.printMenuOption("5", "Update household");
            ConsoleUI.printMenuOption("6", "Add household member");
            ConsoleUI.printMenuOption("7", "Remove household member");
            ConsoleUI.printMenuOption("8", "Assign household head");
            ConsoleUI.printMenuOption("9", "Deactivate household");
            ConsoleUI.printMenuOption("0", "Back");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listHouseholds(actingUser);
                case "2" -> searchHouseholds(actingUser);
                case "3" -> viewHousehold(actingUser);
                case "4" -> createHousehold(actingUser);
                case "5" -> updateHousehold(actingUser);
                case "6" -> addMember(actingUser);
                case "7" -> removeMember(actingUser);
                case "8" -> assignHouseholdHead(actingUser);
                case "9" -> deactivateHousehold(actingUser);
                case "0" -> { }
                default -> ConsoleUI.printError("Please choose a valid menu option.");
            }

            if (!choice.equals("0")) {
                pause();
            }
        } while (!choice.equals("0"));
    }

    private void listHouseholds(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Household Records");
        printHouseholds(householdController.getAllHouseholds(userId(actingUser)));
    }

    private void searchHouseholds(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Search Households");
        ConsoleUI.printPrompt("Code, address, or purok: ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isBlank()) {
            ConsoleUI.printError("A search keyword is required.");
            return;
        }
        printHouseholds(householdController.search(keyword, userId(actingUser)));
    }

    private void viewHousehold(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Household Details");
        int householdId = promptPositiveInteger("Household ID: ");
        Household household = householdController.getById(householdId, userId(actingUser));
        if (household == null) {
            ConsoleUI.printError("Household record not found.");
            return;
        }

        printHouseholdDetails(household);
        System.out.println();
        ConsoleUI.printSubHeader("Household Members");
        printMembers(householdController.getMembers(householdId, userId(actingUser)));
    }

    private void createHousehold(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Create Household");
        Household household = new Household();
        ConsoleUI.printInfo("The household code will be generated automatically.");
        household.setAddressLine(promptRequired("Address: "));
        household.setPurok(promptRequired("Purok: "));
        household.setHouseholdStatus(HouseholdStatus.ACTIVE);

        printOperationResult(householdController.create(household, userId(actingUser)));
    }

    private void updateHousehold(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Update Household");
        int householdId = promptPositiveInteger("Household ID: ");
        Household household = householdController.getById(householdId, userId(actingUser));
        if (household == null) {
            ConsoleUI.printError("Household record not found.");
            return;
        }

        ConsoleUI.printInfo("Household code: " + household.getHouseholdCode());
        ConsoleUI.printInfo("Press Enter to keep the current value.");
        household.setAddressLine(promptTextUpdate("Address", household.getAddressLine()));
        household.setPurok(promptTextUpdate("Purok", household.getPurok()));
        printOperationResult(householdController.update(household, userId(actingUser)));
    }

    private void addMember(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Add Household Member");
        int householdId = promptPositiveInteger("Household ID: ");
        if (!showSelectedHousehold(householdId, actingUser)) {
            return;
        }
        int residentId = promptPositiveInteger("Resident ID: ");
        printOperationResult(householdController.addMember(householdId, residentId, userId(actingUser)));
    }

    private void removeMember(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Remove Household Member");
        int householdId = promptPositiveInteger("Household ID: ");
        Household household = householdController.getById(householdId, userId(actingUser));
        if (household == null) {
            ConsoleUI.printError("Household record not found.");
            return;
        }

        printHouseholdDetails(household);
        System.out.println();
        printMembers(householdController.getMembers(householdId, userId(actingUser)));
        int residentId = promptPositiveInteger("Resident ID to remove: ");
        if (!promptYesNo("Confirm removal? (Y/N): ")) {
            ConsoleUI.printInfo("Member removal cancelled.");
            return;
        }
        printOperationResult(householdController.removeMember(householdId, residentId, userId(actingUser)));
    }

    private void assignHouseholdHead(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Assign Household Head");
        int householdId = promptPositiveInteger("Household ID: ");
        Household household = householdController.getById(householdId, userId(actingUser));
        if (household == null) {
            ConsoleUI.printError("Household record not found.");
            return;
        }

        printHouseholdDetails(household);
        System.out.println();
        List<Resident> members = householdController.getMembers(householdId, userId(actingUser));
        printMembers(members);
        if (members.isEmpty()) {
            return;
        }

        int residentId = promptPositiveInteger("Resident ID for household head: ");
        if (!promptYesNo("Confirm household-head assignment? (Y/N): ")) {
            ConsoleUI.printInfo("Household-head assignment cancelled.");
            return;
        }
        printOperationResult(householdController.assignHouseholdHead(
                householdId, residentId, userId(actingUser)));
    }

    private void deactivateHousehold(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Deactivate Household");
        int householdId = promptPositiveInteger("Household ID: ");
        Household household = householdController.getById(householdId, userId(actingUser));
        if (household == null) {
            ConsoleUI.printError("Household record not found.");
            return;
        }

        printHouseholdDetails(household);
        if (household.getHouseholdStatus() == HouseholdStatus.INACTIVE) {
            ConsoleUI.printInfo("This household is already inactive.");
            return;
        }
        if (!promptYesNo("Confirm deactivation? (Y/N): ")) {
            ConsoleUI.printInfo("Household deactivation cancelled.");
            return;
        }
        printOperationResult(householdController.deactivate(householdId, userId(actingUser)));
    }

    private int userId(User user) {
        return user == null || user.getUserId() == null ? 0 : user.getUserId();
    }

    private boolean showSelectedHousehold(int householdId, User actingUser) {
        Household household = householdController.getById(householdId, userId(actingUser));
        if (household == null) {
            ConsoleUI.printError("Household record not found.");
            return false;
        }
        printHouseholdDetails(household);
        return true;
    }

    private void printHouseholds(List<Household> households) {
        if (households == null || households.isEmpty()) {
            ConsoleUI.printInfo("No household records found.");
            return;
        }

        TableFormatter formatter = new TableFormatter("ID", "Code", "Purok", "Address", "Status");
        for (Household household : households) {
            formatter.addRow(
                    String.valueOf(household.getHouseholdId()),
                    abbreviate(household.getHouseholdCode(), 16),
                    abbreviate(household.getPurok(), 18),
                    abbreviate(household.getAddressLine(), 36),
                    String.valueOf(household.getHouseholdStatus()));
        }
        formatter.print();
        System.out.println();
        ConsoleUI.printInfo(households.size() + " record(s) found.");
    }

    private void printHouseholdDetails(Household household) {
        System.out.printf("Household ID   : %s%n", household.getHouseholdId());
        System.out.printf("Household Code : %s%n", valueOrDash(household.getHouseholdCode()));
        System.out.printf("Address        : %s%n", valueOrDash(household.getAddressLine()));
        System.out.printf("Purok          : %s%n", valueOrDash(household.getPurok()));
        System.out.printf("Status         : %s%n", valueOrDash(household.getHouseholdStatus()));
    }

    private void printMembers(List<Resident> members) {
        if (members == null || members.isEmpty()) {
            ConsoleUI.printInfo("This household has no members.");
            return;
        }

        TableFormatter formatter = new TableFormatter("ID", "Code", "Name", "Role");
        for (Resident resident : members) {
            formatter.addRow(
                    String.valueOf(resident.getResidentId()),
                    valueOrDash(resident.getResidentCode()),
                    abbreviate(fullName(resident), 34),
                    resident.isHouseholdHead() ? "Head" : "Member");
        }
        formatter.print();
        System.out.println();
    }

    private String promptRequired(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) {
                return value;
            }
            ConsoleUI.printError("This field is required.");
        }
    }

    private String promptTextUpdate(String label, String currentValue) {
        while (true) {
            ConsoleUI.printPrompt(label + " [" + valueOrDash(currentValue) + "]: ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return currentValue;
            }
            if (!value.isBlank()) {
                return value;
            }
            ConsoleUI.printError(label + " is required.");
        }
    }

    private int promptPositiveInteger(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number.");
        }
    }

    private boolean promptYesNo(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("Y") || input.equalsIgnoreCase("YES")) {
                return true;
            }
            if (input.equalsIgnoreCase("N") || input.equalsIgnoreCase("NO")) {
                return false;
            }
            ConsoleUI.printError("Enter Y for yes or N for no.");
        }
    }

    private void printOperationResult(String result) {
        if (result != null && result.toLowerCase().contains("successfully")) {
            ConsoleUI.printSuccess(result);
        } else {
            ConsoleUI.printError(result == null ? "The operation could not be completed." : result);
        }
    }

    private String fullName(Resident resident) {
        return String.join(" ",
                valueOrEmpty(resident.getFirstName()),
                valueOrEmpty(resident.getMiddleName()),
                valueOrEmpty(resident.getLastName()),
                valueOrEmpty(resident.getSuffix()))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    private String valueOrDash(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }

    private String abbreviate(String value, int maximumLength) {
        String safeValue = valueOrDash(value);
        if (safeValue.length() <= maximumLength) {
            return safeValue;
        }
        return safeValue.substring(0, maximumLength - 3) + "...";
    }

    private void pause() {
        System.out.println();
        ConsoleUI.printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
