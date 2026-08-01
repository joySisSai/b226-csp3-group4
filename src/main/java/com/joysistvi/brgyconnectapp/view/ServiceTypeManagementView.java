package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ServiceTypeAdminController;
import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ServiceTypeManagementView {
    private final Scanner scanner;
    private final ServiceTypeAdminController serviceTypeController;

    public ServiceTypeManagementView(Scanner scanner, ServiceTypeAdminController serviceTypeController) {
        this.scanner = scanner;
        this.serviceTypeController = serviceTypeController;
    }

    public void show(User actingAdmin) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Service Type Management");
            ConsoleUI.printMenuOption("1", "List service types");
            ConsoleUI.printMenuOption("2", "View service type details");
            ConsoleUI.printMenuOption("3", "Create service type");
            ConsoleUI.printMenuOption("4", "Update service type");
            ConsoleUI.printMenuOption("5", "Activate or deactivate service type");
            ConsoleUI.printMenuOption("0", "Back");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listServiceTypes();
                case "2" -> viewServiceType();
                case "3" -> createServiceType(actingAdmin);
                case "4" -> updateServiceType(actingAdmin);
                case "5" -> changeServiceTypeStatus(actingAdmin);
                case "0" -> { }
                default -> ConsoleUI.printError("Please choose a valid menu option.");
            }

            if (!choice.equals("0")) {
                pause();
            }
        } while (!choice.equals("0"));
    }

    private void listServiceTypes() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Service Types");
        printServiceTypes(serviceTypeController.getAll());
    }

    private void viewServiceType() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Service Type Details");
        int serviceTypeId = promptPositiveInteger("Service type ID: ");
        ServiceType serviceType = serviceTypeController.getById(serviceTypeId);
        if (serviceType == null) {
            ConsoleUI.printError("Service type not found.");
            return;
        }
        printServiceTypeDetails(serviceType);
    }

    private void createServiceType(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Create Service Type");
        ServiceType serviceType = new ServiceType();
        serviceType.setServiceCode(promptRequired("Service code: ", 30));
        serviceType.setServiceName(promptRequired("Service name: ", 120));
        serviceType.setDescription(promptOptional("Description (optional): ", 500));
        serviceType.setDefaultFee(promptMoney("Default fee: "));
        serviceType.setExpectedProcessingDays(promptNonnegativeInteger("Expected processing days: "));

        if (!promptYesNo("Create this service type? (Y/N): ")) {
            ConsoleUI.printInfo("Service-type creation cancelled.");
            return;
        }
        printOperationResult(serviceTypeController.create(serviceType, userId(actingAdmin)));
    }

    private void updateServiceType(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Update Service Type");
        int serviceTypeId = promptPositiveInteger("Service type ID: ");
        ServiceType serviceType = serviceTypeController.getById(serviceTypeId);
        if (serviceType == null) {
            ConsoleUI.printError("Service type not found.");
            return;
        }

        ConsoleUI.printInfo("Service code: " + serviceType.getServiceCode());
        ConsoleUI.printInfo("Press Enter to keep the current value.");
        serviceType.setServiceName(promptTextUpdate(
                "Service name", serviceType.getServiceName(), 120));
        serviceType.setDescription(promptTextUpdate(
                "Description", serviceType.getDescription(), 500));
        serviceType.setDefaultFee(promptMoneyUpdate(
                "Default fee", serviceType.getDefaultFee()));
        serviceType.setExpectedProcessingDays(promptIntegerUpdate(
                "Expected processing days", serviceType.getExpectedProcessingDays()));

        if (!promptYesNo("Save these changes? (Y/N): ")) {
            ConsoleUI.printInfo("Service-type update cancelled.");
            return;
        }
        printOperationResult(serviceTypeController.update(serviceType, userId(actingAdmin)));
    }

    private void changeServiceTypeStatus(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Service Type Status");
        int serviceTypeId = promptPositiveInteger("Service type ID: ");
        ServiceType serviceType = serviceTypeController.getById(serviceTypeId);
        if (serviceType == null) {
            ConsoleUI.printError("Service type not found.");
            return;
        }

        printServiceTypeDetails(serviceType);
        boolean newStatus = !serviceType.isActive();
        String action = newStatus ? "activate" : "deactivate";
        System.out.println();
        if (!promptYesNo("Confirm " + action + "? (Y/N): ")) {
            ConsoleUI.printInfo("Status change cancelled.");
            return;
        }
        printOperationResult(serviceTypeController.setActive(serviceTypeId, newStatus, userId(actingAdmin)));
    }

    private int userId(User user) {
        return user == null || user.getUserId() == null ? 0 : user.getUserId();
    }

    private void printServiceTypes(List<ServiceType> serviceTypes) {
        if (serviceTypes == null || serviceTypes.isEmpty()) {
            ConsoleUI.printInfo("No service types found.");
            return;
        }

        System.out.printf("%-6s %-16s %-30s %-12s %-8s %-10s%n",
                "ID", "Code", "Service", "Fee", "Days", "Status");
        System.out.println("-".repeat(88));
        for (ServiceType serviceType : serviceTypes) {
            System.out.printf("%-6s %-16s %-30s %-12s %-8s %-10s%n",
                    serviceType.getServiceTypeId(),
                    abbreviate(serviceType.getServiceCode(), 16),
                    abbreviate(serviceType.getServiceName(), 30),
                    "PHP " + serviceType.getDefaultFee(),
                    serviceType.getExpectedProcessingDays(),
                    serviceType.isActive() ? "Active" : "Inactive");
        }
        System.out.println();
        ConsoleUI.printInfo(serviceTypes.size() + " service type(s) found.");
    }

    private void printServiceTypeDetails(ServiceType serviceType) {
        System.out.printf("Service Type ID : %s%n", serviceType.getServiceTypeId());
        System.out.printf("Service Code    : %s%n", serviceType.getServiceCode());
        System.out.printf("Service Name    : %s%n", serviceType.getServiceName());
        System.out.printf("Description     : %s%n", valueOrDash(serviceType.getDescription()));
        System.out.printf("Default Fee     : PHP %s%n", serviceType.getDefaultFee());
        System.out.printf("Processing Days : %s%n", serviceType.getExpectedProcessingDays());
        System.out.printf("Status          : %s%n", serviceType.isActive() ? "Active" : "Inactive");
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

    private String promptOptional(String label, int maximumLength) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return null;
            }
            if (value.length() <= maximumLength) {
                return value;
            }
            ConsoleUI.printError("Maximum length is " + maximumLength + " characters.");
        }
    }

    private String promptTextUpdate(String label, String currentValue, int maximumLength) {
        while (true) {
            ConsoleUI.printPrompt(label + " [" + valueOrDash(currentValue) + "]: ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return currentValue;
            }
            if (value.length() <= maximumLength) {
                return value;
            }
            ConsoleUI.printError("Maximum length is " + maximumLength + " characters.");
        }
    }

    private BigDecimal promptMoney(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            try {
                BigDecimal value = new BigDecimal(scanner.nextLine().trim());
                if (isValidMoney(value)) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter an amount from 0.00 to 99999999.99.");
        }
    }

    private BigDecimal promptMoneyUpdate(String label, BigDecimal currentValue) {
        while (true) {
            ConsoleUI.printPrompt(label + " [" + currentValue + "]: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return currentValue;
            }
            try {
                BigDecimal value = new BigDecimal(input);
                if (isValidMoney(value)) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter an amount from 0.00 to 99999999.99.");
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

    private int promptNonnegativeInteger(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= 0 && value <= 65_535) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a whole number from 0 to 65535.");
        }
    }

    private int promptIntegerUpdate(String label, int currentValue) {
        while (true) {
            ConsoleUI.printPrompt(label + " [" + currentValue + "]: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return currentValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= 0 && value <= 65_535) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a whole number from 0 to 65535.");
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

    private boolean isValidMoney(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) >= 0 &&
                value.compareTo(new BigDecimal("99999999.99")) <= 0;
    }

    private void printOperationResult(String result) {
        if (result != null && result.toLowerCase().contains("successfully")) {
            ConsoleUI.printSuccess(result);
        } else if (result != null && result.toLowerCase().contains("already")) {
            ConsoleUI.printInfo(result);
        } else {
            ConsoleUI.printError(result == null ? "The operation could not be completed." : result);
        }
    }

    private String valueOrDash(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
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
