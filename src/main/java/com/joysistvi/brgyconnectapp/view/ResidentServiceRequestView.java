package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ServiceRequestController;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.service.DataAccessException;

import java.util.List;
import java.util.Scanner;

public class ResidentServiceRequestView {
    private final Scanner scanner;
    private final ServiceRequestController requestController;

    public ResidentServiceRequestView(Scanner scanner, ServiceRequestController requestController) {
        this.scanner = scanner;
        this.requestController = requestController;
    }

    public void showSubmit(User user) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Submit Service Request");

        int residentId = user.getResidentId();
        int actingUserId = user.getUserId();

        List<ServiceType> activeTypes = requestController.getActiveServiceTypesForResident(residentId, actingUserId);
        if (activeTypes.isEmpty()) {
            ConsoleUI.printError("No service types are currently available.");
            return;
        }

        System.out.println("Available Service Types:");
        for (ServiceType type : activeTypes) {
            System.out.printf("[%d] %s (Fee: %.2f)\n",
                    type.getServiceTypeId(), type.getServiceName(), type.getDefaultFee());
        }

        ConsoleUI.printPrompt("\nEnter Service Type ID to request (or 0 to cancel): ");
        int typeId;
        try {
            typeId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid input");
            return;
        }

        if (typeId <= 0) {
            ConsoleUI.printInfo("Submission cancelled");
            return;
        }

        boolean valid = activeTypes.stream().anyMatch(t -> t.getServiceTypeId() == typeId);
        if (!valid) {
            ConsoleUI.printError("Invalid service type ID");
            return;
        }

        ConsoleUI.printPrompt("Enter Purpose (max 500 chars): ");
        String purpose = scanner.nextLine().trim();

        ServiceRequest request = new ServiceRequest();
        request.setServiceTypeId(typeId);
        request.setPurpose(purpose);

        String result = requestController.createOwnRequest(request, residentId, actingUserId);
        if (result.endsWith("submitted successfully")) {
            ConsoleUI.printSuccess(result);
        } else {
            ConsoleUI.printError(result);
        }
    }

    public void showCheckStatus(User user) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("My Service Requests");

        int residentId = user.getResidentId();
        int actingUserId = user.getUserId();

        int page = 1;
        int pageSize = 10;
        
        while (true) {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("My Service Requests - Page " + page);

            List<ServiceRequest> requests = requestController.getOwnRequests(residentId, (page - 1) * pageSize, pageSize, actingUserId);
            
            if (requests.isEmpty() && page == 1) {
                ConsoleUI.printInfo("You have no service requests.");
                pause();
                return;
            }

            if (!requests.isEmpty()) {
                TableFormatter formatter = new TableFormatter("ID", "Request Number", "Date", "Status");
                for (ServiceRequest req : requests) {
                    formatter.addRow(
                            String.valueOf(req.getRequestId()),
                            req.getRequestNumber(),
                            String.valueOf(req.getRequestDate()),
                            String.valueOf(req.getStatus()));
                }
                formatter.print();
            }

            System.out.println();
            ConsoleUI.printInfo("N - Next Page | P - Previous Page | Q - Go Back | Or enter Request ID to view details");
            ConsoleUI.printPrompt("Choose an option: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("N")) {
                if (requests.size() == pageSize) {
                    page++;
                } else {
                    ConsoleUI.printInfo("You are already on the last page.");
                    pause();
                }
                continue;
            } else if (input.equalsIgnoreCase("P")) {
                if (page > 1) {
                    page--;
                } else {
                    ConsoleUI.printInfo("You are already on the first page.");
                    pause();
                }
                continue;
            } else if (input.equalsIgnoreCase("Q") || input.equals("0")) {
                break;
            }

            long requestId;
            try {
                requestId = Long.parseLong(input);
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid input");
                pause();
                continue;
            }

            if (requestId <= 0) {
                continue;
            }

            ServiceRequest request = requestController.getOwnRequestById(requestId, residentId, actingUserId);
            if (request == null) {
                ConsoleUI.printError("Request not found");
                pause();
                continue;
            }

        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Request Details");
        System.out.printf("Request Number : %s\n", request.getRequestNumber());
        System.out.printf("Status         : %s\n", request.getStatus());
        System.out.printf("Purpose        : %s\n", request.getPurpose());
        System.out.printf("Date           : %s\n", request.getRequestDate());
        System.out.printf("Remarks        : %s\n", request.getRemarks() == null ? "None" : request.getRemarks());

        List<RequestStatusHistory> history = requestController.getOwnStatusHistory(requestId, residentId, actingUserId);
        System.out.println("\nStatus History:");
        if (history.isEmpty()) {
            System.out.println("No history available.");
        } else {
            TableFormatter historyFormatter = new TableFormatter("Date", "Old Status", "New Status", "Remarks");
            for (RequestStatusHistory h : history) {
                historyFormatter.addRow(
                        String.valueOf(h.getChangedAt()),
                        h.getOldStatus() == null ? "NEW" : String.valueOf(h.getOldStatus()),
                        String.valueOf(h.getNewStatus()),
                        h.getRemarks() == null ? "" : h.getRemarks());
            }
            historyFormatter.print();
        }
        
            if (request.getStatus() == com.joysistvi.brgyconnectapp.model.RequestStatus.PENDING) {
                System.out.println();
                ConsoleUI.printPrompt("Would you like to cancel this pending request? (Y/N): ");
                String confirm = scanner.nextLine().trim();
                if (confirm.equalsIgnoreCase("Y") || confirm.equalsIgnoreCase("YES")) {
                    String result = requestController.cancelOwnRequest(requestId, residentId, actingUserId);
                    if (result.endsWith("successfully")) {
                        ConsoleUI.printSuccess(result);
                    } else {
                        ConsoleUI.printError(result);
                    }
                }
            }
            pause();
        }
    }

    private void pause() {
        System.out.println();
        ConsoleUI.printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
