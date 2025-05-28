package org.example.be.board.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.example.be.board.dto.BoardDTO;
import org.example.be.board.dto.BoardSearchRequestDTO;
import org.example.be.board.dto.SimplePageableRequestDTO;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.SortType;
import org.example.be.board.repository.BoardRepository;
import org.example.be.exception.custom.ForbiddenResourceAccessException;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.place.place_category.PlaceCategoryService;
import org.example.be.place.region.TourRegionService;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.dto.UnifiedUsersNameAndProfileImageUrl;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UnifiedUserService unifiedUserService;
    private final TourRegionService tourRegionService;
    private final PlaceCategoryService placeCategoryService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 30;

    // ======================= 게시글 한 개 조회 ======================= //

    // 게시글 PK ( ID )로 게시글 조회
    @Transactional
    public BoardDTO getBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Id 해당 게시글을 찾을 수 없습니다. id: " + id));

        increaseBoardHits(board);

        BoardDTO dto = BoardDTO.toDTO(board);

        UnifiedUsersNameAndProfileImageUrl profile = unifiedUserService.getNameAndProfileImageUrlByUserIdentifier(board.getWriterIdentifier());
        dto.setWriterProfileImageUrl(profile.getProfileImageUrl());

        return dto;
    }

    // 조회수 증가 처리
    private void increaseBoardHits(Board board) {
        board.setBoardHits(board.getBoardHits() + 1);
        boardRepository.save(board); // 조회수 반영
    }

    // ======================= 게시글 List 조회 ======================= //

    // 전체 게시판 목록 조회 ( 인기순은 sortType값 POPULAR, 최신순은 sortType값 RECENT; 누락 시 기본값 인기순 POPULAR)
    @Transactional
    public List<BoardDTO> getSortedBoardList(SimplePageableRequestDTO request) {

        int page = (request.getPage() == null || request.getPage() < 0) ? DEFAULT_PAGE : request.getPage();
        int size = (request.getSize() == null || request.getSize() <= 0) ? DEFAULT_SIZE : request.getSize();

        SortType sortType = Optional.ofNullable(request.getSortType()).orElse(SortType.POPULAR);

        Pageable pageable = PageRequest.of(page, size);

        return switch (sortType) {
            case POPULAR -> boardRepository.findAllByOrderByBoardHitsDesc(pageable)
                    .stream().map(BoardDTO::toDTO).collect(Collectors.toList());
            case RECENT -> boardRepository.findAllByOrderByUpdatedTimeDesc(pageable)
                    .stream().map(BoardDTO::toDTO).collect(Collectors.toList());
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다. sortType: " + sortType);
        };
    }

    // 검색 게시판 목록 조회 ( 초기라 검색결과가 많지 않을 것 같아서 Pageable 미사용 )
    public List<BoardDTO> searchBoardByKeyword(BoardSearchRequestDTO request) {
        String searchKeyword = request.getSearchKeyword();

        if (searchKeyword == null || searchKeyword.isEmpty()) {
            throw new IllegalArgumentException("키워드를 입력해야 합니다.");
        }

        SortType sortType = Optional.ofNullable(request.getSortType()).orElse(SortType.POPULAR);

        return switch (sortType) {
            case POPULAR -> boardRepository
                    .findByTitleContainingOrContentContainingOrWriterContainingOrderByBoardHitsDesc(searchKeyword, searchKeyword, searchKeyword)
                    .stream().map(BoardDTO::toDTO).collect(Collectors.toList());
            case RECENT -> boardRepository
                    .findByTitleContainingOrContentContainingOrWriterContainingOrderByUpdatedTimeDesc(searchKeyword, searchKeyword, searchKeyword)
                    .stream().map(BoardDTO::toDTO).collect(Collectors.toList());
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다. sortType: " + sortType);
        };
    }

    // 테마 별 게시판 목록 조회
    @Transactional
    public List<BoardDTO> searchBoardByTheme(BoardSearchRequestDTO request) {

        int page = (request.getPage() == null || request.getPage() < 0) ? DEFAULT_PAGE : request.getPage();
        int size = (request.getSize() == null || request.getSize() <= 0) ? DEFAULT_SIZE : request.getSize();

        String theme = request.getTheme();

        if (theme == null || theme.isEmpty()) {
            throw new IllegalArgumentException("테마를 입력해야 합니다.");
        }

        SortType sortType = Optional.ofNullable(request.getSortType()).orElse(SortType.POPULAR);

        Pageable pageable = PageRequest.of(page, size);

        return switch (sortType) {
            case POPULAR -> boardRepository.findByThemeOrderByBoardHitsDesc(theme, pageable)
                    .stream().map(BoardDTO::toDTO).collect(Collectors.toList());
            case RECENT -> boardRepository.findByThemeOrderByUpdatedTimeDesc(theme, pageable)
                    .stream().map(BoardDTO::toDTO).collect(Collectors.toList());
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다. sortType: " + sortType);
        };
    }

    // 지역 별 게시판 목록 조회
    @Transactional
    public List<BoardDTO> searchBoardByRegion(BoardSearchRequestDTO request) {

        int page = (request.getPage() == null || request.getPage() < 0) ? DEFAULT_PAGE : request.getPage();
        int size = (request.getSize() == null || request.getSize() <= 0) ? DEFAULT_SIZE : request.getSize();

        String region = request.getRegion();

        if (region == null || region.isEmpty()) {
            throw new IllegalArgumentException("지역을 입력해야 합니다.");
        }

        SortType sortType = Optional.ofNullable(request.getSortType()).orElse(SortType.POPULAR);

        Pageable pageable = PageRequest.of(page, size);

        return switch (sortType) {
            case POPULAR -> boardRepository.findByRegionOrderByBoardHitsDesc(region, pageable)
                    .stream().map(BoardDTO::toDTO).collect(Collectors.toList());
            case RECENT -> boardRepository.findByRegionOrderByUpdatedTimeDesc(region, pageable)
                    .stream().map(BoardDTO::toDTO).collect(Collectors.toList());
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다. sortType: " + sortType);
        };
    }

    // ======================= 게시글 관리 ( 생성 수정 삭제 ) ======================= //

    // 게시글 작성 ( 생성 )
    @Transactional
    public BoardDTO writeBoard(BoardDTO boardDTO) {

        // 입력된 게시판 필수 입력 누락정보 확인
        validateBoardDTO(boardDTO);

        try {
            // 사용자 인증 확인, 게시글 작성자 값 결정
            String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
            String writer = unifiedUserService.getNameByUserIdentifier(userIdentifier);

            // HTML content escape처리 ( 특수문자 인식 오류 방지, XSS공격 방지 )
            String escapedContent= StringEscapeUtils.escapeHtml4(boardDTO.getContent());
            boardDTO.setContent(escapedContent);

            // 저장할 엔티티로 변환, 작성자 정보 기입
            Board board = Board.toCreateEntity(boardDTO);
            board.setWriter(writer);
            board.setWriterIdentifier(userIdentifier);

            Board savedBoard = boardRepository.save(board);
            return BoardDTO.toDTO(savedBoard);

        } catch (Exception e) {
            throw new ResourceCreationException("게시글 저장 실패. : \n" + e.getMessage());
        }
    }

    // 게시글 수정
    @Transactional
    public BoardDTO updateBoard(BoardDTO boardDTO) {
        // 수정자가 작성자와 일치하는지 확인
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        // 기존 게시글 조회 (없으면 예외 발생)
        Board originalBoard = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));

        if (!userIdentifier.equals(originalBoard.getWriterIdentifier())) {
            throw new ForbiddenResourceAccessException("이 글의 작성자만 이 게시글을 수정할 수 있습니다.");
        }

        // 들어온 수정데이터 유효성 확인
        validateBoardDTO(boardDTO);

        try {
            // HTML escape처리
            String escapedContent = StringEscapeUtils.escapeHtml4(boardDTO.getContent());
            boardDTO.setContent(escapedContent);

            // 작성자는 기존 유지
            boardDTO.setWriter(originalBoard.getWriter());

            // 새로운 게시글 데이터 생성 및 저장
            Board updatedBoard = Board.toUpdateEntity(boardDTO, originalBoard.getWriterIdentifier());
            Board savedBoard = boardRepository.save(updatedBoard);
            return BoardDTO.toDTO(savedBoard);
        } catch (Exception e) {
            throw new ResourceUpdateException("게시글 수정 실패. : \n" + e.getMessage());
        }
    }


    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));

        // 사용자 인증 정보 확인
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        if (!userIdentifier.equals(board.getWriterIdentifier())) {
            throw new ForbiddenResourceAccessException("작성자 본인만 삭제할 수 있습니다.");
        }
            // Lazy 로딩을 사용하면 연관 데이터를 즉시 불러오지 않음.
            // 삭제 전에 size()를 호출하면 실제 데이터가 로딩되며, 연관 엔티티도 정상적으로 삭제됨.
            board.getCommentList().size();

        try {
            boardRepository.delete(board);
            // 실제 삭제 쿼리 강제 실행
            boardRepository.flush();
        } catch (Exception e) {
            throw new ResourceDeletionException("게시글 삭제 실패. : \n" + e.getMessage());
        }
    }

    // ========== 내부 메서드 ==========

    // 게시글 반환결과(List<BoardDTO>)에 작성자 profile image url 추가
    private List<BoardDTO> enrichWithProfileImage(List<Board> boards) {
        Set<String> writerIdentifiers = boards.stream()
                .map(Board::getWriterIdentifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, UnifiedUsersNameAndProfileImageUrl> userProfileMap = writerIdentifiers.stream()
                .map(identifier -> Map.entry(identifier, unifiedUserService.getNameAndProfileImageUrlByUserIdentifier(identifier)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return boards.stream().map(board -> {
            BoardDTO dto = BoardDTO.toDTO(board);
            UnifiedUsersNameAndProfileImageUrl profile = userProfileMap.get(board.getWriterIdentifier());
            if (profile != null) {
                dto.setWriterProfileImageUrl(profile.getProfileImageUrl());
            }
            return dto;
        }).toList();
    }



    // 게시글 입력DTO 유효성 검사
    private void validateBoardDTO(BoardDTO boardDTO) {
        if (boardDTO.getTitle() == null || boardDTO.getTitle().isBlank()) {
            throw new IllegalArgumentException("게시글 제목은 필수 입력입니다.");
        }
        if (boardDTO.getContent() == null || boardDTO.getContent().isBlank()) {
            throw new IllegalArgumentException("게시글 내용은 필수 입력입니다.");
        }
        if (boardDTO.getTheme() == null || boardDTO.getTheme().isBlank()) {
            throw new IllegalArgumentException("테마는 필수 입력입니다.");
        }
        if (boardDTO.getRegion() == null || boardDTO.getRegion().isBlank()) {
            throw new IllegalArgumentException("지역은 필수 입력입니다.");
        }
        if (!placeCategoryService.existsByTheme(boardDTO.getTheme())) {
            throw new IllegalArgumentException("해당 테마는 지원하지 않는 테마입니다.");
        }
        if (!tourRegionService.existsByRegion(boardDTO.getRegion())) {
            throw new IllegalArgumentException("해당 지역은 지원하지 않는 지역입니다.");
        }
    }
}
