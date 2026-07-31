package com.joysistvi.brgyconnectapp.view;

import com.joysistvi.brgyconnectapp.model.User;

public class DashboardRouter {
    private final AdminDashboard adminDashboard;
    private final StaffDashboard staffDashboard;
    private final ResidentDashboard residentDashboard;

    public DashboardRouter(AdminDashboard adminDashboard,
                           StaffDashboard staffDashboard,
                           ResidentDashboard residentDashboard) {
        this.adminDashboard = adminDashboard;
        this.staffDashboard = staffDashboard;
        this.residentDashboard = residentDashboard;
    }

    public void showDashboard(User user) {
        switch (user.getRole()) {
            case ADMIN -> adminDashboard.show(user);
            case STAFF -> staffDashboard.show(user);
            case RESIDENT -> residentDashboard.show(user);
        }
    }
}
