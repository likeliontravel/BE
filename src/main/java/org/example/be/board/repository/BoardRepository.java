package org.example.be.board.repository;

import org.example.be.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board> findByTitleContainingOrContentContaining(String title, String content);

    Page<Board> findAllByOrderByBoardHitsDesc(Pageable pageable);
}
