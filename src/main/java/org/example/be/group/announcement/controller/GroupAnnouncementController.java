package org.example.be.group.announcement.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.group.announcement.dto.GroupAnnouncementCreationRequestDTO;
import org.example.be.group.announcement.dto.GroupAnnouncementDeleteRequestDTO;
import org.example.be.group.announcement.dto.GroupAnnouncementResponseDTO;
import org.example.be.group.announcement.service.GroupAnnouncementService;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/group/announcement")
@RequiredArgsConstructor
public class GroupAnnouncementController {

    private final GroupAnnouncementService groupAnnouncementService;

    // 그룹 공지 생성하기
    @PostMapping("/create")
    public ResponseEntity<CommonResponse<GroupAnnouncementResponseDTO>> createGroupAnnouncement(@RequestBody GroupAnnouncementCreationRequestDTO request) {
        GroupAnnouncementResponseDTO responseDTO = groupAnnouncementService.createGroupAnnouncement(request);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(responseDTO, "그룹 공지 생성 성공"));
    }

    // 가장 최신 공지 1개 조회 ( 최상단에 노출할 공지 )
    @GetMapping("/latestOne")
    public ResponseEntity<CommonResponse<GroupAnnouncementResponseDTO>> getLatestGroupAnnouncement(@RequestParam String groupName) {
        GroupAnnouncementResponseDTO responseDTO = groupAnnouncementService.getLatestAnnouncement(groupName);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(responseDTO, "가장 최신 그룹 공지 1개 조회 성공"));
    }

    // 해당 그룹 모든 공지 조회 ( 그룹 공지사항 전체 조회 )
    @GetMapping("/getAllAnnouncement")
    public ResponseEntity<CommonResponse<List<GroupAnnouncementResponseDTO>>> getAllGroupAnnouncementsByGroupName(@RequestParam("groupName") String groupName) {
        List<GroupAnnouncementResponseDTO> responseDTOList = groupAnnouncementService.getAllGroupAnnouncements(groupName);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(responseDTOList, "그룹 공지 전체 조회 성공"));
    }

    // 그룹 공지 삭제 ( 공지 id로 삭제 )
    @DeleteMapping("/delete")
    public ResponseEntity<CommonResponse<Void>> deleteGroupAnnouncement(@RequestBody GroupAnnouncementDeleteRequestDTO request) {
        groupAnnouncementService.deleteGroupAnnouncement(request);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "그룹 공지 삭제 성공"));
    }
}

// 나중에 내용정리용 주석
// 원준이가 두개 이상의 데이터를 요청해야 할 때는 바디에 담는 것을 선호하다 보니 알잘딱으로 해두긴 하는데
// 아래는 그냥 궁금해서 글 몇개 찾아보다가 이번에 알게됨
// 공부하다 알게된 점 : RESTful 설계 방식에 있어서 GET, DELETE 메서드는 일반적으로 바디를 사용해 요청하지 않는다.
//                  요청 자체에 그 의미가 명확하게 전달되게 하기 위해 바디 안에 데이터를 담지 않고 파라미터에 표현한다.
//                  특수한 경우 바디를 사용해야만 한다면 조직에서 요청 본문의 형식과 내용에 대한 명확한 문서화를 해두는 것이 필수적이다.
//                  특히 DELETE메서드는 요청 대상 리소스와 해당 리소스와의 연결을 제거하는 것을 요청하는 메서드이다. 요청 본문을 바디에 포함시키지 않고 URI에 직접적으로 표현하는게 일반적이라고 한다.
