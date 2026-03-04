package com.splitwise.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SplitRequest {
    private Long userId;
    private Double amount;
}