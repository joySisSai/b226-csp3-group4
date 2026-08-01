package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ActivityLogController;
import com.joysistvi.brgyconnectapp.model.ActivityLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Console UI for viewing activity history.
 * Accessible only to Admin/Staff.
 */
public class ActivityLogView {
    private final Scanner scanner;
    private final ActivityLogController controller;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ActivityLogView(Scanner scanner) {
        this.scanner = scanner;
        this.controller = new ActivityLogController();
    }

    // Main menu for log viewing
    public void show() {
        String choice;
        do {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Activity Logs");
            ConsoleUI.printMenuOption("1", "View All Activity");
            ConsoleUI.printMenuOption("2", "View by Date Range");
            ConsoleUI.printMenuOption("0", "Back");
            ConsoleUI.printPrompt("Select option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": showAllLogs(); break;
                case "2": showByRange(); break;
                case "0": break;
                default: ConsoleUI.printError("Invalid choice");
            }
            if (!choice.equals("0")) {
                ConsoleUI.printPrompt("\nPress Enter to continue...");
                scanner.nextLine();
            }
        } while (!choice.equals("0"));
    }

    // Display full list of all actions
    private void showAllLogs() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("All Activity Logs");
        List<ActivityLog> logs = controller.viewAllLogs();
        if (logs.isEmpty()) {
            ConsoleUI.printInfo("No activity records found");
            return;
        }
        for (ActivityLog log : logs) {
            System.out.printf("[%s] User %d | %s | %s #%s | %s%n",
                    log.getCreatedAt(), log.getUserId(), log.getAction(),
                    log.getEntityType(), log.getEntityId() == null ? "-" : log.getEntityId(),
                    log.getDescription());
        }
    }

    // Filter logs between two dates
    private void showByRange() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Logs by Date Range");
        ConsoleUI.printPrompt("Start date (yyyy-MM-dd): ");
        LocalDate startDate = LocalDate.parse(scanner.nextLine().trim(), DATE_FMT);
        ConsoleUI.printPrompt("End date (yyyy-MM-dd): ");
        LocalDate endDate = LocalDate.parse(scanner.nextLine().trim(), DATE_FMT);

        // Convert date to full date-time for database query
        List<ActivityLog> logs = controller.viewLogsByRange(startDate.atStartOfDay(), endDate.atTime(23,59,59));
        if (logs.isEmpty()) {
            ConsoleUI.printInfo("No activity found in this period");
            return;
        }
        for (ActivityLog log : logs) {
            System.out.printf("[%s] User %d | %s | %s #%s | %s%n",
                    log.getCreatedAt(), log.getUserId(), log.getAction(),
                    log.getEntityType(), log.getEntityId() == null ? "-" : log.getEntityId(),
                    log.getDescription());
        }
    }
}