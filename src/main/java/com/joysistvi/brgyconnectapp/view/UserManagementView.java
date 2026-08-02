package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.UserAdminController;
import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;

import java.io.Console;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UserManagementView {
    private final Scanner scanner;
    private final UserAdminController userController;

    public UserManagementView(Scanner scanner, UserAdminController userController) {
        this.scanner = scanner;
        this.userController = userController;
    }

    public void show(User actingAdmin) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("User Account Management");
            ConsoleUI.printMenuOption("1", "List or search user accounts");
            ConsoleUI.printMenuOption("2", "View user account details");
            ConsoleUI.printMenuOption("3", "Create user account");
            ConsoleUI.printMenuOption("4", "Change user role");
            ConsoleUI.printMenuOption("5", "Change account status");
            ConsoleUI.printMenuOption("6", "Unlock user account");
            ConsoleUI.printMenuOption("7", "Reset user password");
            ConsoleUI.printMenuOption("0", "Back");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> searchUsers(actingAdmin);
                case "2" -> viewUser(actingAdmin);
                case "3" -> createUser(actingAdmin);
                case "4" -> changeRole(actingAdmin);
                case "5" -> changeStatus(actingAdmin);
                case "6" -> unlockUser(actingAdmin);
                case "7" -> resetPassword(actingAdmin);
                case "0" -> { }
                default -> ConsoleUI.printError("Please choose a valid menu option.");
            }

            if (!choice.equals("0")) {
                pause();
            }
        } while (!choice.equals("0"));
    }

    private void searchUsers(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("User Accounts");
        ConsoleUI.printInfo("Leave the search blank to list all accounts.");
        ConsoleUI.printPrompt("Username, display name, role, or status: ");
        printUsers(userController.search(scanner.nextLine().trim(), userId(actingAdmin)));
    }

    private void viewUser(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("User Account Details");
        int userId = promptPositiveInteger("User ID: ");
        User user = userController.getById(userId, userId(actingAdmin));
        if (user == null) {
            ConsoleUI.printError("User account not found.");
            return;
        }
        printUserDetails(user);
    }

    private void createUser(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Create User Account");

        User user = new User();
        user.setUsername(promptRequired("Username: ", 50));
        user.setDisplayName(promptRequired("Display name: ", 150));
        user.setRole(promptRole());
        if (user.getRole() == UserRole.RESIDENT) {
            user.setResidentId(promptPositiveInteger("Resident ID: "));
        }

        char[] password = readConfirmedPassword();
        if (password == null) {
            return;
        }
        if (!promptYesNo("Create this account? (Y/N): ")) {
            Arrays.fill(password, '\0');
            ConsoleUI.printInfo("Account creation cancelled.");
            return;
        }
        printOperationResult(userController.create(user, password, userId(actingAdmin)));
    }

    private void changeRole(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Change User Role");
        int userId = promptPositiveInteger("User ID: ");
        User target = userController.getById(userId, userId(actingAdmin));
        if (target == null) {
            ConsoleUI.printError("User account not found.");
            return;
        }

        printUserDetails(target);
        UserRole newRole = promptRole();
        if (!promptYesNo("Change role to " + formatEnum(newRole) + "? (Y/N): ")) {
            ConsoleUI.printInfo("Role change cancelled.");
            return;
        }
        printOperationResult(userController.changeRole(
                userId,
                newRole,
                actingAdmin.getUserId() == null ? 0 : actingAdmin.getUserId()
        ));
    }

    private void changeStatus(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Change Account Status");
        int userId = promptPositiveInteger("User ID: ");
        User target = userController.getById(userId, userId(actingAdmin));
        if (target == null) {
            ConsoleUI.printError("User account not found.");
            return;
        }

        printUserDetails(target);
        AccountStatus newStatus = promptAccountStatus();
        if (!promptYesNo("Change status to " + formatEnum(newStatus) + "? (Y/N): ")) {
            ConsoleUI.printInfo("Status change cancelled.");
            return;
        }
        printOperationResult(userController.changeStatus(
                userId,
                newStatus,
                actingAdmin.getUserId() == null ? 0 : actingAdmin.getUserId()
        ));
    }

    private void unlockUser(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Unlock User Account");
        int userId = promptPositiveInteger("User ID: ");
        User target = userController.getById(userId, userId(actingAdmin));
        if (target == null) {
            ConsoleUI.printError("User account not found.");
            return;
        }
        printUserDetails(target);
        if (!promptYesNo("Unlock this account? (Y/N): ")) {
            ConsoleUI.printInfo("Unlock cancelled.");
            return;
        }
        printOperationResult(userController.unlock(userId, userId(actingAdmin)));
    }

    private void resetPassword(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Reset User Password");
        int userId = promptPositiveInteger("User ID: ");
        User target = userController.getById(userId, userId(actingAdmin));
        if (target == null) {
            ConsoleUI.printError("User account not found.");
            return;
        }
        ConsoleUI.printInfo("Resetting password for " + target.getDisplayName() + " (" + target.getUsername() + ")");

        char[] password = readConfirmedPassword();
        if (password == null) {
            return;
        }
        if (!promptYesNo("Save the new password? (Y/N): ")) {
            Arrays.fill(password, '\0');
            ConsoleUI.printInfo("Password reset cancelled.");
            return;
        }
        printOperationResult(userController.resetPassword(userId, password, userId(actingAdmin)));
    }

    private int userId(User user) {
        return user == null || user.getUserId() == null ? 0 : user.getUserId();
    }

    private void printUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            ConsoleUI.printInfo("No user accounts found.");
            return;
        }

        TableFormatter formatter = new TableFormatter("ID", "Username", "Display Name", "Role", "Status", "Resident");
        for (User user : users) {
            formatter.addRow(
                    String.valueOf(user.getUserId()),
                    abbreviate(user.getUsername(), 18),
                    abbreviate(user.getDisplayName(), 28),
                    String.valueOf(user.getRole()),
                    String.valueOf(user.getAccountStatus()),
                    user.getResidentId() == null ? "-" : String.valueOf(user.getResidentId()));
        }
        formatter.print();
        System.out.println();
        ConsoleUI.printInfo(users.size() + " account(s) found.");
    }

    private void printUserDetails(User user) {
        System.out.printf("User ID         : %s%n", user.getUserId());
        System.out.printf("Username        : %s%n", user.getUsername());
        System.out.printf("Display Name    : %s%n", user.getDisplayName());
        System.out.printf("Role            : %s%n", formatEnum(user.getRole()));
        System.out.printf("Account Status  : %s%n", formatEnum(user.getAccountStatus()));
        System.out.printf("Resident ID     : %s%n", user.getResidentId() == null ? "-" : user.getResidentId());
        System.out.printf("Failed Attempts : %s%n", user.getFailedLoginAttempts());
        System.out.printf("Last Login      : %s%n", user.getLastLoginAt() == null ? "Never" : user.getLastLoginAt());
        System.out.printf("Created At      : %s%n", user.getCreatedAt());
        System.out.printf("Updated At      : %s%n", user.getUpdatedAt());
    }

    private UserRole promptRole() {
        return promptEnum("Role", UserRole.values());
    }

    private AccountStatus promptAccountStatus() {
        AccountStatus[] statuses = {
                AccountStatus.ACTIVE,
                AccountStatus.PENDING_ACTIVATION,
                AccountStatus.INACTIVE
        };
        return promptEnum("Account status", statuses);
    }

    private <E extends Enum<E>> E promptEnum(String label, E[] values) {
        System.out.println();
        ConsoleUI.printSubHeader(label);
        for (int index = 0; index < values.length; index++) {
            ConsoleUI.printMenuOption(String.valueOf(index + 1), formatEnum(values[index]));
        }
        while (true) {
            ConsoleUI.printPrompt("Select " + label.toLowerCase() + ": ");
            try {
                int selected = Integer.parseInt(scanner.nextLine().trim());
                if (selected >= 1 && selected <= values.length) {
                    return values[selected - 1];
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Choose a number from 1 to " + values.length + ".");
        }
    }

    private char[] readConfirmedPassword() {
        char[] password = readPassword("New password: ");
        char[] confirmation = readPassword("Confirm password: ");
        boolean matches = Arrays.equals(password, confirmation);
        Arrays.fill(confirmation, '\0');
        if (!matches) {
            Arrays.fill(password, '\0');
            ConsoleUI.printError("Passwords do not match.");
            return null;
        }
        return password;
    }

    private char[] readPassword(String label) {
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword(label);
            return password == null ? new char[0] : password;
        }
        ConsoleUI.printPrompt(label);
        return scanner.nextLine().toCharArray();
    }

    private String promptRequired(String label, int maximumLength) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String value = scanner.nextLine().trim();
            if (value.isBlank()) {
                ConsoleUI.printError("This field is required.");
            } else if (value.length() > maximumLength) {
                ConsoleUI.printError("Maximum length is " + maximumLength + " characters.");
            } else {
                return value;
            }
        }
    }

    private int promptPositiveInteger(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
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
        } else if (result != null && (result.toLowerCase().contains("already") ||
                result.toLowerCase().startsWith("only locked"))) {
            ConsoleUI.printInfo(result);
        } else {
            ConsoleUI.printError(result == null ? "The operation could not be completed." : result);
        }
    }

    private String formatEnum(Enum<?> value) {
        String name = value.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String abbreviate(String value, int maximumLength) {
        if (value.length() <= maximumLength) {
            return value;
        }
        return value.substring(0, maximumLength - 3) + "...";
    }

    private void pause() {
        System.out.println();
        ConsoleUI.printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
