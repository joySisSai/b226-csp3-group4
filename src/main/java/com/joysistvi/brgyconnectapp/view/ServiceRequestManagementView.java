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

    public void searchRequests(User user) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Search Service Requests");
        ConsoleUI.printInfo("Leave the search blank to show the 100 most recent requests.");
        ConsoleUI.printPrompt("Request number, resident code, or resident name: ");
        String keyword = scanner.nextLine().trim();
        viewPaginatedRequests(keyword, user);
    }

    public void createRequest(User user) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Create Service Request");

        List<ServiceType> serviceTypes = requestController.getActiveServiceTypes(userId(user));
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
        ServiceRequest request = requestController.getById(requestId, userId(user));
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

    public void viewRequestHistory(User user) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Request Status History");
        long requestId = promptPositiveLong("Request ID: ");
        ServiceRequest request = requestController.getById(requestId, userId(user));
        if (request == null) {
            ConsoleUI.printError("Service request not found.");
            pause();
            return;
        }

        printRequestDetails(request);
        System.out.println();
        printHistory(requestController.getHistory(requestId, userId(user)));
        pause();
    }

    private int userId(User user) {
        return user == null || user.getUserId() == null ? 0 : user.getUserId();
    }

    private void viewPaginatedRequests(String keyword, User user) {
        int page = 1;
        int pageSize = 10;

        while (true) {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("Service Requests - Page " + page);
            List<ServiceRequest> requests = requestController.search(keyword, (page - 1) * pageSize, pageSize, userId(user));

            if (requests.isEmpty() && page == 1) {
                ConsoleUI.printInfo("No service requests found.");
                pause();
                break;
            }

            if (!requests.isEmpty()) {
                TableFormatter formatter = new TableFormatter("ID", "Request Number", "Resident", "Service", "Status", "Date");
                for (ServiceRequest request : requests) {
                    formatter.addRow(
                            String.valueOf(request.getRequestId()),
                            request.getRequestNumber(),
                            String.valueOf(request.getResidentId()),
                            String.valueOf(request.getServiceTypeId()),
                            String.valueOf(request.getStatus()),
                            String.valueOf(request.getRequestDate()));
                }
                formatter.print();
            }

            System.out.println();
            ConsoleUI.printInfo("N - Next Page | P - Previous Page | Q - Quit to Menu");
            ConsoleUI.printPrompt("Choose an option: ");
            String opt = scanner.nextLine().trim().toUpperCase();

            if (opt.equals("N")) {
                if (requests.size() == pageSize) {
                    page++;
                } else {
                    ConsoleUI.printInfo("You are already on the last page.");
                    pause();
                }
            } else if (opt.equals("P")) {
                if (page > 1) {
                    page--;
                } else {
                    ConsoleUI.printInfo("You are already on the first page.");
                    pause();
                }
            } else if (opt.equals("Q") || opt.equals("0")) {
                break;
            } else {
                ConsoleUI.printError("Invalid option.");
                pause();
            }
        }
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

        TableFormatter formatter = new TableFormatter("Changed At", "Old Status", "New Status", "User", "Remarks");
        for (RequestStatusHistory entry : history) {
            formatter.addRow(
                    String.valueOf(entry.getChangedAt()),
                    entry.getOldStatus() == null ? "-" : String.valueOf(entry.getOldStatus()),
                    String.valueOf(entry.getNewStatus()),
                    String.valueOf(entry.getChangedByUserId()),
                    abbreviate(valueOrDash(entry.getRemarks()), 30));
        }
        formatter.print();
    }

    private void printServiceTypes(List<ServiceType> serviceTypes) {
        ConsoleUI.printSubHeader("Available Services");
        TableFormatter formatter = new TableFormatter("ID", "Code", "Service", "Fee", "Days");
        for (ServiceType serviceType : serviceTypes) {
            formatter.addRow(
                    String.valueOf(serviceType.getServiceTypeId()),
                    serviceType.getServiceCode(),
                    abbreviate(serviceType.getServiceName(), 30),
                    "PHP " + serviceType.getDefaultFee(),
                    String.valueOf(serviceType.getExpectedProcessingDays()));
        }
        formatter.print();
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
