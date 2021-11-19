package com.example.myvax.Classes;

import java.io.Serializable;
import java.util.List;

public class HealthcareCentre implements Serializable {

    private String centreName;
    private String centreAddress;
    private List<String> vaccineID;

    public String getCentreName() {
        return centreName;
    }

    public void setCentreName(String centreName) {
        this.centreName = centreName;
    }

    public String getCentreAddress() {
        return centreAddress;
    }

    public void setCentreAddress(String centreAddress) {
        this.centreAddress = centreAddress;
    }

    public List<String> getVaccineID() {
        return vaccineID;
    }

    public void setVaccineID(List<String> vaccineID) {
        this.vaccineID = vaccineID;
    }

    @Override
    public String toString() {
        return centreName + "\n" + centreAddress;
    }
}
