package com.splitwise.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ExpenseResponse {

    private Long id;
    private String description;
    private Double amount;
    private Long paidById;
    private String paidByName;
    private boolean settlement;
    private LocalDateTime createdAt;
}