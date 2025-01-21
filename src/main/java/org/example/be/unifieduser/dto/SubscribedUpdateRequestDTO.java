package org.example.be.unifieduser.dto;

import lombok.Getter;

@Getter
public class SubscribedUpdateRequestDTO {
    private String userIdentifier;
    private Boolean subscribed;
}
