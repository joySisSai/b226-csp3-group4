package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.AuthController;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.service.LoginResult;

import java.io.Console;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import static com.joysistvi.brgyconnectapp.view.ConsoleUI.printMainBanner;

public class LoginView {
    private final AuthController authController;
    private final RegistrationView registrationView;
    private final Scanner scanner;
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String CYAN = "\u001B[36m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BG_MAROON = "\u001B[48;5;52m";
    public static final String WHITE = "\u001B[97m";

    public LoginView(AuthController authController, RegistrationView registrationView, Scanner scanner) {
        this.authController = authController;
        this.registrationView = registrationView;
        this.scanner = scanner;
    }


    public Optional<User> show() {
        while (true) {
            ConsoleUI.clearScreen();
            ConsoleUI.printMainBanner();
            System.out.println();
            ConsoleUI.printMenuOption("1", "Log in");
            ConsoleUI.printMenuOption("2", "Register Account");
            ConsoleUI.printMenuOption("0", "Exit");
            System.out.println();
            ConsoleUI.printPrompt("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    Optional<User> authenticatedUser = attemptLogin();
                    if (authenticatedUser.isPresent()) {
                        return authenticatedUser;
                    } else {
                        System.out.println();
                        ConsoleUI.printPrompt("Press Enter to try again...");
                        scanner.nextLine();
                    }
                }
                case "2" -> {
                    if (registrationView != null) {
                        registrationView.show();
                    }
                }
                case "0" -> {
                    return Optional.empty();
                }
                default -> {
                    ConsoleUI.printError("Please enter 1, 2, or 0.");
                    System.out.println();
                    ConsoleUI.printPrompt("Press Enter to continue...");
                    scanner.nextLine();
                }
            }
        }
    }

    private Optional<User> attemptLogin() {
        System.out.println();
        ConsoleUI.printPrompt("Username: ");
        String username = scanner.nextLine();
        char[] password = readPassword();

        try {
            LoginResult result = authController.login(username, password);
            if (result.user() != null) {
                ConsoleUI.printSuccess(result.message());
            } else {
                ConsoleUI.printError(result.message());
            }
            return Optional.ofNullable(result.user());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private char[] readPassword() {
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword(ConsoleUI.CYAN + ConsoleUI.BOLD + " » " + ConsoleUI.RESET + "Password: ");
            return password == null ? new char[0] : password;
        }

        ConsoleUI.printPrompt("Password: ");
        return scanner.nextLine().toCharArray();
    }
}