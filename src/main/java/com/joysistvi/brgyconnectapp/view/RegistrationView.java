package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.RegistrationController;
import com.joysistvi.brgyconnectapp.model.CivilStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.Sex;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.validation.ResidentFieldValidator;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;

public class RegistrationView {
    private static final int MAXIMUM_DISPLAY_NAME_LENGTH = 150;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]{4,50}");
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
                user.setUsername(promptUsername("Username: "));
                user.setDisplayName(promptDisplayName("Display Name (Full Name): "));
                password = readValidPassword();
            } else {
                ConsoleUI.printInfo("Press Enter to keep the current value.");
                residentCode = promptTextUpdate("Resident Code", residentCode);
                user.setUsername(promptValidatedTextUpdate(
                        "Username", user.getUsername(), this::validateUsername));
                user.setDisplayName(promptValidatedTextUpdate(
                        "Display Name", user.getDisplayName(), this::validateDisplayName));
                ConsoleUI.printInfo("Re-enter the password to retry registration.");
                password = readValidPassword();
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
                resident.setSex(promptEnum("Sex", Sex.values(), false, null));
                resident.setCivilStatus(promptEnum("Civil status", CivilStatus.values(), false, null));
                resident.setContactNumber(promptValidatedOptional(
                        "Contact Number: ", ResidentFieldValidator::validateContactNumber));
                resident.setEmail(promptValidatedOptional(
                        "Email Address: ", ResidentFieldValidator::validateEmail));
                resident.setOccupation(promptOptional("Occupation: "));
                resident.setHouseholdId(promptOptionalPositiveInteger("Household ID (leave blank if none): "));
                resident.setRegisteredVoter(promptYesNo("Is a registered voter? (Y/N): "));
                resident.setHouseholdHead(promptYesNo("Is household head? (Y/N): "));
                
                System.out.println("\n--- Account Details ---");
                user.setUsername(promptUsername("Username: "));
                user.setDisplayName(resident.getFirstName() + " " + resident.getLastName());
                System.out.println("Display Name auto-set to: " + user.getDisplayName());
                password = readValidPassword();
            } else {
                ConsoleUI.printInfo("Press Enter to keep the current value.");
                resident.setFirstName(promptTextUpdate("First Name", resident.getFirstName()));
                resident.setMiddleName(promptTextUpdate("Middle Name", resident.getMiddleName()));
                resident.setLastName(promptTextUpdate("Last Name", resident.getLastName()));
                resident.setSuffix(promptTextUpdate("Suffix", resident.getSuffix()));
                resident.setBirthDate(promptDateUpdate("Birth Date (YYYY-MM-DD)", resident.getBirthDate()));
                resident.setSex(promptEnum("Sex", Sex.values(), true, resident.getSex()));
                resident.setCivilStatus(promptEnum("Civil status", CivilStatus.values(), true, resident.getCivilStatus()));
                resident.setContactNumber(promptValidatedTextUpdate(
                        "Contact Number", resident.getContactNumber(), ResidentFieldValidator::validateContactNumber));
                resident.setEmail(promptValidatedTextUpdate(
                        "Email Address", resident.getEmail(), ResidentFieldValidator::validateEmail));
                resident.setOccupation(promptTextUpdate("Occupation", resident.getOccupation()));
                resident.setHouseholdId(promptIntegerUpdate("Household ID", resident.getHouseholdId()));
                resident.setRegisteredVoter(promptBooleanUpdate("Is a registered voter? (Y/N)", resident.isRegisteredVoter()));
                resident.setHouseholdHead(promptBooleanUpdate("Is household head? (Y/N)", resident.isHouseholdHead()));
                
                System.out.println("\n--- Account Details ---");
                user.setUsername(promptValidatedTextUpdate(
                        "Username", user.getUsername(), this::validateUsername));
                user.setDisplayName(promptValidatedTextUpdate(
                        "Display Name", user.getDisplayName(), this::validateDisplayName));
                ConsoleUI.printInfo("Re-enter the password to retry registration.");
                password = readValidPassword();
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

    private String promptValidatedOptional(String label, Function<String, String> validator) {
        while (true) {
            String value = promptOptional(label);
            String validationError = validator.apply(value);
            if (validationError == null) {
                return ResidentFieldValidator.normalizeOptional(value);
            }
            ConsoleUI.printError(validationError + ".");
        }
    }

    private LocalDate promptDate(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            try {
                LocalDate date = LocalDate.parse(input);
                String validationError = ResidentFieldValidator.validateBirthDate(date);
                if (validationError == null) {
                    return date;
                }
                ConsoleUI.printError(validationError + ".");
            } catch (DateTimeParseException ignored) {
                ConsoleUI.printError("Please enter a valid date in YYYY-MM-DD format.");
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

    private String formatEnum(Enum<?> value) {
        if (value == null) {
            return "-";
        }
        String name = value.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
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

    private String promptValidatedTextUpdate(String fieldName,
                                             String currentValue,
                                             Function<String, String> validator) {
        while (true) {
            String value = promptTextUpdate(fieldName, currentValue);
            String validationError = validator.apply(value);
            if (validationError == null) {
                return ResidentFieldValidator.normalizeOptional(value);
            }
            ConsoleUI.printError(validationError);
        }
    }

    private LocalDate promptDateUpdate(String fieldName, LocalDate currentValue) {
        while (true) {
            ConsoleUI.printPrompt(String.format("%s [%s]: ", fieldName, currentValue));
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return currentValue;
            try {
                LocalDate date = LocalDate.parse(input);
                String validationError = ResidentFieldValidator.validateBirthDate(date);
                if (validationError == null) {
                    return date;
                }
                ConsoleUI.printError(validationError + ".");
            } catch (DateTimeParseException ignored) {
                ConsoleUI.printError("Please enter a valid date in YYYY-MM-DD format.");
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

    private char[] readValidPassword() {
        while (true) {
            char[] password = readPassword();
            String validationError = validatePassword(password);
            if (validationError == null) {
                return password;
            }
            Arrays.fill(password, '\0');
            ConsoleUI.printError(validationError);
        }
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

    private String promptUsername(String label) {
        while (true) {
            String username = promptRequired(label);
            String validationError = validateUsername(username);
            if (validationError == null) {
                return username;
            }
            ConsoleUI.printError(validationError);
        }
    }

    private String promptDisplayName(String label) {
        while (true) {
            String displayName = promptRequired(label);
            String validationError = validateDisplayName(displayName);
            if (validationError == null) {
                return displayName;
            }
            ConsoleUI.printError(validationError);
        }
    }

    private String validateUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches()
                ? null
                : "Username must be 4-50 characters using letters, numbers, dots, underscores, or hyphens.";
    }

    private String validateDisplayName(String displayName) {
        return displayName != null && !displayName.isBlank() &&
                displayName.trim().length() <= MAXIMUM_DISPLAY_NAME_LENGTH
                ? null
                : "Display name is required and must not exceed 150 characters.";
    }

    private String validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            return "Password must contain at least 8 characters.";
        }
        if (new String(password).getBytes(StandardCharsets.UTF_8).length > 72) {
            return "Password must not exceed 72 UTF-8 bytes.";
        }
        return null;
    }

    private void pause() {
        System.out.println();
        ConsoleUI.printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
