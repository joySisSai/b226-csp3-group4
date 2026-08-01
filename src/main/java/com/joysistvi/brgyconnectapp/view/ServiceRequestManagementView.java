package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ServiceRequestController;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.model.User;

import java.util.List;
import java.util.Scanner;

public class ServiceRequestManagementView {
    private final Scanner scanner;
    private final ServiceRequestController requestController;

    public ServiceRequestManagementView(Scanner scanner, ServiceRequestController requestController) {
        this.scanner = scanner;
        this.requestController = requestController;
    }

    public void searchRequests() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Search Service Requests");
        ConsoleUI.printInfo("Leave the search blank to show the 100 most recent requests.");
        ConsoleUI.printPrompt("Request number, resident code, or resident name: ");
        String keyword = scanner.nextLine().trim();
        printRequests(requestController.search(keyword));
        pause();
    }

    public void createRequest(User user) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Create Service Request");

        List<ServiceType> serviceTypes = requestController.getActiveServiceTypes();
        if (serviceTypes.isEmpty()) {
            ConsoleUI.printError("No active service types are available.");
            pause();
            return;
        }

        printServiceTypes(serviceTypes);
        int residentId = promptPositiveInteger("Resident ID: ");
        int serviceTypeId = promptPositiveInteger("Service type ID: ");
        ServiceType selectedType = serviceTypes.stream()
                .filter(serviceType -> serviceType.getServiceTypeId() == serviceTypeId)
                .findFirst()
                .orElse(null);
        if (selectedType == null) {
            ConsoleUI.printError("Choose an active service type from the list.");
            pause();
            return;
        }

        String purpose = promptRequired("Purpose: ", 500);
        String remarks = promptOptional("Initial remarks (optional): ", 1000);
        System.out.println();
        ConsoleUI.printInfo("Service: " + selectedType.getServiceName());
        ConsoleUI.printInfo("Fee: PHP " + selectedType.getDefaultFee());
        ConsoleUI.printInfo("Expected processing: " + selectedType.getExpectedProcessingDays() + " day(s)");
        if (!promptYesNo("Create this service request? (Y/N): ")) {
            ConsoleUI.printInfo("Request creation cancelled.");
            pause();
            return;
        }

        ServiceRequest request = new ServiceRequest();
        request.setResidentId(residentId);
        request.setServiceTypeId(serviceTypeId);
        request.setPurpose(purpose);
        request.setRemarks(remarks);
        request.setCreatedByUserId(user.getUserId());
        printOperationResult(requestController.create(request));
        pause();
    }

    public void updateRequestStatus(User user) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Update Request Status");
        long requestId = promptPositiveLong("Request ID: ");
        ServiceRequest request = requestController.getById(requestId);
        if (request == null) {
            ConsoleUI.printError("Service request not found.");
            pause();
            return;
        }

        printRequestDetails(request);
        List<RequestStatus> transitions = requestController.getAllowedTransitions(request.getStatus());
        if (transitions.isEmpty()) {
            ConsoleUI.printInfo("This request is in a final status and cannot be changed.");
            pause();
            return;
        }

        System.out.println();
        ConsoleUI.printSubHeader("Allowed Status Changes");
        for (int index = 0; index < transitions.size(); index++) {
            ConsoleUI.printMenuOption(String.valueOf(index + 1), formatEnum(transitions.get(index)));
        }
        int selected = promptNumberInRange("Select new status: ", 1, transitions.size());
        RequestStatus newStatus = transitions.get(selected - 1);
        String remarks = promptOptional(
                newStatus == RequestStatus.REJECTED || newStatus == RequestStatus.CANCELLED
                        ? "Remarks (required): "
                        : "Remarks (optional): ",
                1000
        );
        if ((newStatus == RequestStatus.REJECTED || newStatus == RequestStatus.CANCELLED) &&
                remarks == null) {
            ConsoleUI.printError("Remarks are required for this status.");
            pause();
            return;
        }

        if (!promptYesNo("Change status to " + formatEnum(newStatus) + "? (Y/N): ")) {
            ConsoleUI.printInfo("Status update cancelled.");
            pause();
            return;
        }

        int changedByUserId = user.getUserId() == null ? 0 : user.getUserId();
        printOperationResult(requestController.updateStatus(
                requestId,
                newStatus,
                remarks,
                changedByUserId
        ));
        pause();
    }

    public void viewRequestHistory() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Request Status History");
        long requestId = promptPositiveLong("Request ID: ");
        ServiceRequest request = requestController.getById(requestId);
        if (request == null) {
            ConsoleUI.printError("Service request not found.");
            pause();
            return;
        }

        printRequestDetails(request);
        System.out.println();
        printHistory(requestController.getHistory(requestId));
        pause();
    }

    private void printRequests(List<ServiceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            ConsoleUI.printInfo("No service requests found.");
            return;
        }

        System.out.printf("%-6s %-23s %-10s %-10s %-15s %-12s%n",
                "ID", "Request Number", "Resident", "Service", "Status", "Date");
        System.out.println("-".repeat(84));
        for (ServiceRequest request : requests) {
            System.out.printf("%-6s %-23s %-10s %-10s %-15s %-12s%n",
                    request.getRequestId(),
                    request.getRequestNumber(),
                    request.getResidentId(),
                    request.getServiceTypeId(),
                    request.getStatus(),
                    request.getRequestDate());
        }
        System.out.println();
        ConsoleUI.printInfo(requests.size() + " request(s) found.");
    }

    private void printRequestDetails(ServiceRequest request) {
        System.out.printf("Request ID       : %s%n", request.getRequestId());
        System.out.printf("Request Number   : %s%n", request.getRequestNumber());
        System.out.printf("Resident ID      : %s%n", request.getResidentId());
        System.out.printf("Service Type ID  : %s%n", request.getServiceTypeId());
        System.out.printf("Purpose          : %s%n", request.getPurpose());
        System.out.printf("Request Date     : %s%n", request.getRequestDate());
        System.out.printf("Fee              : PHP %s%n", request.getServiceFeeSnapshot());
        System.out.printf("Status           : %s%n", formatEnum(request.getStatus()));
        System.out.printf("Remarks          : %s%n", valueOrDash(request.getRemarks()));
        System.out.printf("Created By       : User %s%n", request.getCreatedByUserId());
        System.out.printf("Processed By     : %s%n", valueOrDash(request.getProcessedByUserId()));
        System.out.printf("Processed At     : %s%n", valueOrDash(request.getProcessedAt()));
        System.out.printf("Released At      : %s%n", valueOrDash(request.getReleasedAt()));
    }

    private void printHistory(List<RequestStatusHistory> history) {
        ConsoleUI.printSubHeader("Status Changes");
        if (history == null || history.isEmpty()) {
            ConsoleUI.printInfo("No status history was found.");
            return;
        }

        System.out.printf("%-20s %-15s %-15s %-10s %-30s%n",
                "Changed At", "Old Status", "New Status", "User", "Remarks");
        System.out.println("-".repeat(96));
        for (RequestStatusHistory entry : history) {
            System.out.printf("%-20s %-15s %-15s %-10s %-30s%n",
                    entry.getChangedAt(),
                    entry.getOldStatus() == null ? "-" : entry.getOldStatus(),
                    entry.getNewStatus(),
                    entry.getChangedByUserId(),
                    abbreviate(valueOrDash(entry.getRemarks()), 30));
        }
    }

    private void printServiceTypes(List<ServiceType> serviceTypes) {
        ConsoleUI.printSubHeader("Available Services");
        System.out.printf("%-6s %-14s %-30s %-12s %-8s%n",
                "ID", "Code", "Service", "Fee", "Days");
        System.out.println("-".repeat(74));
        for (ServiceType serviceType : serviceTypes) {
            System.out.printf("%-6s %-14s %-30s PHP %-8s %-8s%n",
                    serviceType.getServiceTypeId(),
                    serviceType.getServiceCode(),
                    abbreviate(serviceType.getServiceName(), 30),
                    serviceType.getDefaultFee(),
                    serviceType.getExpectedProcessingDays());
        }
        System.out.println();
    }

    private String promptRequired(String label, int maximumLength) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String value = scanner.nextLine().trim();
            if (value.isBlank()) {
                ConsoleUI.printError("This field is required.");
            } else if (value.length() > maximumLength) {
                ConsoleUI.printError("Maximum length is " + maximumLength + " characters.");
            } else {
                return value;
            }
        }
    }

    private String promptOptional(String label, int maximumLength) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return null;
            }
            if (value.length() <= maximumLength) {
                return value;
            }
            ConsoleUI.printError("Maximum length is " + maximumLength + " characters.");
        }
    }

    private int promptPositiveInteger(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number.");
        }
    }

    private long promptPositiveLong(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            try {
                long value = Long.parseLong(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Enter a positive whole number.");
        }
    }

    private int promptNumberInRange(String label, int minimum, int maximum) {
        while (true) {
            ConsoleUI.printPrompt(label);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= minimum && value <= maximum) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUI.printError("Choose a number from " + minimum + " to " + maximum + ".");
        }
    }

    private boolean promptYesNo(String label) {
        while (true) {
            ConsoleUI.printPrompt(label);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("Y") || input.equalsIgnoreCase("YES")) {
                return true;
            }
            if (input.equalsIgnoreCase("N") || input.equalsIgnoreCase("NO")) {
                return false;
            }
            ConsoleUI.printError("Enter Y for yes or N for no.");
        }
    }

    private void printOperationResult(String result) {
        if (result != null && result.toLowerCase().contains("successfully")) {
            ConsoleUI.printSuccess(result);
        } else {
            ConsoleUI.printError(result == null ? "The operation could not be completed." : result);
        }
    }

    private String formatEnum(Enum<?> value) {
        if (value == null) {
            return "-";
        }
        String name = value.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String valueOrDash(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
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
