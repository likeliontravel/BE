package org.example.be.board.repository;

import org.example.be.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board> findByTitleContainingOrContentContainingOrWriterContaining(String title, String content,String writer);

    Page<Board> findAllByOrderByBoardHitsDesc(Pageable pageable);

    Page<Board> findAllByOrderByCreatedTimeDesc(Pageable pageable);
}
