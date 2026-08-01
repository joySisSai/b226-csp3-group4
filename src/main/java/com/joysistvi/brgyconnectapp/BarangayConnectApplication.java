package com.joysistvi.brgyconnectapp;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.controller.AuthController;
import com.joysistvi.brgyconnectapp.controller.HouseholdController;
import com.joysistvi.brgyconnectapp.controller.ResidentController;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.repository.HouseholdRepo;
import com.joysistvi.brgyconnectapp.repository.HouseholdRepoImpl;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ResidentRepoImpl;
import com.joysistvi.brgyconnectapp.repository.UserRepo;
import com.joysistvi.brgyconnectapp.repository.UserRepoImpl;
import com.joysistvi.brgyconnectapp.service.AuthService;
import com.joysistvi.brgyconnectapp.service.HouseholdService;
import com.joysistvi.brgyconnectapp.view.*;

import java.util.Optional;
import java.util.Scanner;

public class BarangayConnectApplication {
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new DbConnection();
        UserRepo userRepo = new UserRepoImpl(connectionFactory);
        AuthService authService = new AuthService(userRepo);
        AuthController authController = new AuthController(authService);

        Scanner scanner = new Scanner(System.in);
        LoginView loginView = new LoginView(authController, scanner);
        ResidentManagementView residentManagementView = new ResidentManagementView(
                scanner,
                new ResidentController()
        );
        HouseholdRepo householdRepo = new HouseholdRepoImpl(connectionFactory);
        ResidentRepo residentRepo = new ResidentRepoImpl();
        HouseholdService householdService = new HouseholdService(householdRepo, residentRepo);
        HouseholdManagementView householdManagementView = new HouseholdManagementView(
                scanner,
                new HouseholdController(householdService)
        );
        DashboardRouter dashboardRouter = new DashboardRouter(
                new AdminDashboard(scanner, residentManagementView, householdManagementView),
                new StaffDashboard(scanner, residentManagementView, householdManagementView),
                new ResidentDashboard(scanner)
        );

        while (true) {
            Optional<User> authenticatedUser = loginView.show();
            if (authenticatedUser.isEmpty()) {
                ConsoleUI.resetTerminal();
                System.out.println("Goodbye.");
                break;
            }

            dashboardRouter.showDashboard(authenticatedUser.get());
        }
    }
}
