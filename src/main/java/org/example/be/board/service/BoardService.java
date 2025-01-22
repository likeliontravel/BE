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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;

    // 전체 게시판 글 조회 (페이지 처리)
    public List<BoardDTO> getAllBoards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Board> boardPage = boardRepository.findAll(pageable);
        return boardPage.stream()
                .map(BoardDTO::toDTO) // board 엔티티를 받아와서 dto로 변환
                .collect(Collectors.toList());
    }

    // 인기순 게시판 글 조회 (조회수 기준 정렬 + 페이지 처리)
    public List<BoardDTO> getBoardsSortedByHits(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Board> boardPage = boardRepository.findAllByOrderByBoardHitsDesc(pageable);
        return boardPage.stream()
                .map(BoardDTO::toDTO)
                .collect(Collectors.toList());
    }

    // 게시글 등록
    public void write(BoardDTO boardDTO, MultipartFile imageFile) throws IOException {
        if (boardDTO.getTitle() == null || boardDTO.getContent() == null) {
            throw new IllegalArgumentException("게시글 제목과 내용을 입력해야 합니다.");
        }
        // 파일 첨부 여부에 따라 로직 분리
        if (boardDTO.getImage().isEmpty()) {
            //파일이 없을 때
            Board board = Board.toSaveEntity(boardDTO);
            boardRepository.save(board);
        } else {
            // 파일이 있을 때
            /*
            1.DTO에 담긴 파일을 꺼냄
            2.파일의 이름을 가져옴
            3.서버 저장용 이름으로 만듦 ex) 사용자가 내사진.jpg로 저장하면 -> 384728374_내사진.jpg로 변환하여 중복 제거
            4.저장 경로를 설정
            5.해당 경로에 파일 저장
            6.board_table에 게시글 정보 save 처리
            7.board_file_table에 파일 정보 save 처리
             */
            Board boardEntity = Board.toSaveFileEntity(boardDTO);
            int savedId = boardRepository.save(boardEntity).getId(); //게시글에 한 id를 pk로 쓰기 때문에 가져와야함
            Board board = boardRepository.findById(savedId).get();
            for (MultipartFile image : boardDTO.getImage()) {

                String originalFilename = image.getOriginalFilename(); // 2. 사용자가 올린 파일의 이름
                String storedFileName = System.currentTimeMillis() + "_" + originalFilename; //3. 사진 업로드의 시간으로 중복 구분
                String savePath = "D:/board_img/" + storedFileName; //4
                image.transferTo(new File(savePath)); // 5

                BoardFile boardFile = BoardFile.toBoardFile(board, originalFilename, storedFileName);
                boardFileRepository.save(boardFile);
            }
        }
    }

    // 게시글 수정
    public void updateBoard(BoardDTO boardDTO) {
        Board board = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
        boardRepository.save(Board.toUpdateEntity(boardDTO));
    }

    // 게시글 삭제
    public void deleteBoard(int id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));
        // 게시글에 파일이 첨부되었는지 확인
        if (board.getFileAttached() == 1) {  // fileAttached가 1일 경우 파일이 첨부된 상태
            // 게시글에 연결된 모든 파일 정보 조회
            List<BoardFile> boardFiles = board.getBoardFileList();
            // 파일 삭제 로직
            for (BoardFile boardFile : boardFiles) {
                String storedFileName = boardFile.getStoredFileName();  // 파일의 저장된 이름
                String filePath = "D:/board_img/" + storedFileName; // 실제 파일 경로
                // 로컬 파일 시스템에서 파일 삭제
                File file = new File(filePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        throw new NoSuchElementException("파일 삭제를 실패하였습니다.");
                        // 삭제 실패 시 처리 (원하는 대로 처리할 수 있음)
                        // 예: 예외를 던지거나 다른 로직 수행
                    }
                }
            }
            boardRepository.delete(board);
        }

    }

    // 게시판 검색 (제목/내용 검색)
    public List<BoardDTO> searchBoardsByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("검색 키워드를 입력해야 합니다.");
        }
        List<Board> boards = boardRepository.findByTitleContainingOrContentContaining(keyword, keyword);
        return boards.stream()
                .map(BoardDTO::toDTO)
                .collect(Collectors.toList());
    }
}
