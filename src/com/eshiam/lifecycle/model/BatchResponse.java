package com.eshiam.lifecycle.model;

import java.util.ArrayList;
import java.util.List;

public class BatchResponse {
    private String status;
    private List<BatchResult> results = new ArrayList<>();

    public BatchResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BatchResult> getResults() {
        return results;
    }

    public void setResults(List<BatchResult> results) {
        this.results = results;
    }
}
