package org.example.be.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.BoardFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BoardDTO {
    private int id;
    private String title;
    private String content;
    private String writer;
    private int boardHits;
    private LocalDateTime createdTime;
    private List<MultipartFile> image; // 이미지 파일을 담는 용도
    private List<String> originalFileName; // 원본 파일 이름
    private List<String> storeFileName; // 서버 저장용 파일 이름
    private int fileAttached; // 파일 첨부 여부(첨부 1, 미첨부 0)

    // 엔티티를 dto로 변환
    public static BoardDTO toDTO(Board board) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(board.getId());
        boardDTO.setTitle(board.getTitle());
        boardDTO.setContent(board.getContent());
        boardDTO.setWriter(board.getWriter());
        boardDTO.setBoardHits(board.getBoardHits());
        boardDTO.setCreatedTime(board.getCreatedTime());
        if (board.getFileAttached() == 0) {
            boardDTO.setFileAttached(board.getFileAttached()); // 0
        } else {
            List<String> originalFileNameList = new ArrayList<>();
            List<String> storedFileNameList = new ArrayList<>();
            boardDTO.setFileAttached(board.getFileAttached()); // 1

            for (BoardFile boardFile : board.getBoardFileList()) {
                originalFileNameList.add(boardFile.getOriginalFileName());
                storedFileNameList.add(boardFile.getStoredFileName());
            }
            boardDTO.setOriginalFileName(originalFileNameList);
            boardDTO.setStoreFileName(storedFileNameList);
        }
        return boardDTO;
    }
}