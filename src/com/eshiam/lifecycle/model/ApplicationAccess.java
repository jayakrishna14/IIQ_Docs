package com.eshiam.lifecycle.model;

public class ApplicationAccess {
    private String name;
    private String operation;
    private Access access;

    public ApplicationAccess() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
}
