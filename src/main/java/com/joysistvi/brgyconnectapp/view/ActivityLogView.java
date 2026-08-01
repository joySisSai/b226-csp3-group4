package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ActivityLogController;
import com.joysistvi.brgyconnectapp.model.ActivityLog;
import com.joysistvi.brgyconnectapp.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ActivityLogView {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Scanner scanner;
    private final ActivityLogController activityLogController;

    public ActivityLogView(Scanner scanner, ActivityLogController activityLogController) {
        this.scanner = scanner;
        this.activityLogController = activityLogController;
    }

    public void show(User actingAdmin) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Activity Log");
            ConsoleUI.printMenuOption("1", "View recent activity");
            ConsoleUI.printMenuOption("2", "Filter activity log");
            ConsoleUI.printMenuOption("3", "View activity details");
            ConsoleUI.printMenuOption("0", "Back");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> printLogs(activityLogController.search(
                        userId(actingAdmin), null, null, null, null, null));
                case "2" -> filterLogs(actingAdmin);
                case "3" -> viewDetails(actingAdmin);
                case "0" -> { }
                default -> ConsoleUI.printError("Please choose a valid menu option.");
            }

            if (!choice.equals("0")) {
                pause();
            }
        } while (!choice.equals("0"));
    }

    private void filterLogs(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Filter Activity Log");
        ConsoleUI.printInfo("Leave any filter blank to include all values.");
        Integer userId = promptOptionalPositiveInteger("User ID: ");
        String action = promptOptional("Action (example: CREATE or UPDATE_STATUS): ");
        String entityType = promptOptional("Entity type (example: RESIDENT): ");
        LocalDate dateFrom = promptOptionalDate("Start date (YYYY-MM-DD): ");
        LocalDate dateTo = promptOptionalDate("End date (YYYY-MM-DD): ");
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            ConsoleUI.printError("Start date cannot be after end date.");
            return;
        }
        printLogs(activityLogController.search(
                userId(actingAdmin), userId, action, entityType, dateFrom, dateTo));
    }

    private void printLogs(List<ActivityLog> logs) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Activity Log Results");
        if (logs == null || logs.isEmpty()) {
            ConsoleUI.printInfo("No activity log entries found.");
            return;
        }

        System.out.printf("%-6s %-19s %-18s %-18s %-17s %-9s %-42s%n",
                "ID", "Date", "Actor", "Action", "Entity", "Entity ID", "Description");
        System.out.println("-".repeat(137));
        for (ActivityLog log : logs) {
            System.out.printf("%-6s %-19s %-18s %-18s %-17s %-9s %-42s%n",
                    log.getActivityLogId(),
                    log.getCreatedAt() == null ? "-" : log.getCreatedAt().format(DATE_TIME_FORMAT),
                    abbreviate(actorLabel(log), 18),
                    abbreviate(log.getAction(), 18),
                    abbreviate(log.getEntityType(), 17),
                    log.getEntityId() == null ? "-" : log.getEntityId(),
                    abbreviate(log.getDescription(), 42));
        }
        System.out.println();
        ConsoleUI.printInfo(logs.size() + " record(s) found; newest entries are shown first.");
    }

    private void viewDetails(User actingAdmin) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Activity Log Details");
        long activityLogId = promptPositiveLong("Activity log ID: ");
        ActivityLog log = activityLogController.getById(activityLogId, userId(actingAdmin));
        if (log == null) {
            ConsoleUI.printError("Activity log entry not found.");
            return;
        }

        System.out.printf("Activity ID : %s%n", log.getActivityLogId());
        System.out.printf("Date        : %s%n",
                log.getCreatedAt() == null ? "-" : log.getCreatedAt().format(DATE_TIME_FORMAT));
        System.out.printf("User ID     : %s%n", log.getUserId() == null ? "-" : log.getUserId());
        System.out.printf("Actor       : %s%n", actorLabel(log));
        System.out.printf("Display Name: %s%n", valueOrDash(log.getActorDisplayName()));
        System.out.printf("Action      : %s%n", valueOrDash(log.getAction()));
        System.out.printf("Entity Type : %s%n", valueOrDash(log.getEntityType()));
        System.out.printf("Entity ID   : %s%n", log.getEntityId() == null ? "-" : log.getEntityId());
        System.out.printf("Description : %s%n", valueOrDash(log.getDescription()));
    }

    private String actorLabel(ActivityLog log) {
        if (log.getActorUsername() != null && !log.getActorUsername().isBlank()) {
            return log.getActorUsername();
        }
        return log.getUserId() == null ? "System" : "User " + log.getUserId();
    }

    private int userId(User user) {
        return user == null || user.getUserId() == null ? 0 : user.getUserId();
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
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number or leave it blank.");
        }
    }

    private long promptPositiveLong(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            try {
                long value = Long.parseLong(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number.");
        }
    }

    private LocalDate promptOptionalDate(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException ignored) {
                ConsoleUI.printError("Enter a valid date using YYYY-MM-DD or leave it blank.");
            }
        }
    }

    private String promptOptional(String label) {
        ConsoleUI.printPrompt(label);
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    private String abbreviate(Object value, int maximumLength) {
        String text = value == null || value.toString().isBlank() ? "-" : value.toString();
        return text.length() <= maximumLength
                ? text
                : text.substring(0, maximumLength - 3) + "...";
    }

    private String valueOrDash(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }

    private void pause() {
        System.out.println();
        ConsoleUI.printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
