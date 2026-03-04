package com.splitwise.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateExpenseRequest {

    private String description;
    private Double amount;
    private List<SplitRequest> splits;
    private Long paidByUserId;
}