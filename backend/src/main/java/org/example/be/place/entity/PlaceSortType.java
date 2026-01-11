package org.example.be.place.entity;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum PlaceSortType {

    TITLE_ASC("title", Sort.Direction.ASC), // 이름 오름차순 <- 지금은 이게 사용될텐데 혹시 몰라서 내림차순도 만들어둠
    TITLE_DESC("title", Sort.Direction.DESC);   // 이름 내림차순

    private final String sortProperty;
    private final Sort.Direction sortDirection;

    PlaceSortType(String sortProperty, Sort.Direction sortDirection) {
        this.sortProperty = sortProperty;
        this.sortDirection = sortDirection;
    }
}
