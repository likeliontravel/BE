package org.example.be.group.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.be.group.announcement.dto.GroupAnnouncementDTO;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GroupDetailDTO {
    private String groupName;
    private String description;
    private String createdName;
    private List<GroupMemberDTO> members;
    private GroupAnnouncementDTO latestAnnouncement;
    private GroupScheduleDTO schedule;
}
