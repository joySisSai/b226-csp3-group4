package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.controller.ServiceRequestController;
import com.joysistvi.brgyconnectapp.controller.ServiceTypeController;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.model.User;

import java.util.List;
import java.util.Scanner;

// Resident-only view: handles submitting new service requests
public class SubmitServiceRequestView {
    private final Scanner scanner;
    private final ServiceRequestController requestController;
    private final ServiceTypeController serviceTypeController;

    // Initialize required controllers
    public SubmitServiceRequestView(Scanner scanner) {
        this.scanner = scanner;
        this.requestController = new ServiceRequestController();
        this.serviceTypeController = new ServiceTypeController();
    }

    // Display the full request submission flow
    public void show(User loggedInUser) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("Submit New Service Request");

        // Load only active services from database
        List<ServiceType> availableServices = serviceTypeController.getAvailableServices();
        if (availableServices.isEmpty()) {
            ConsoleUI.printError("No services are available at this time.");
            ConsoleUI.printPrompt("\nPress Enter to return...");
            scanner.nextLine();
            return;
        }

        // Show numbered list for easy selection
        ConsoleUI.printSubHeader("Available Services");
        for (int i = 0; i < availableServices.size(); i++) {
            ServiceType service = availableServices.get(i);
            System.out.printf("[%d] %s (%s)%n    Fee: ₱%.2f | Processing: %d day(s)%n%n",
                    (i + 1), service.getServiceName(), service.getServiceCode(),
                    service.getDefaultFee(), service.getExpectedProcessingDays());
        }

        // Validate service selection input
        ServiceType selectedService = null;
        while (selectedService == null) {
            ConsoleUI.printPrompt("Enter the number of the service you need: ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                int index = choice - 1;
                if (index >= 0 && index < availableServices.size()) {
                    selectedService = availableServices.get(index);
                } else {
                    ConsoleUI.printError("Please select a number shown in the list only.");
                }
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Please enter a valid number.");
            }
        }

        // Get additional request details
        ConsoleUI.printPrompt("Purpose or details of your request: ");
        String purpose = scanner.nextLine().trim();
        if (purpose.isBlank()) {
            ConsoleUI.printError("Please provide a purpose for your request.");
            return;
        }

        // Prepare request data
        Integer residentId = loggedInUser.getResidentId();

        if (residentId == null) {
            ConsoleUI.printError("No resident record is linked to this account.");
            return;
        }

        ServiceRequest newRequest = new ServiceRequest();
        newRequest.setResidentId(residentId);
        newRequest.setCreatedByUserId(loggedInUser.getUserId());
        newRequest.setServiceTypeId(selectedService.getServiceTypeId());
        newRequest.setPurpose(purpose);

// The service layer should load the fee from the database.
        String resultMessage = requestController.submitRequest(newRequest);

        if (resultMessage.toLowerCase().contains("success")) {
            ConsoleUI.printSuccess(resultMessage);
        } else {
            ConsoleUI.printError(resultMessage);
        }

        ConsoleUI.printPrompt("\nPress Enter to return to menu...");
        scanner.nextLine();
    }
}