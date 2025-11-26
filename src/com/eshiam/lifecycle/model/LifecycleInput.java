package com.eshiam.lifecycle.model;

import java.util.ArrayList;
import java.util.List;

public class LifecycleInput {

    private String eventType;
    private String identityName;
    private String firstName;
    private String lastName;
    private String department;
    private String email;
    private List<ApplicationAccess> applications = new ArrayList<>();

    public LifecycleInput() {
        // MUST EXIST
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getIdentityName() {
        return identityName;
    }
    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public List<ApplicationAccess> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationAccess> applications) {
        this.applications = applications;
    }
}
