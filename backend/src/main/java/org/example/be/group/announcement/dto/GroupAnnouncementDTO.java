package org.example.be.group.announcement.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GroupAnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private String writerName;
    private LocalDateTime timeStamp;
}
