package org.example.be.domain.board.entity;

import java.util.ArrayList;
import java.util.List;

import org.example.be.domain.board.dto.BoardCreateReqBody;
import org.example.be.domain.board.dto.BoardUpdateReqBody;
import org.example.be.global.entity.Base;
import org.example.be.domain.member.entity.Member;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "board")
public class Board extends Base {

	// 제목
	@Column(nullable = false, length = 50)
	private String title;

	// 내용
	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
	private String content;

	// 작성자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member writer;

	// 조회수
	@Column(nullable = false)
	private int boardHits;

	// 테마
	@Column(nullable = false)
	private String theme;

	// 지역
	@Column(nullable = false)
	private String region;

	// 썸네일 이미지 URL
	@Column
	private String thumbnailPublicUrl;

	// 댓글 리스트
	@OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<Comment> commentList = new ArrayList<>();

	// 게시글 생성 (정적 팩토리 메서드)
	public static Board toCreateEntity(BoardCreateReqBody req, Member writer, String escapedContent) {
		Board board = new Board();
		board.title = req.title();
		board.content = escapedContent;
		board.writer = writer;
		board.boardHits = 0;
		board.theme = req.theme();
		board.region = req.region();
		board.thumbnailPublicUrl = req.thumbnailPublicUrl();
		return board;
	}

	// 게시글 수정
	public void toUpdateEntity(BoardUpdateReqBody req, String escapedContent) {

		if (req.title() != null) {
			this.title = req.title();
		}

		if (req.content() != null) {
			this.content = escapedContent;
		}

		if (req.theme() != null) {
			this.theme = req.theme();
		}

		if (req.region() != null) {
			this.region = req.region();
		}

		if (req.thumbnailPublicUrl() != null) {
			this.thumbnailPublicUrl = req.thumbnailPublicUrl();
		}
	}

	// 조회수 증가
	public void increaseHits() {
		this.boardHits++;
	}

	// 댓글 연관관계 편의 메서드
	public void addComment(Comment comment) {
		commentList.add(comment);
		comment.setBoard(this);
	}

	public void removeComment(Comment comment) {
		commentList.remove(comment);
		comment.setBoard(null);
	}
}