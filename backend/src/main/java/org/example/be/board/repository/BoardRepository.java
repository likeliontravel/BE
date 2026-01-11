package org.example.be.board.repository;

import org.example.be.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 조건 없이 전체 인기순 ( 조회순 ) 정렬 - 내림차순
    Page<Board> findAllByOrderByBoardHitsDesc(Pageable pageable);

    // 조건 없이 전체 최신순 정렬 - 내림차순
    Page<Board> findAllByOrderByUpdatedTimeDesc(Pageable pageable);

    // 이건 왜 페이저블 안쓰지? 확인해봐야겠음
    // 키워드 검색 시 인기순 ( 조회순 ) 정렬 - 내림차순
    List<Board> findByTitleContainingOrContentContainingOrWriterContainingOrderByBoardHitsDesc(String title, String content, String writer);

    // 키워드 검색 시 최신순 정렬 - 내림차순
    List<Board> findByTitleContainingOrContentContainingOrWriterContainingOrderByUpdatedTimeDesc(String title, String content, String writer);

    // 선택한 테마 인기순 ( 조회순 ) 정렬 - 내림차순
    Page<Board> findByThemeOrderByBoardHitsDesc(String theme, Pageable pageable);

    // 선택한 테마 최신순 정렬 - 내림차순
    Page<Board> findByThemeOrderByUpdatedTimeDesc(String theme, Pageable pageable);

    // 선택한 지역 인기순 ( 조회순 ) 정렬 - 내림차순
    Page<Board> findByRegionOrderByBoardHitsDesc(String region, Pageable pageable);

    // 선택한 지역 최신순 정렬 - 내림차순
    Page<Board> findByRegionOrderByUpdatedTimeDesc(String region, Pageable pageable);
}
