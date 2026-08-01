package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.ActivityLog;
import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepo {
    boolean save(ActivityLog log);
    List<ActivityLog> getAll();
    List<ActivityLog> getByUser(Integer userId);
    List<ActivityLog> getByDateRange(LocalDateTime start, LocalDateTime end);
}