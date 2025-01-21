package org.example.be.unifieduser.dto;

import lombok.Getter;

@Getter
public class PolicyUpdateRequestDTO {
    private String userIdentifier;
    private Boolean policyAgreed;

}
