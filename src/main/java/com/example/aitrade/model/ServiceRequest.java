package com.example.aitrade.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonClassDescription;

@JsonClassDescription("Customer service request information extracted from natural language")
public record ServiceRequest(
    @JsonPropertyDescription("Type of service needed: PLUMBING, ELECTRICAL, PAINTING, CARPENTRY, GARDENING, or OTHER")
    ServiceType serviceType,
    
    @JsonPropertyDescription("How urgent is this request: HIGH, MEDIUM, LOW, or UNKNOWN")
    Urgency urgency,
    
    @JsonPropertyDescription("Customer location (city, postcode, or address)")
    String location,
    
    @JsonPropertyDescription("Budget range if mentioned (e.g., '£200-300', 'under £500')")
    String budgetRange,
    
    @JsonPropertyDescription("Whether this is an emergency situation")
    Boolean isEmergency,
    
    @JsonPropertyDescription("Specific details about the job or requirements")
    String jobDescription
) {
    
    public enum ServiceType {
        PLUMBING, ELECTRICAL, PAINTING, CARPENTRY, GARDENING, OTHER
    }
    
    public enum Urgency {
        HIGH, MEDIUM, LOW, UNKNOWN
    }
}