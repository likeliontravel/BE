package org.example.be.group.dto;

import lombok.Getter;

@Getter
public class GroupModifyRequestDTO {
    private String groupName;
    private String userIdentifier;
    private String description;
    private String announcement;
}
