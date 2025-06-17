package org.example.be.group.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.group.announcement.entity.GroupAnnouncement;
import org.example.be.unifieduser.entity.UnifiedUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupName;   // 그룹명, 중복 불가

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private UnifiedUser createdBy;  // 그룹 창설자

    // group_members라는 조인테이블 생성. groupId와 userId가 연결되어있는 테이블이 될것임.
    // 자바 상으로는 UnifiedUser를 멤버로 가지는 Set으로 표현.
    @ManyToMany
    @JoinTable(
            name = "user_groups_members",
            joinColumns = @JoinColumn(name = "user_groups_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UnifiedUser> members = new HashSet<>(); // 그룹 내 멤버를 표현할 것임.

    private String description; // 그룹 설명

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupAnnouncement> announcements = new ArrayList<>();

    // UnifiedUser를 멤버에 추가해주는 함수
    public void addMember(UnifiedUser user) {
        this.members.add(user);
    }

    // UnifiedUser를 멤버에서 제거해주는 함수
    public void removeMember(UnifiedUser user) {
        this.members.remove(user);
    }

}
