package com.joysistvi.brgyconnectapp;

import com.joysistvi.brgyconnectapp.config.DatabaseConfig;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.model.Household;
import com.joysistvi.brgyconnectapp.model.HouseholdStatus;
import com.joysistvi.brgyconnectapp.repository.HouseholdRepoImpl;

public class TestHousehold {
    public static void main(String[] args) throws Exception {
        DbConnection conn = new DbConnection(DatabaseConfig.load());
        HouseholdRepoImpl repo = new HouseholdRepoImpl(conn);
        Household h = new Household();
        h.setAddressLine("Test Address");
        h.setPurok("Test Purok");
        h.setHouseholdStatus(HouseholdStatus.ACTIVE);
        repo.save(h);
        System.out.println("Saved household: " + h.getHouseholdCode());
    }
}
