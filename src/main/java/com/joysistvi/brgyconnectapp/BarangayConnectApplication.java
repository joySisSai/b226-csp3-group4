package com.joysistvi.brgyconnectapp;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.controller.AuthController;
import com.joysistvi.brgyconnectapp.controller.ActivityLogController;
import com.joysistvi.brgyconnectapp.controller.HouseholdController;
import com.joysistvi.brgyconnectapp.controller.ResidentController;
import com.joysistvi.brgyconnectapp.controller.ReportController;
import com.joysistvi.brgyconnectapp.controller.ServiceRequestController;
import com.joysistvi.brgyconnectapp.controller.ServiceTypeAdminController;
import com.joysistvi.brgyconnectapp.controller.UserAdminController;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.repository.HouseholdRepo;
import com.joysistvi.brgyconnectapp.repository.HouseholdRepoImpl;
import com.joysistvi.brgyconnectapp.repository.ActivityLogRepo;
import com.joysistvi.brgyconnectapp.repository.ActivityLogRepoImpl;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ResidentRepoImpl;
import com.joysistvi.brgyconnectapp.repository.ReportRepo;
import com.joysistvi.brgyconnectapp.repository.ReportRepoImpl;
import com.joysistvi.brgyconnectapp.repository.ServiceRequestRepo;
import com.joysistvi.brgyconnectapp.repository.ServiceRequestRepoImpl;
import com.joysistvi.brgyconnectapp.repository.ServiceTypeRepo;
import com.joysistvi.brgyconnectapp.repository.ServiceTypeRepoImpl;
import com.joysistvi.brgyconnectapp.repository.UserRepo;
import com.joysistvi.brgyconnectapp.repository.UserRepoImpl;
import com.joysistvi.brgyconnectapp.service.AuthService;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;
import com.joysistvi.brgyconnectapp.service.HouseholdService;
import com.joysistvi.brgyconnectapp.service.ReportService;
import com.joysistvi.brgyconnectapp.service.ResidentService;
import com.joysistvi.brgyconnectapp.service.ServiceRequestService;
import com.joysistvi.brgyconnectapp.service.ServiceTypeAdminService;
import com.joysistvi.brgyconnectapp.service.UserAdminService;
import com.joysistvi.brgyconnectapp.view.*;

import java.util.Optional;
import java.util.Scanner;

public class BarangayConnectApplication {
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new DbConnection();
        UserRepo userRepo = new UserRepoImpl(connectionFactory);
        ActivityLogRepo activityLogRepo = new ActivityLogRepoImpl(connectionFactory);
        ActivityLogService activityLogService = new ActivityLogService(activityLogRepo);
        AuthService authService = new AuthService(userRepo);
        AuthController authController = new AuthController(authService);

        Scanner scanner = new Scanner(System.in);
        LoginView loginView = new LoginView(authController, scanner);
        ResidentRepo residentRepo = new ResidentRepoImpl();
        ResidentManagementView residentManagementView = new ResidentManagementView(
                scanner,
                new ResidentController(new ResidentService(residentRepo), activityLogService)
        );
        HouseholdRepo householdRepo = new HouseholdRepoImpl(connectionFactory);
        HouseholdService householdService = new HouseholdService(householdRepo, residentRepo);
        HouseholdManagementView householdManagementView = new HouseholdManagementView(
                scanner,
                new HouseholdController(householdService, activityLogService)
        );
        ServiceRequestRepo serviceRequestRepo = new ServiceRequestRepoImpl(connectionFactory);
        ServiceTypeRepo serviceTypeRepo = new ServiceTypeRepoImpl(connectionFactory);
        ServiceRequestService serviceRequestService = new ServiceRequestService(
                serviceRequestRepo,
                serviceTypeRepo,
                residentRepo
        );
        ServiceRequestManagementView serviceRequestManagementView = new ServiceRequestManagementView(
                scanner,
                new ServiceRequestController(serviceRequestService, activityLogService)
        );
        ReportRepo reportRepo = new ReportRepoImpl(connectionFactory);
        ReportView reportView = new ReportView(
                scanner,
                new ReportController(new ReportService(reportRepo))
        );
        ServiceTypeManagementView serviceTypeManagementView = new ServiceTypeManagementView(
                scanner,
                new ServiceTypeAdminController(
                        new ServiceTypeAdminService(serviceTypeRepo), activityLogService)
        );
        UserManagementView userManagementView = new UserManagementView(
                scanner,
                new UserAdminController(
                        new UserAdminService(userRepo, residentRepo), activityLogService)
        );
        ActivityLogView activityLogView = new ActivityLogView(
                scanner,
                new ActivityLogController(activityLogService)
        );
        DashboardRouter dashboardRouter = new DashboardRouter(
                new AdminDashboard(
                        scanner,
                        residentManagementView,
                        householdManagementView,
                        serviceRequestManagementView,
                        reportView,
                        serviceTypeManagementView,
                        userManagementView,
                        activityLogView
                ),
                new StaffDashboard(
                        scanner,
                        residentManagementView,
                        householdManagementView,
                        serviceRequestManagementView,
                        reportView
                ),
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
