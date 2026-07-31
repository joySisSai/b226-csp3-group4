package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.model.User;

import java.util.Scanner;

public class StaffDashboard {
    private final Scanner scanner;

    public StaffDashboard(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show(User user) {
        String choice;
        do {
            printHeader(user);
            System.out.println("1. Manage resident records");
            System.out.println("2. Manage household records");
            System.out.println("3. Search records");
            System.out.println("4. Create service request");
            System.out.println("5. Update request status");
            System.out.println("6. View request history");
            System.out.println("7. Generate reports");
            System.out.println("0. Log out");
            System.out.print("Choose an option: ");
            choice = scanner.nextLine().trim();

            if (!choice.equals("0")) {
                System.out.println("This feature is not implemented yet.");
            }
        } while (!choice.equals("0"));

        System.out.println("Logged out successfully.");
    }

    private void printHeader(User user) {
        System.out.println();
        System.out.println("=== Staff Dashboard ===");
        System.out.println("Welcome, " + user.getDisplayName() + "!");
    }
}
