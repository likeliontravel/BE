package org.example.be.board.entity;

import java.util.ArrayList;
import java.util.List;

import org.example.be.board.dto.CommentCreateReqBody;
import org.example.be.board.dto.CommentUpdateReqBody;
import org.example.be.config.Base;
import org.example.be.member.entity.Member;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "comment")
public class Comment extends Base {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member writer;

	@Column(nullable = false)
	private String commentContent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parentcomment_id")
	private Comment parentComment; // null이면 일반 댓글 아니면 대댓글

	@OneToMany(mappedBy = "parentComment", orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<Comment> childComments = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id")
	private Board board;

	// 부모댓글 작성시
	public static Comment toCreateEntity(CommentCreateReqBody reqBody, Member member, Board board,
		Comment parentComment) {
		Comment comment = new Comment();
		comment.setCommentContent(reqBody.content());
		comment.setWriter(member);
		comment.setBoard(board);
		comment.setParentComment(parentComment);
		return comment;
	}

	public void toUpdateEntity(CommentUpdateReqBody reqBody) {
		if (reqBody.content() != null) {
			this.commentContent = reqBody.content();
		}
	}

}
