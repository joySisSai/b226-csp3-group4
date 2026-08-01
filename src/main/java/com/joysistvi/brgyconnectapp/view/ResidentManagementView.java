package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ResidentController;
import com.joysistvi.brgyconnectapp.model.CivilStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.Sex;
import com.joysistvi.brgyconnectapp.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ResidentManagementView {
    private final Scanner scanner;
    private final ResidentController residentController;

    public ResidentManagementView(Scanner scanner, ResidentController residentController) {
        this.scanner = scanner;
        this.residentController = residentController;
    }

    public void show(User actingUser) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Resident Management");
            ConsoleUI.printMenuOption("1", "List residents");
            ConsoleUI.printMenuOption("2", "Search residents");
            ConsoleUI.printMenuOption("3", "View resident details");
            ConsoleUI.printMenuOption("4", "Register resident");
            ConsoleUI.printMenuOption("5", "Update resident");
            ConsoleUI.printMenuOption("6", "Deactivate resident");
            ConsoleUI.printMenuOption("0", "Back");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listResidents();
                case "2" -> searchResidents();
                case "3" -> viewResident();
                case "4" -> registerResident(actingUser);
                case "5" -> updateResident(actingUser);
                case "6" -> deactivateResident(actingUser);
                case "0" -> { }
                default -> ConsoleUI.printError("Please choose a valid menu option.");
            }

            if (!choice.equals("0")) {
                pause();
            }
        } while (!choice.equals("0"));
    }

    private void listResidents() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Resident Records");
        printResidents(residentController.getAllResidents());
    }

    private void searchResidents() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Search Residents");
        ConsoleUI.printPrompt("Name or resident code: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isBlank()) {
            ConsoleUI.printError("A search keyword is required.");
            return;
        }

        printResidents(residentController.searchResidents(keyword));
    }

    private void viewResident() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Resident Details");
        Integer residentId = promptPositiveInteger("Resident ID: ", false);
        Resident resident = residentController.getById(residentId);

        if (resident == null) {
            ConsoleUI.printError("Resident record not found.");
            return;
        }

        printResidentDetails(resident);
    }

    private void registerResident(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Register Resident");

        Resident resident = new Resident();
        resident.setResidentCode(promptRequired("Resident code: "));
        resident.setFirstName(promptRequired("First name: "));
        resident.setMiddleName(promptOptional("Middle name (optional): "));
        resident.setLastName(promptRequired("Last name: "));
        resident.setSuffix(promptOptional("Suffix (optional): "));
        resident.setBirthDate(promptDate("Birth date (YYYY-MM-DD): ", false, null));
        resident.setSex(promptEnum("Sex", Sex.values(), false, null));
        resident.setCivilStatus(promptEnum("Civil status", CivilStatus.values(), false, null));
        resident.setContactNumber(promptOptional("Contact number (optional): "));
        resident.setEmail(promptOptional("Email (optional): "));
        resident.setOccupation(promptOptional("Occupation (optional): "));
        resident.setHouseholdId(promptPositiveInteger("Household ID (optional): ", true));
        resident.setRegisteredVoter(promptYesNo("Registered voter? (Y/N): ", false, false));
        resident.setHouseholdHead(promptYesNo("Household head? (Y/N): ", false, false));
        resident.setResidencyStatus(ResidencyStatus.ACTIVE);

        String result = residentController.register(resident, userId(actingUser));
        printOperationResult(result);
    }

    private void updateResident(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Update Resident");
        Integer residentId = promptPositiveInteger("Resident ID: ", false);
        Resident resident = residentController.getById(residentId);

        if (resident == null) {
            ConsoleUI.printError("Resident record not found.");
            return;
        }

        ConsoleUI.printInfo("Press Enter to keep the current value.");
        resident.setFirstName(promptTextUpdate("First name", resident.getFirstName(), true));
        resident.setMiddleName(promptTextUpdate("Middle name", resident.getMiddleName(), false));
        resident.setLastName(promptTextUpdate("Last name", resident.getLastName(), true));
        resident.setSuffix(promptTextUpdate("Suffix", resident.getSuffix(), false));
        resident.setBirthDate(promptDate("Birth date", true, resident.getBirthDate()));
        resident.setSex(promptEnum("Sex", Sex.values(), true, resident.getSex()));
        resident.setCivilStatus(promptEnum("Civil status", CivilStatus.values(), true,
                resident.getCivilStatus()));
        resident.setContactNumber(promptTextUpdate("Contact number", resident.getContactNumber(), false));
        resident.setEmail(promptTextUpdate("Email", resident.getEmail(), false));
        resident.setOccupation(promptTextUpdate("Occupation", resident.getOccupation(), false));
        resident.setHouseholdId(promptIntegerUpdate("Household ID", resident.getHouseholdId()));
        resident.setRegisteredVoter(promptYesNo(
                "Registered voter [" + yesNo(resident.isRegisteredVoter()) + "] (Y/N or Enter): ",
                true,
                resident.isRegisteredVoter()));
        resident.setHouseholdHead(promptYesNo(
                "Household head [" + yesNo(resident.isHouseholdHead()) + "] (Y/N or Enter): ",
                true,
                resident.isHouseholdHead()));
        resident.setResidencyStatus(promptEnum("Residency status", ResidencyStatus.values(), true,
                resident.getResidencyStatus()));

        String result = residentController.update(resident, userId(actingUser));
        printOperationResult(result);
    }

    private void deactivateResident(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Deactivate Resident");
        Integer residentId = promptPositiveInteger("Resident ID: ", false);
        Resident resident = residentController.getById(residentId);

        if (resident == null) {
            ConsoleUI.printError("Resident record not found.");
            return;
        }

        printResidentDetails(resident);
        if (resident.getResidencyStatus() == ResidencyStatus.INACTIVE) {
            ConsoleUI.printInfo("This resident is already inactive.");
            return;
        }

        System.out.println();
        boolean confirmed = promptYesNo("Confirm deactivation? (Y/N): ", false, false);
        if (!confirmed) {
            ConsoleUI.printInfo("Deactivation cancelled.");
            return;
        }

        String result = residentController.deactivate(residentId, userId(actingUser));
        printOperationResult(result);
    }

    private int userId(User user) {
        return user == null || user.getUserId() == null ? 0 : user.getUserId();
    }

    private void printResidents(List<Resident> residents) {
        if (residents == null || residents.isEmpty()) {
            ConsoleUI.printInfo("No resident records found.");
            return;
        }

        System.out.printf("%-6s %-14s %-30s %-12s %-12s%n",
                "ID", "Code", "Name", "Status", "Household");
        System.out.println("-".repeat(82));
        for (Resident resident : residents) {
            System.out.printf("%-6s %-14s %-30s %-12s %-12s%n",
                    resident.getResidentId(),
                    valueOrDash(resident.getResidentCode()),
                    abbreviate(fullName(resident), 30),
                    resident.getResidencyStatus(),
                    resident.getHouseholdId() == null ? "-" : resident.getHouseholdId());
        }
        System.out.println();
        ConsoleUI.printInfo(residents.size() + " record(s) found.");
    }

    private void printResidentDetails(Resident resident) {
        System.out.printf("Resident ID      : %s%n", resident.getResidentId());
        System.out.printf("Resident Code    : %s%n", valueOrDash(resident.getResidentCode()));
        System.out.printf("Full Name        : %s%n", fullName(resident));
        System.out.printf("Birth Date       : %s%n", valueOrDash(resident.getBirthDate()));
        System.out.printf("Sex              : %s%n", valueOrDash(resident.getSex()));
        System.out.printf("Civil Status     : %s%n", valueOrDash(resident.getCivilStatus()));
        System.out.printf("Contact Number   : %s%n", valueOrDash(resident.getContactNumber()));
        System.out.printf("Email            : %s%n", valueOrDash(resident.getEmail()));
        System.out.printf("Occupation       : %s%n", valueOrDash(resident.getOccupation()));
        System.out.printf("Household ID     : %s%n", valueOrDash(resident.getHouseholdId()));
        System.out.printf("Registered Voter : %s%n", yesNo(resident.isRegisteredVoter()));
        System.out.printf("Household Head   : %s%n", yesNo(resident.isHouseholdHead()));
        System.out.printf("Residency Status : %s%n", valueOrDash(resident.getResidencyStatus()));
        System.out.printf("Date Registered  : %s%n", valueOrDash(resident.getDateRegistered()));
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

    private String promptOptional(String label) {
        ConsoleUI.printPrompt(label);
        return blankToNull(scanner.nextLine());
    }

    private String promptTextUpdate(String label, String currentValue, boolean required) {
        while (true) {
            ConsoleUI.printPrompt(label + " [" + valueOrDash(currentValue) + "]: ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return currentValue;
            }
            if (!required || !value.isBlank()) {
                return value;
            }
            ConsoleUI.printError(label + " is required.");
        }
    }

    private Integer promptPositiveInteger(String label, boolean optional) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            if (optional && input.isEmpty()) {
                return null;
            }
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number" + (optional ? " or leave it blank." : "."));
        }
    }

    private Integer promptIntegerUpdate(String label, Integer currentValue) {
        while (true) {
            ConsoleUI.printPrompt(label + " [" + valueOrDash(currentValue) + "] (Enter to keep): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return currentValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number or leave it blank.");
        }
    }

    private LocalDate promptDate(String label, boolean allowBlank, LocalDate currentValue) {
        while (true) {
            String current = currentValue == null ? "" : " [" + currentValue + "]";
            ConsoleUI.printPrompt(label + current + (label.endsWith(": ") ? "" : ": "));
            String input = scanner.nextLine().trim();
            if (allowBlank && input.isEmpty()) {
                return currentValue;
            }
            try {
                LocalDate date = LocalDate.parse(input);
                if (date.isAfter(LocalDate.now())) {
                    ConsoleUI.printError("Birth date cannot be in the future.");
                    continue;
                }
                return date;
            } catch (DateTimeParseException ignored) {
                ConsoleUI.printError("Enter a valid date using YYYY-MM-DD.");
            }
        }
    }

    private <E extends Enum<E>> E promptEnum(String label, E[] values, boolean allowBlank, E currentValue) {
        while (true) {
            System.out.println();
            ConsoleUI.printSubHeader(label);
            for (int index = 0; index < values.length; index++) {
                ConsoleUI.printMenuOption(String.valueOf(index + 1), formatEnum(values[index]));
            }
            String suffix = allowBlank ? " (Enter to keep " + formatEnum(currentValue) + ")" : "";
            ConsoleUI.printPrompt("Select " + label.toLowerCase() + suffix + ": ");
            String input = scanner.nextLine().trim();
            if (allowBlank && input.isEmpty()) {
                return currentValue;
            }
            try {
                int selected = Integer.parseInt(input);
                if (selected >= 1 && selected <= values.length) {
                    return values[selected - 1];
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Choose a number from 1 to " + values.length + ".");
        }
    }

    private boolean promptYesNo(String label, boolean allowBlank, boolean currentValue) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            if (allowBlank && input.isEmpty()) {
                return currentValue;
            }
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
        if (result != null && result.toLowerCase().contains("success")) {
            ConsoleUI.printSuccess(result);
        } else if (result != null && result.toLowerCase().contains("inactive")) {
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

    private String blankToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String abbreviate(String value, int maximumLength) {
        if (value.length() <= maximumLength) {
            return value;
        }
        return value.substring(0, maximumLength - 3) + "...";
    }

    private String formatEnum(Enum<?> value) {
        if (value == null) {
            return "-";
        }
        String name = value.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private void pause() {
        System.out.println();
        ConsoleUI.printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
