package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ServiceRequestRepo;
import com.joysistvi.brgyconnectapp.repository.ServiceTypeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceRequestServiceTest {
    private ServiceRequestService service;

    @BeforeEach
    public void setUp() {
        ServiceRequestRepo reqRepo = Mockito.mock(ServiceRequestRepo.class);
        ServiceTypeRepo typeRepo = Mockito.mock(ServiceTypeRepo.class);
        ResidentRepo resRepo = Mockito.mock(ResidentRepo.class);
        AuthorizationService authService = Mockito.mock(AuthorizationService.class);
        
        service = new ServiceRequestService(reqRepo, typeRepo, resRepo, authService);
    }

    @Test
    public void testGetAllowedTransitions_FromPending() {
        List<RequestStatus> allowed = service.getAllowedTransitions(RequestStatus.PENDING);
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(RequestStatus.UNDER_REVIEW));
        assertTrue(allowed.contains(RequestStatus.CANCELLED));
    }

    @Test
    public void testGetAllowedTransitions_FromUnderReview() {
        List<RequestStatus> allowed = service.getAllowedTransitions(RequestStatus.UNDER_REVIEW);
        assertEquals(3, allowed.size());
        assertTrue(allowed.contains(RequestStatus.APPROVED));
        assertTrue(allowed.contains(RequestStatus.REJECTED));
        assertTrue(allowed.contains(RequestStatus.CANCELLED));
    }

    @Test
    public void testGetAllowedTransitions_FromApproved() {
        List<RequestStatus> allowed = service.getAllowedTransitions(RequestStatus.APPROVED);
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(RequestStatus.RELEASED));
        assertTrue(allowed.contains(RequestStatus.CANCELLED));
    }

    @Test
    public void testGetAllowedTransitions_FromTerminalStates() {
        assertTrue(service.getAllowedTransitions(RequestStatus.RELEASED).isEmpty());
        assertTrue(service.getAllowedTransitions(RequestStatus.REJECTED).isEmpty());
        assertTrue(service.getAllowedTransitions(RequestStatus.CANCELLED).isEmpty());
    }
}
