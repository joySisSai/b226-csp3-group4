package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;

import java.util.List;

public interface RequestStatusHistoryRepo {

    boolean save(RequestStatusHistory history);

    List<RequestStatusHistory> findByRequestId(Long requestId);
}