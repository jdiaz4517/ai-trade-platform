package com.example.aitrade.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {
    
    private String customerId;
    private String serviceType;
    private String location;
    private String description;
    private String budgetRange;
    private String urgency;
    private LocalDateTime createdAt;
    private String status;
}