package com.splitwise.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BalanceResponse {

    private Long fromUserId;
    private Long toUserId;
    private Double amount;
}