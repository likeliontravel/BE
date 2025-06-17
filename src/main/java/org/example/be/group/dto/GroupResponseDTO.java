package org.example.be.group.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupResponseDTO {
    private Long id;
    private String groupName;
    private String description;
    private String createdBy;   // 그룹 창설자의 userIdentifier
}
