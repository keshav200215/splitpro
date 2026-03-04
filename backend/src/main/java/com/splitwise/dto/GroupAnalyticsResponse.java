package com.splitwise.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupAnalyticsResponse {

    private double totalGroupExpense;
    private double yourExpense;
    private String highestSpender;
    private double highestExpenseAmount;
}