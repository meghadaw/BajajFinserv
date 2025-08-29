package com.bajajfinserv.qualifier.dto;

public class SolutionRequest {
    private String finalQuery;

    public SolutionRequest() {}

    public SolutionRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    // Getters and setters
    public String getFinalQuery() {
        return finalQuery;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }
}
