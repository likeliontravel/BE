package org.example.be.domain.board.repository;

import org.example.be.domain.board.entity.Comment;
import org.example.be.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

	@Modifying
	@Query("DELETE FROM Comment c WHERE c.writer = :writer")
	void deleteByWriter(@Param("writer") Member writer);

}
