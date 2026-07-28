package org.example.be.domain.board.repository;

import org.example.be.domain.board.entity.Board;
import org.example.be.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom {

	@Modifying
	@Query("DELETE FROM Board b WHERE b.writer = :writer")
	void deleteByWriter(@Param("writer") Member writer);
}
