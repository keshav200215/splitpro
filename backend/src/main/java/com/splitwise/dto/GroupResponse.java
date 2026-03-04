package com.splitwise.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupResponse {

    private Long id;
    private String name;
    private Long createdById;
    private String createdByName;
}