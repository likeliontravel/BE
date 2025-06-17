package org.example.be.group.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class GroupAnnouncementResponseDTO {
    private Long id;
    private String groupName;
    private String title;
    private String content;
    private LocalDateTime timeStamp;
    private String writerName;
}
