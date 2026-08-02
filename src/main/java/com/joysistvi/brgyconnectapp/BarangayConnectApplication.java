package com.joysistvi.brgyconnectapp;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.config.DatabaseConfig;
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
import com.joysistvi.brgyconnectapp.service.AuthorizationService;
import com.joysistvi.brgyconnectapp.service.HouseholdService;
import com.joysistvi.brgyconnectapp.service.ReportService;
import com.joysistvi.brgyconnectapp.service.ResidentService;
import com.joysistvi.brgyconnectapp.service.ServiceRequestService;
import com.joysistvi.brgyconnectapp.service.ServiceTypeAdminService;
import com.joysistvi.brgyconnectapp.service.UserAdminService;
import com.joysistvi.brgyconnectapp.view.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class BarangayConnectApplication {
    private static final Set<String> REQUIRED_TABLES = Set.of(
            "users",
            "residents",
            "households",
            "service_types",
            "service_requests",
            "request_status_history",
            "activity_logs"
    );

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = initializeDatabase();
        if (connectionFactory == null) {
            return;
        }
        UserRepo userRepo = new UserRepoImpl(connectionFactory);
        AuthorizationService authorizationService = new AuthorizationService(userRepo);
        ActivityLogRepo activityLogRepo = new ActivityLogRepoImpl(connectionFactory);
        ActivityLogService activityLogService = new ActivityLogService(
                activityLogRepo, authorizationService);
        AuthService authService = new AuthService(userRepo);
        AuthController authController = new AuthController(authService);

        Scanner scanner = new Scanner(System.in);
        LoginView loginView = new LoginView(authController, scanner);
        ResidentRepo residentRepo = new ResidentRepoImpl(connectionFactory);
        ResidentController residentController = new ResidentController(
                new ResidentService(residentRepo, authorizationService), activityLogService);
        HouseholdRepo householdRepo = new HouseholdRepoImpl(connectionFactory);
        HouseholdService householdService = new HouseholdService(
                householdRepo, residentRepo, authorizationService);
        HouseholdController householdController = new HouseholdController(householdService, activityLogService);
        HouseholdManagementView householdManagementView = new HouseholdManagementView(
                scanner,
                householdController
        );

        ResidentManagementView residentManagementView = new ResidentManagementView(
                scanner, residentController, householdController);
        ServiceRequestRepo serviceRequestRepo = new ServiceRequestRepoImpl(connectionFactory);
        ServiceTypeRepo serviceTypeRepo = new ServiceTypeRepoImpl(connectionFactory);
        ServiceRequestService serviceRequestService = new ServiceRequestService(
                serviceRequestRepo,
                serviceTypeRepo,
                residentRepo,
                authorizationService
        );
        ServiceRequestController serviceRequestController = new ServiceRequestController(serviceRequestService, activityLogService);
        ServiceRequestManagementView serviceRequestManagementView = new ServiceRequestManagementView(
                scanner,
                serviceRequestController
        );
        ReportRepo reportRepo = new ReportRepoImpl(connectionFactory);
        ReportView reportView = new ReportView(
                scanner,
                new ReportController(new ReportService(reportRepo, authorizationService))
        );
        ServiceTypeManagementView serviceTypeManagementView = new ServiceTypeManagementView(
                scanner,
                new ServiceTypeAdminController(
                        new ServiceTypeAdminService(serviceTypeRepo, authorizationService),
                        activityLogService)
        );
        UserManagementView userManagementView = new UserManagementView(
                scanner,
                new UserAdminController(
                        new UserAdminService(userRepo, residentRepo, authorizationService),
                        activityLogService)
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
                new ResidentDashboard(scanner, residentController, serviceRequestController, householdController)
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

    private static ConnectionFactory initializeDatabase() {
        try {
            DbConnection connectionFactory = new DbConnection(DatabaseConfig.load());
            try (Connection connection = connectionFactory.openConnection()) {
                if (!connection.isValid(3)) {
                    throw new SQLException("Database connection validation failed");
                }
                validateSchema(connection);
            }
            return connectionFactory;
        } catch (IllegalStateException exception) {
            System.err.println("Application startup failed: " + exception.getMessage());
            System.err.println("Configure the project .env file or set DB_URL, " +
                    "DB_USERNAME, and optionally DB_PASSWORD.");
            return null;
        } catch (SQLException exception) {
            System.err.println("Application startup failed: unable to connect to the database.");
            System.err.println("Verify the database server, schema, and configured credentials.");
            return null;
        }
    }

    private static void validateSchema(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        Set<String> availableTables = new HashSet<>();
        try (ResultSet tables = metadata.getTables(
                connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                availableTables.add(tables.getString("TABLE_NAME").toLowerCase(Locale.ROOT));
            }
        }

        if (!availableTables.containsAll(REQUIRED_TABLES)) {
            Set<String> missingTables = new HashSet<>(REQUIRED_TABLES);
            missingTables.removeAll(availableTables);
            throw new SQLException("Missing required database tables: " + missingTables);
        }
    }
}
