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

        TableFormatter formatter = new TableFormatter("Purok", "Total", "Active", "Inactive", "Members");
        long total = 0;
        long active = 0;
        long inactive = 0;
        long members = 0;
        for (HouseholdReportRow row : rows) {
            formatter.addRow(
                    row.purok(),
                    String.valueOf(row.totalHouseholds()),
                    String.valueOf(row.activeHouseholds()),
                    String.valueOf(row.inactiveHouseholds()),
                    String.valueOf(row.totalMembers()));
            total += row.totalHouseholds();
            active += row.activeHouseholds();
            inactive += row.inactiveHouseholds();
            members += row.totalMembers();
        }
        formatter.addRow("TOTAL", String.valueOf(total), String.valueOf(active), String.valueOf(inactive), String.valueOf(members));
        formatter.print();
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
        TableFormatter formatter = new TableFormatter("Service", "Total", "Pend", "Review", "Appr", "Rel", "Rej", "Canc", "Fees");
        long total = 0;
        long pending = 0;
        long review = 0;
        long approved = 0;
        long released = 0;
        long rejected = 0;
        long cancelled = 0;
        BigDecimal fees = BigDecimal.ZERO;
        for (ServiceRequestReportRow row : rows) {
            formatter.addRow(
                    abbreviate(row.serviceName(), 26),
                    String.valueOf(row.totalRequests()),
                    String.valueOf(row.pendingRequests()),
                    String.valueOf(row.underReviewRequests()),
                    String.valueOf(row.approvedRequests()),
                    String.valueOf(row.releasedRequests()),
                    String.valueOf(row.rejectedRequests()),
                    String.valueOf(row.cancelledRequests()),
                    String.valueOf(row.totalFees()));
            total += row.totalRequests();
            pending += row.pendingRequests();
            review += row.underReviewRequests();
            approved += row.approvedRequests();
            released += row.releasedRequests();
            rejected += row.rejectedRequests();
            cancelled += row.cancelledRequests();
            fees = fees.add(row.totalFees());
        }
        formatter.addRow("TOTAL", String.valueOf(total), String.valueOf(pending), String.valueOf(review), String.valueOf(approved), String.valueOf(released), String.valueOf(rejected), String.valueOf(cancelled), String.valueOf(fees));
        formatter.print();
    }

    private void printResidentRows(List<ResidentReportRow> rows) {
        TableFormatter formatter = new TableFormatter("Purok", "Total", "Active", "Moved", "Deceased", "Inactive", "Voters");
        long total = 0;
        long active = 0;
        long transferred = 0;
        long deceased = 0;
        long inactive = 0;
        long voters = 0;
        for (ResidentReportRow row : rows) {
            formatter.addRow(
                    row.purok(),
                    String.valueOf(row.totalResidents()),
                    String.valueOf(row.activeResidents()),
                    String.valueOf(row.transferredResidents()),
                    String.valueOf(row.deceasedResidents()),
                    String.valueOf(row.inactiveResidents()),
                    String.valueOf(row.registeredVoters()));
            total += row.totalResidents();
            active += row.activeResidents();
            transferred += row.transferredResidents();
            deceased += row.deceasedResidents();
            inactive += row.inactiveResidents();
            voters += row.registeredVoters();
        }
        formatter.addRow("TOTAL", String.valueOf(total), String.valueOf(active), String.valueOf(transferred), String.valueOf(deceased), String.valueOf(inactive), String.valueOf(voters));
        formatter.print();
    }

    private void printVoterRows(List<ResidentReportRow> rows) {
        TableFormatter formatter = new TableFormatter("Purok", "Residents", "Voters", "Percent");
        long residents = 0;
        long voters = 0;
        for (ResidentReportRow row : rows) {
            formatter.addRow(
                    row.purok(),
                    String.valueOf(row.totalResidents()),
                    String.valueOf(row.registeredVoters()),
                    String.format("%.1f%%", percentage(row.registeredVoters(), row.totalResidents())));
            residents += row.totalResidents();
            voters += row.registeredVoters();
        }
        formatter.addRow("TOTAL", String.valueOf(residents), String.valueOf(voters), String.format("%.1f%%", percentage(voters, residents)));
        formatter.print();
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
