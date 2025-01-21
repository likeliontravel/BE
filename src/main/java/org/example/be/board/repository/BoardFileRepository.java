package org.example.be.board.repository;

import org.example.be.board.entity.BoardFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardFileRepository extends JpaRepository<BoardFile, Integer> {
}
