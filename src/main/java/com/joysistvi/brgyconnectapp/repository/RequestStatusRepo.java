package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;

public interface RequestStatusRepo {

    ServiceRequest findById(Long requestId);

    boolean updateStatus(Long requestId,
                         RequestStatus status,
                         Integer processedByUserId,
                         String remarks);

    boolean releaseRequest(Long requestId,
                           Integer processedByUserId,
                           String remarks);
}