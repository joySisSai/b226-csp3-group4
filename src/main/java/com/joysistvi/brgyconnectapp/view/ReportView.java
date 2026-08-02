package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ReportController;
import com.joysistvi.brgyconnectapp.model.HouseholdReportRow;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ResidentReportRow;
import com.joysistvi.brgyconnectapp.model.ServiceRequestReportRow;
import com.joysistvi.brgyconnectapp.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ReportView {
    private final Scanner scanner;
    private final ReportController reportController;

    public ReportView(Scanner scanner, ReportController reportController) {
        this.scanner = scanner;
        this.reportController = reportController;
    }

    public void show(User actingUser) {
        String choice;
        do {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Reports");
            ConsoleUI.printMenuOption("1", "Resident summary by purok");
            ConsoleUI.printMenuOption("2", "Registered voter summary by purok");
            ConsoleUI.printMenuOption("3", "Household summary by purok");
            ConsoleUI.printMenuOption("4", "Service request summary");
            ConsoleUI.printMenuOption("0", "Back");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");
            choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showResidentSummary(false, actingUser);
                case "2" -> showResidentSummary(true, actingUser);
                case "3" -> showHouseholdSummary(actingUser);
                case "4" -> showServiceRequestSummary(actingUser);
                case "0" -> { }
                default -> ConsoleUI.printError("Please choose a valid menu option.");
            }

            if (!choice.equals("0")) {
                pause();
            }
        } while (!choice.equals("0"));
    }

    private void showResidentSummary(boolean votersOnly, User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader(votersOnly ? "Registered Voter Summary" : "Resident Summary");
        String purok = promptOptionalFilter("Purok (leave blank for all): ");
        List<ResidentReportRow> rows = reportController.getResidentSummary(purok, userId(actingUser));
        if (rows.isEmpty()) {
            ConsoleUI.printInfo("No resident data matched the selected filter.");
            return;
        }

        if (votersOnly) {
            printVoterRows(rows);
        } else {
            printResidentRows(rows);
        }
    }

    private void showHouseholdSummary(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Household Summary");
        String purok = promptOptionalFilter("Purok (leave blank for all): ");
        List<HouseholdReportRow> rows = reportController.getHouseholdSummary(
                purok, userId(actingUser));
        if (rows.isEmpty()) {
            ConsoleUI.printInfo("No household data matched the selected filter.");
            return;
        }

        System.out.printf("%-20s %10s %10s %10s %10s%n",
                "Purok", "Total", "Active", "Inactive", "Members");
        System.out.println("-".repeat(66));
        long total = 0;
        long active = 0;
        long inactive = 0;
        long members = 0;
        for (HouseholdReportRow row : rows) {
            System.out.printf("%-20s %10d %10d %10d %10d%n",
                    row.purok(),
                    row.totalHouseholds(),
                    row.activeHouseholds(),
                    row.inactiveHouseholds(),
                    row.totalMembers());
            total += row.totalHouseholds();
            active += row.activeHouseholds();
            inactive += row.inactiveHouseholds();
            members += row.totalMembers();
        }
        System.out.println("-".repeat(66));
        System.out.printf("%-20s %10d %10d %10d %10d%n", "TOTAL", total, active, inactive, members);
    }

    private void showServiceRequestSummary(User actingUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Service Request Summary");
        LocalDate today = LocalDate.now();
        LocalDate startDate = promptDate("Start date", today.withDayOfMonth(1));
        LocalDate endDate = promptDate("End date", today);
        if (startDate.isAfter(endDate)) {
            ConsoleUI.printError("Start date cannot be after the end date.");
            return;
        }
        RequestStatus status = promptStatusFilter();

        List<ServiceRequestReportRow> rows = reportController.getServiceRequestSummary(
                startDate,
                endDate,
                status,
                userId(actingUser)
        );
        if (rows.isEmpty()) {
            ConsoleUI.printInfo("No service-request data matched the selected filter.");
            return;
        }

        ConsoleUI.printInfo("Period: " + startDate + " to " + endDate +
                " | Status: " + (status == null ? "All" : formatEnum(status)));
        System.out.printf("%-26s %6s %6s %7s %6s %6s %6s %6s %12s%n",
                "Service", "Total", "Pend", "Review", "Appr", "Rel", "Rej", "Canc", "Fees");
        System.out.println("-".repeat(92));

        long total = 0;
        long pending = 0;
        long review = 0;
        long approved = 0;
        long released = 0;
        long rejected = 0;
        long cancelled = 0;
        BigDecimal fees = BigDecimal.ZERO;
        for (ServiceRequestReportRow row : rows) {
            System.out.printf("%-26s %6d %6d %7d %6d %6d %6d %6d %12s%n",
                    abbreviate(row.serviceName(), 26),
                    row.totalRequests(),
                    row.pendingRequests(),
                    row.underReviewRequests(),
                    row.approvedRequests(),
                    row.releasedRequests(),
                    row.rejectedRequests(),
                    row.cancelledRequests(),
                    row.totalFees());
            total += row.totalRequests();
            pending += row.pendingRequests();
            review += row.underReviewRequests();
            approved += row.approvedRequests();
            released += row.releasedRequests();
            rejected += row.rejectedRequests();
            cancelled += row.cancelledRequests();
            fees = fees.add(row.totalFees());
        }
        System.out.println("-".repeat(92));
        System.out.printf("%-26s %6d %6d %7d %6d %6d %6d %6d %12s%n",
                "TOTAL", total, pending, review, approved, released, rejected, cancelled, fees);
    }

    private void printResidentRows(List<ResidentReportRow> rows) {
        System.out.printf("%-20s %8s %8s %8s %8s %8s %8s%n",
                "Purok", "Total", "Active", "Moved", "Deceased", "Inactive", "Voters");
        System.out.println("-".repeat(78));
        long total = 0;
        long active = 0;
        long transferred = 0;
        long deceased = 0;
        long inactive = 0;
        long voters = 0;
        for (ResidentReportRow row : rows) {
            System.out.printf("%-20s %8d %8d %8d %8d %8d %8d%n",
                    row.purok(),
                    row.totalResidents(),
                    row.activeResidents(),
                    row.transferredResidents(),
                    row.deceasedResidents(),
                    row.inactiveResidents(),
                    row.registeredVoters());
            total += row.totalResidents();
            active += row.activeResidents();
            transferred += row.transferredResidents();
            deceased += row.deceasedResidents();
            inactive += row.inactiveResidents();
            voters += row.registeredVoters();
        }
        System.out.println("-".repeat(78));
        System.out.printf("%-20s %8d %8d %8d %8d %8d %8d%n",
                "TOTAL", total, active, transferred, deceased, inactive, voters);
    }

    private void printVoterRows(List<ResidentReportRow> rows) {
        System.out.printf("%-24s %12s %12s %12s%n", "Purok", "Residents", "Voters", "Percent");
        System.out.println("-".repeat(64));
        long residents = 0;
        long voters = 0;
        for (ResidentReportRow row : rows) {
            System.out.printf("%-24s %12d %12d %11.1f%%%n",
                    row.purok(),
                    row.totalResidents(),
                    row.registeredVoters(),
                    percentage(row.registeredVoters(), row.totalResidents()));
            residents += row.totalResidents();
            voters += row.registeredVoters();
        }
        System.out.println("-".repeat(64));
        System.out.printf("%-24s %12d %12d %11.1f%%%n",
                "TOTAL", residents, voters, percentage(voters, residents));
    }

    private String promptOptionalFilter(String label) {
        ConsoleUI.printPrompt(label);
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    private LocalDate promptDate(String label, LocalDate defaultValue) {
        while (true) {
            ConsoleUI.printPrompt(label + " [" + defaultValue + "]: ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return defaultValue;
            }
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ignored) {
                ConsoleUI.printError("Enter a valid date using YYYY-MM-DD.");
            }
        }
    }

    private RequestStatus promptStatusFilter() {
        System.out.println();
        ConsoleUI.printSubHeader("Status Filter");
        ConsoleUI.printMenuOption("0", "All statuses");
        RequestStatus[] statuses = RequestStatus.values();
        for (int index = 0; index < statuses.length; index++) {
            ConsoleUI.printMenuOption(String.valueOf(index + 1), formatEnum(statuses[index]));
        }

        while (true) {
            ConsoleUI.printPrompt("Select status: ");
            try {
                int selected = Integer.parseInt(scanner.nextLine().trim());
                if (selected == 0) {
                    return null;
                }
                if (selected >= 1 && selected <= statuses.length) {
                    return statuses[selected - 1];
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Choose a number from 0 to " + statuses.length + ".");
        }
    }

    private double percentage(long part, long whole) {
        return whole == 0 ? 0.0 : (part * 100.0) / whole;
    }

    private int userId(User user) {
        return user == null || user.getUserId() == null ? 0 : user.getUserId();
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
