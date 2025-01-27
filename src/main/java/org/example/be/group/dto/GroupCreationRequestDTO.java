package org.example.be.group.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupCreationRequestDTO {

    private String groupName;
    private String description;
    private String createdBy;   // userIdentifier
}
