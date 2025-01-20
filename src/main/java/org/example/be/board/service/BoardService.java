package org.example.be.board.service;

import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.BoardDTO;
import org.example.be.board.entity.Board;
import org.example.be.board.repository.BoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    // 전체 게시판 글 조회
    public List<BoardDTO> getAllBoards(int page, int size) {
        List<Board> boards = boardRepository.findAll();
        List<BoardDTO> boardDTOList = new ArrayList<>();
        for (Board board : boards) {
            boardDTOList.add(BoardDTO.toDTO(board));
        }
        return boardDTOList;
    }
    // 게시글 등록
    public void write(BoardDTO boardDTO) {
        Board board = Board.toSavaEntity(boardDTO);
        boardRepository.save(board);
    }

    // 게시글 수정
    public void updateBoard(BoardDTO boardDTO) {
        Optional<Board> optionalBoard = boardRepository.findById(boardDTO.getId());
        if (optionalBoard.isPresent()) {
            Board board = Board.toUpdateEntity(boardDTO);
            boardRepository.save(board);
        } else {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
    }
    // 게시글 삭제
    public void deleteBoard(BoardDTO boardDTO) {
        Optional<Board> optionalBoard = boardRepository.findById(boardDTO.getId());
        if (optionalBoard.isPresent()) {
            boardRepository.delete(optionalBoard.get());
        } else {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
    }
    // 게시글 검색 (제목이나 내용으로 검색)
    public List<BoardDTO> searchBoardsByKeyword(String keyword) {
        List<Board> boards = boardRepository.findByTitleContainingOrContentContaining(keyword, keyword);
        List<BoardDTO> boardDTOList = new ArrayList<>();
        for (Board board : boards) {
            boardDTOList.add(BoardDTO.toDTO(board));
        }
        return boardDTOList;
    }
    // 인기순 정렬 게시판 글 조회 (조회수 기준)
    public List<BoardDTO> getBoardsSortedByHits(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // 조회수 기준으로 정렬된 게시글 리스트 가져오기 (페이징 포함)
        Page<Board> boardPage = boardRepository.findAllByOrderByBoardHitsDesc(pageable);

        List<BoardDTO> boardDTOList = new ArrayList<>();
        for (Board board : boardPage.getContent()) {
            boardDTOList.add(BoardDTO.toDTO(board));
        }
        return boardDTOList;
    }
}

