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
                case "1" -> viewPaginatedLogs(actingAdmin, null, null, null, null, null);
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
        viewPaginatedLogs(actingAdmin, userId, action, entityType, dateFrom, dateTo);
    }

    private void viewPaginatedLogs(User actingAdmin, Integer userId, String action, String entityType, LocalDate dateFrom, LocalDate dateTo) {
        int page = 1;
        int pageSize = 10;
        
        while (true) {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Activity Log Results - Page " + page);
            List<ActivityLog> logs = activityLogController.search(userId(actingAdmin), userId, action, entityType, dateFrom, dateTo, (page - 1) * pageSize, pageSize);
            
            if (logs.isEmpty() && page == 1) {
                ConsoleUI.printInfo("No activity log entries found.");
                break;
            }

            if (!logs.isEmpty()) {
                TableFormatter formatter = new TableFormatter("ID", "Date", "Actor", "Action", "Entity", "Entity ID", "Description");
                for (ActivityLog log : logs) {
                    formatter.addRow(
                            String.valueOf(log.getActivityLogId()),
                            log.getCreatedAt() == null ? "-" : log.getCreatedAt().format(DATE_TIME_FORMAT),
                            abbreviate(actorLabel(log), 18),
                            abbreviate(log.getAction(), 18),
                            abbreviate(log.getEntityType(), 17),
                            log.getEntityId() == null ? "-" : String.valueOf(log.getEntityId()),
                            abbreviate(log.getDescription(), 42));
                }
                formatter.print();
            }
            
            System.out.println();
            ConsoleUI.printInfo("N - Next Page | P - Previous Page | Q - Quit to Menu");
            ConsoleUI.printPrompt("Choose an option: ");
            String opt = scanner.nextLine().trim().toUpperCase();
            
            if (opt.equals("N")) {
                if (logs.size() == pageSize) {
                    page++;
                } else {
                    ConsoleUI.printInfo("You are already on the last page.");
                    pause();
                }
            } else if (opt.equals("P")) {
                if (page > 1) {
                    page--;
                } else {
                    ConsoleUI.printInfo("You are already on the first page.");
                    pause();
                }
            } else if (opt.equals("Q") || opt.equals("0")) {
                break;
            } else {
                ConsoleUI.printError("Invalid option.");
                pause();
            }
        }
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
