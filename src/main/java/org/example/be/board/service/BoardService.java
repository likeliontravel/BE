package org.example.be.board.service;

import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.BoardDTO;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.BoardFile;
import org.example.be.board.repository.BoardFileRepository;
import org.example.be.board.repository.BoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;
    private final GcsUploader gcsUploader;

    // 전체 게시판 글 조회 (페이지 처리)
    public List<BoardDTO> getAllBoards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Board> boardPage = boardRepository.findAll(pageable);
        return boardPage.stream()
                .map(BoardDTO::toDTO) // board 엔티티를 받아와서 dto로 변환
                .collect(Collectors.toList());
    }

    // 게시글 조회 (조회수 증가 포함)
    @Transactional
    public BoardDTO getBoard(int id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));

        // 조회수 증가
        increaseBoardHits(board);

        return BoardDTO.toDTO(board); // BoardDTO로 변환하여 반환
    }

    // 조회수 증가 처리
    private void increaseBoardHits(Board board) {
        board.setBoardHits(board.getBoardHits() + 1);
        boardRepository.save(board); // 조회수 반영
    }

    // 인기순 게시판 글 조회 (조회수 기준 정렬 + 페이지 처리)
    public List<BoardDTO> getBoardsSortedByHits(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Board> boardPage = boardRepository.findAllByOrderByBoardHitsDesc(pageable);
        return boardPage.stream()
                .map(BoardDTO::toDTO)
                .collect(Collectors.toList());
    }

    // 최신순 게시판 글 조회
    public List<BoardDTO> getBoardSortedByRecents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Board> boardPage = boardRepository.findAllByOrderByCreatedTimeDesc(pageable);
        return boardPage.stream()
                .map(BoardDTO::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void write(BoardDTO boardDTO) throws IOException {
        // 1. 제목과 내용이 없는 경우 예외 처리
        if (boardDTO.getTitle() == null || boardDTO.getContent() == null) {
            throw new IllegalArgumentException("게시글 제목과 내용을 입력해야 합니다.");
        }
        // 2. 게시글 정보 저장
        if (boardDTO.getFileAttached() == 0 || boardDTO.getImage() == null || boardDTO.getImage().isEmpty()) {
            // 파일이 없을 때: 게시글만 저장
            Board board = Board.toSaveEntity(boardDTO);
            boardRepository.save(board);
        } else {
            // 파일이 있을 때: 게시글 및 파일 정보 저장
            Board boardEntity = Board.toSaveFileEntity(boardDTO);
            int savedId = boardRepository.save(boardEntity).getId(); // 게시글의 PK인 ID를 저장
            Board board = boardRepository.findById(savedId).orElseThrow(() -> new IllegalArgumentException("게시글 저장 실패"));
            for (MultipartFile file : boardDTO.getImage()) {
                String originalFilename = file.getOriginalFilename();
                String storedFileName = gcsUploader.uploadImage(file); // GCS에 파일 업로드하는 메서드
                BoardFile boardFile = BoardFile.toBoardFile(board, originalFilename, storedFileName);
                boardFileRepository.save(boardFile);
            }
        }
    }

    // 게시글 수정
    public void updateBoard(BoardDTO boardDTO) throws IOException {
        Board board = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));

        // 게시글 업데이트
        boardRepository.save(Board.toUpdateEntity(boardDTO));

        // 이미지 삭제 및 교체 처리 (fileAttached가 1일 때 삭제)
        if (board.getFileAttached() == 1) {
            // 기존 이미지 삭제 처리
            List<BoardFile> boardFiles = board.getBoardFileList();

            for (BoardFile boardFile : boardFiles) {
                String storedFileName = boardFile.getStoredFileName();
                try {
                    // GCS에서 이미지 삭제
                    gcsUploader.deleteImage(storedFileName);

                    // boardFileRepository에서 해당 파일 정보 삭제
                    boardFileRepository.delete(boardFile);
                } catch (NoSuchElementException e) {
                    throw new NoSuchElementException("파일 삭제를 실패하였습니다.");
                }
            }
        }

        // 새로운 이미지가 있으면 추가 처리
        if (boardDTO.getImage() != null && !boardDTO.getImage().isEmpty()) {
            for (MultipartFile image : boardDTO.getImage()) {
                String storedFileName = gcsUploader.uploadImage(image); // GCS에 이미지 업로드
                BoardFile boardFile = new BoardFile();
                boardFile.setBoard(board);
                boardFile.setOriginalFileName(image.getOriginalFilename());
                boardFile.setStoredFileName(storedFileName);

                // boardFileRepository에 새 파일 정보 저장
                boardFileRepository.save(boardFile);
            }

            // 게시글에 파일이 있으면 fileAttached를 1로 업데이트 (이미지가 추가된 경우)
            board.setFileAttached(1);
            boardRepository.save(board);
        } else {
            // 이미지가 없으면 fileAttached를 0으로 설정 (이미지가 삭제된 경우)
            board.setFileAttached(0);
            boardRepository.save(board);
        }
    }


    // 게시글 삭제
    @Transactional
    public void deleteBoard(int id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));

        // Lazy 로딩을 사용하면 연관 데이터를 즉시 불러오지 않음.
        //삭제 전에 size()를 호출하면 실제 데이터가 로딩되며, 연관 엔티티도 정상적으로 삭제됨.
        board.getBoardFileList().size();
        board.getCommentList().size();

        // 게시글에 파일이 첨부되었는지 확인
        if (board.getFileAttached() == 1) {
            List<BoardFile> boardFiles = board.getBoardFileList();

            // 파일 삭제 로직
            for (BoardFile boardFile : boardFiles) {
                String storedFileName = boardFile.getStoredFileName();
                try {
                    gcsUploader.deleteImage(storedFileName);
                } catch (NoSuchElementException e) {
                    throw new NoSuchElementException("파일 삭제를 실패하였습니다.");
                }
            }
        }
        boardRepository.delete(board);
        // 실제 삭제 쿼리 강제 실행
        boardRepository.flush();
    }

    // 게시판 검색 (제목/내용 검색)
    public List<BoardDTO> searchBoardsByKeyword(String keyword) {
        String validatedKeyword = Optional.ofNullable(keyword)
                .filter(k -> !k.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("검색 키워드를 입력해야 합니다."));

        List<Board> boards = boardRepository.findByTitleContainingOrContentContainingOrWriterContaining(validatedKeyword, validatedKeyword, validatedKeyword);
        return boards.stream()
                .map(BoardDTO::toDTO)
                .collect(Collectors.toList());
    }
}