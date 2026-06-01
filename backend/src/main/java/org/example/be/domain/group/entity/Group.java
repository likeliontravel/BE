package org.example.be.domain.group.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.be.domain.group.announcement.entity.GroupAnnouncement;
import org.example.be.domain.member.entity.Member;
import org.example.be.global.entity.Base;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_groups")
public class Group extends Base {

	@Column(nullable = false, unique = true)
	private String groupName;   // 그룹명, 중복 불가

	@ManyToOne
	@JoinColumn(name = "created_by", nullable = false)
	private Member createdBy;  // 그룹 창설자

	// user_groups_members라는 조인테이블 생성. groupId와 userId가 연결되어있는 테이블이 될것임.
	// 자바 상으로는 Member를 멤버로 가지는 Set으로 표현.
	@ManyToMany
	@JoinTable(
		name = "user_groups_members",
		joinColumns = @JoinColumn(name = "user_groups_id"),
		inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	private Set<Member> members = new HashSet<>(); // 그룹 내 멤버를 표현할 것임.

	private String description; // 그룹 설명

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GroupAnnouncement> announcements = new ArrayList<>();

	// 그룹에 멤버를 추가한다. (조인 테이블 user_groups_members에 매핑 행 생성)
	public void addMember(Member member) {
		this.members.add(member);
	}

	// 그룹에서 멤버를 제거한다. (조인 테이블 user_groups_members에서 매핑 행 사라짐)
	public void removeMember(Member member) {
		this.members.remove(member);
	}

	// --- 팩토리 메서드 ---
	// 그룹 생성용 정적 팩토리 메서드
	public static Group create(String groupName, Member createdBy, String description) {
		Group group = new Group();
		group.groupName = groupName;
		group.createdBy = createdBy;
		group.description = description;
		return group;
	}

	// 그룹 설명 수정
	public void updateDescription(String description) {
		this.description = description;
	}

}
