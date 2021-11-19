package com.example.myvax.Classes;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

public class Vaccination implements Serializable {

    private String vaccinationID;
    private String appointmentDate;
    private String remarks;
    private String status;
    private String username;
    private String batchNo;
    //included vaccineName and centreName so that it can be displayed in My Appointment
    private String vaccineName;
    private String centreName;


    public String getVaccinationID() {
        return vaccinationID;
    }

    public void setVaccinationID(String vaccinationID) {
        this.vaccinationID = vaccinationID;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getCentreName() {
        return centreName;
    }

    public void setCentreName(String centreName) {
        this.centreName = centreName;
    }

    @Override
    public String toString() {
        return "Vaccination{" +
                "vaccinationID='" + vaccinationID + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
