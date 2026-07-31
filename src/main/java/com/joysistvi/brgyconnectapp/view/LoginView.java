package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.AuthController;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.service.LoginResult;

import java.io.Console;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

public class LoginView {
    private final AuthController authController;
    private final Scanner scanner;

    public LoginView(AuthController authController, Scanner scanner) {
        this.authController = authController;
        this.scanner = scanner;
    }

    public Optional<User> show() {
        while (true) {
            System.out.println();
            System.out.println("=== Barangay Connect ===");
            System.out.println("1. Log in");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    Optional<User> authenticatedUser = attemptLogin();
                    if (authenticatedUser.isPresent()) {
                        return authenticatedUser;
                    }
                }
                case "0" -> {
                    return Optional.empty();
                }
                default -> System.out.println("Please enter 1 or 0.");
            }
        }
    }

    private Optional<User> attemptLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        char[] password = readPassword();

        try {
            LoginResult result = authController.login(username, password);
            System.out.println(result.message());
            return Optional.ofNullable(result.user());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private char[] readPassword() {
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword("Password: ");
            return password == null ? new char[0] : password;
        }

        System.out.print("Password: ");
        return scanner.nextLine().toCharArray();
    }
}
