package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.RegistrationController;
import com.joysistvi.brgyconnectapp.model.CivilStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.Sex;
import com.joysistvi.brgyconnectapp.model.User;

import java.io.Console;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class RegistrationView {
    private final RegistrationController registrationController;
    private final Scanner scanner;

    public RegistrationView(RegistrationController registrationController, Scanner scanner) {
        this.registrationController = registrationController;
        this.scanner = scanner;
    }

    public void show() {
        while (true) {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Register Account");
            ConsoleUI.printMenuOption("1", "Register with an existing Resident Code");
            ConsoleUI.printMenuOption("2", "Register as a New Resident");
            ConsoleUI.printMenuOption("0", "Back to Login");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> registerExistingResident();
                case "2" -> registerNewResident();
                case "0" -> {
                    return;
                }
                default -> {
                    ConsoleUI.printError("Please enter 1, 2, or 0.");
                    pause();
                }
            }
        }
    }

    private void registerExistingResident() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Link Existing Resident Record");

        boolean firstTry = true;
        String residentCode = "";
        User user = new User();
        char[] password = new char[0];
        
        while (true) {
            if (firstTry) {
                residentCode = promptRequired("Resident Code: ");
                user.setUsername(promptRequired("Username: "));
                user.setDisplayName(promptRequired("Display Name (Full Name): "));
                password = readPassword();
            } else {
                ConsoleUI.printInfo("Press Enter to keep the current value.");
                residentCode = promptTextUpdate("Resident Code", residentCode);
                user.setUsername(promptTextUpdate("Username", user.getUsername()));
                user.setDisplayName(promptTextUpdate("Display Name", user.getDisplayName()));
                
                char[] newPassword = readPassword(true);
                if (newPassword.length > 0) {
                    password = newPassword;
                }
            }
            
            String result = registrationController.registerWithExistingResident(residentCode, user, password);
            if (result != null && result.toLowerCase().contains("success")) {
                printOperationResult(result);
                pause();
                break;
            }

            ConsoleUI.printError(result);
            if (!promptYesNo("Registration failed. Would you like to edit your details and try again? (Y/N): ")) {
                break;
            }
            firstTry = false;
        }
    }

    private void registerNewResident() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Register New Resident");

        boolean firstTry = true;
        Resident resident = new Resident();
        User user = new User();
        char[] password = new char[0];
        resident.setResidencyStatus(ResidencyStatus.ACTIVE);

        while (true) {
            if (firstTry) {
                resident.setFirstName(promptRequired("First Name: "));
                resident.setMiddleName(promptOptional("Middle Name: "));
                resident.setLastName(promptRequired("Last Name: "));
                resident.setSuffix(promptOptional("Suffix (e.g., Jr, III): "));
                resident.setBirthDate(promptDate("Birth Date (YYYY-MM-DD): "));
                resident.setSex(promptEnum("Sex (MALE/FEMALE): ", Sex.class));
                resident.setCivilStatus(promptEnum("Civil Status (SINGLE/MARRIED/WIDOWED/LEGALLY_SEPARATED): ", CivilStatus.class));
                resident.setContactNumber(promptOptional("Contact Number: "));
                resident.setEmail(promptOptional("Email Address: "));
                resident.setOccupation(promptOptional("Occupation: "));
                resident.setHouseholdId(promptOptionalPositiveInteger("Household ID (leave blank if none): "));
                resident.setRegisteredVoter(promptYesNo("Is a registered voter? (Y/N): "));
                resident.setHouseholdHead(promptYesNo("Is household head? (Y/N): "));
                
                System.out.println("\n--- Account Details ---");
                user.setUsername(promptRequired("Username: "));
                user.setDisplayName(resident.getFirstName() + " " + resident.getLastName());
                System.out.println("Display Name auto-set to: " + user.getDisplayName());
                password = readPassword();
            } else {
                ConsoleUI.printInfo("Press Enter to keep the current value.");
                resident.setFirstName(promptTextUpdate("First Name", resident.getFirstName()));
                resident.setMiddleName(promptTextUpdate("Middle Name", resident.getMiddleName()));
                resident.setLastName(promptTextUpdate("Last Name", resident.getLastName()));
                resident.setSuffix(promptTextUpdate("Suffix", resident.getSuffix()));
                resident.setBirthDate(promptDateUpdate("Birth Date (YYYY-MM-DD)", resident.getBirthDate()));
                resident.setSex(promptEnumUpdate("Sex (MALE/FEMALE)", resident.getSex(), Sex.class));
                resident.setCivilStatus(promptEnumUpdate("Civil Status", resident.getCivilStatus(), CivilStatus.class));
                resident.setContactNumber(promptTextUpdate("Contact Number", resident.getContactNumber()));
                resident.setEmail(promptTextUpdate("Email Address", resident.getEmail()));
                resident.setOccupation(promptTextUpdate("Occupation", resident.getOccupation()));
                resident.setHouseholdId(promptIntegerUpdate("Household ID", resident.getHouseholdId()));
                resident.setRegisteredVoter(promptBooleanUpdate("Is a registered voter? (Y/N)", resident.isRegisteredVoter()));
                resident.setHouseholdHead(promptBooleanUpdate("Is household head? (Y/N)", resident.isHouseholdHead()));
                
                System.out.println("\n--- Account Details ---");
                user.setUsername(promptTextUpdate("Username", user.getUsername()));
                user.setDisplayName(promptTextUpdate("Display Name", user.getDisplayName()));
                
                char[] newPassword = readPassword(true);
                if (newPassword.length > 0) {
                    password = newPassword;
                }
            }

            String result = registrationController.registerNewResident(resident, user, password);
            if (result != null && result.toLowerCase().contains("success")) {
                printOperationResult(result);
                pause();
                break;
            }

            ConsoleUI.printError(result);
            if (!promptYesNo("Registration failed. Would you like to edit your details and try again? (Y/N): ")) {
                break;
            }
            firstTry = false;
        }
    }

    private String promptRequired(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            ConsoleUI.printError("This field is required.");
        }
    }

    private String promptOptional(String label) {
        ConsoleUI.printPrompt(label);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    private LocalDate promptDate(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException ignored) {
                ConsoleUI.printError("Please enter a valid date in YYYY-MM-DD format.");
            }
        }
    }

    private <T extends Enum<T>> T promptEnum(String label, Class<T> enumClass) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return Enum.valueOf(enumClass, input);
            } catch (IllegalArgumentException ignored) {
                ConsoleUI.printError("Please enter a valid option.");
            }
        }
    }

    private Integer promptOptionalPositiveInteger(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                int value = Integer.parseInt(input);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number or leave it blank.");
        }
    }

    private boolean promptYesNo(String label) {
        return promptYesNo(label, false);
    }

    private boolean promptYesNo(String label, boolean isUpdate) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim().toUpperCase();
            if (isUpdate && input.isEmpty()) {
                throw new IllegalStateException("Empty input during update should be caught before this");
            }
            if (input.equals("Y") || input.equals("YES")) return true;
            if (input.equals("N") || input.equals("NO")) return false;
            ConsoleUI.printError("Please enter Y or N.");
        }
    }

    private String promptTextUpdate(String fieldName, String currentValue) {
        ConsoleUI.printPrompt(String.format("%s [%s]: ", fieldName, currentValue == null ? "" : currentValue));
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentValue : input;
    }

    private LocalDate promptDateUpdate(String fieldName, LocalDate currentValue) {
        while (true) {
            ConsoleUI.printPrompt(String.format("%s [%s]: ", fieldName, currentValue));
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return currentValue;
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException ignored) {
                ConsoleUI.printError("Please enter a valid date in YYYY-MM-DD format.");
            }
        }
    }

    private <T extends Enum<T>> T promptEnumUpdate(String fieldName, T currentValue, Class<T> enumClass) {
        while (true) {
            ConsoleUI.printPrompt(String.format("%s [%s]: ", fieldName, currentValue));
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) return currentValue;
            try {
                return Enum.valueOf(enumClass, input);
            } catch (IllegalArgumentException ignored) {
                ConsoleUI.printError("Please enter a valid option.");
            }
        }
    }

    private Integer promptIntegerUpdate(String fieldName, Integer currentValue) {
        while (true) {
            ConsoleUI.printPrompt(String.format("%s [%s]: ", fieldName, currentValue == null ? "" : currentValue));
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return currentValue;
            try {
                int value = Integer.parseInt(input);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number or leave it blank.");
        }
    }

    private boolean promptBooleanUpdate(String fieldName, boolean currentValue) {
        while (true) {
            ConsoleUI.printPrompt(String.format("%s [%s]: ", fieldName, currentValue ? "Y" : "N"));
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) return currentValue;
            if (input.equals("Y") || input.equals("YES")) return true;
            if (input.equals("N") || input.equals("NO")) return false;
            ConsoleUI.printError("Please enter Y or N.");
        }
    }

    private char[] readPassword() {
        return readPassword(false);
    }
    
    private char[] readPassword(boolean isUpdate) {
        Console console = System.console();
        String prompt = isUpdate ? "Password [hidden]: " : "Password: ";
        
        if (console != null) {
            char[] password = console.readPassword(ConsoleUI.CYAN + ConsoleUI.BOLD + " » " + ConsoleUI.RESET + prompt);
            return password == null ? new char[0] : password;
        }

        ConsoleUI.printPrompt(prompt);
        return scanner.nextLine().toCharArray();
    }

    private void printOperationResult(String result) {
        System.out.println();
        if (result != null && result.toLowerCase().contains("success")) {
            ConsoleUI.printSuccess(result);
        } else {
            ConsoleUI.printError(result != null ? result : "Operation failed");
        }
    }

    private void pause() {
        System.out.println();
        ConsoleUI.printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
