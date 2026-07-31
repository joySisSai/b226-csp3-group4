package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.model.User;

import java.util.Scanner;

public class ResidentDashboard {
    private final Scanner scanner;

    public ResidentDashboard(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show(User user) {
        String choice;
        do {
            printHeader(user);
            System.out.println("1. View profile");
            System.out.println("2. Submit service request");
            System.out.println("3. View request status");
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
        System.out.println("=== Resident Dashboard ===");
        System.out.println("Welcome, " + user.getDisplayName() + "!");
    }
}
