package org.example.be.board.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.text.StringEscapeUtils;
import org.example.be.board.dto.BoardCreateReqBody;
import org.example.be.board.dto.BoardResBody;
import org.example.be.board.dto.BoardSearchReqBody;
import org.example.be.board.dto.BoardUpdateReqBody;
import org.example.be.board.dto.SimplePageableReqBody;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.BoardSortType;
import org.example.be.board.repository.BoardRepository;
import org.example.be.exception.custom.ForbiddenResourceAccessException;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.example.be.place.region.TourRegionService;
import org.example.be.place.theme.PlaceCategoryService;
import org.example.be.security.util.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	private final BoardRepository boardRepository;
	private final MemberRepository memberRepository;
	private final TourRegionService tourRegionService;
	private final PlaceCategoryService placeCategoryService;

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 30;

	// ======================= 게시글 한 개 조회 ======================= //

	// 게시글 PK ( ID )로 게시글 조회
	@Transactional
	public BoardResBody getBoard(Long id) {
		Board board = boardRepository.findById(id)
			.orElseThrow(() -> new NoSuchElementException("Id 해당 게시글을 찾을 수 없습니다. id: " + id));

		board.increaseHits();

		return BoardResBody.from(board, board.getWriter().getProfileImageUrl());
	}

	// ======================= 게시글 List 조회 ======================= //

	// 전체 게시판 목록 조회 ( 인기순은 sortType값 POPULAR, 최신순은 sortType값 RECENT; 누락 시 기본값 인기순 POPULAR)
	@Transactional
	public List<BoardResBody> getSortedBoardList(SimplePageableReqBody reqBody) {

		int page = (reqBody.page() == null || reqBody.page() < 0) ? DEFAULT_PAGE : reqBody.page();
		int size = (reqBody.size() == null || reqBody.size() <= 0) ? DEFAULT_SIZE : reqBody.size();

		BoardSortType boardSortType = Optional.ofNullable(reqBody.boardSortType()).orElse(BoardSortType.POPULAR);

		Pageable pageable = PageRequest.of(page, size);

		List<Board> boards = switch (boardSortType) {
			case POPULAR -> boardRepository.findAllByOrderByBoardHitsDesc(pageable).getContent();
			case RECENT -> boardRepository.findAllByOrderByUpdatedTimeDesc(pageable).getContent();
			default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다. boardSortType: " + boardSortType);
		};

		return enrichWithProfileImage(boards);
	}

	public List<BoardResBody> searchBoardByKeyword(BoardSearchReqBody reqBody) {
		String searchKeyword = reqBody.searchKeyword();

		if (searchKeyword == null || searchKeyword.isEmpty()) {
			throw new IllegalArgumentException("키워드를 입력해야 합니다.");
		}

		int page = (reqBody.page() == null || reqBody.page() < 0) ? DEFAULT_PAGE : reqBody.page();
		int size = (reqBody.size() == null || reqBody.size() <= 0) ? DEFAULT_SIZE : reqBody.size();
		Pageable pageable = PageRequest.of(page, size);

		BoardSortType boardSortType = Optional.ofNullable(reqBody.boardSortType()).orElse(BoardSortType.POPULAR);

		List<Board> boards = switch (boardSortType) {
			case POPULAR ->
				boardRepository.findByTitleContainingOrContentContainingOrWriterContainingOrderByBoardHitsDesc(
					searchKeyword, searchKeyword, searchKeyword, pageable);
			case RECENT ->
				boardRepository.findByTitleContainingOrContentContainingOrWriterContainingOrderByUpdatedTimeDesc(
					searchKeyword, searchKeyword, searchKeyword, pageable);
			default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다. boardSortType: " + boardSortType);
		};

		return enrichWithProfileImage(boards);
	}

	// 테마 별 게시판 목록 조회
	@Transactional
	public List<BoardResBody> searchBoardByTheme(BoardSearchReqBody reqBody) {

		int page = (reqBody.page() == null || reqBody.page() < 0) ? DEFAULT_PAGE : reqBody.page();
		int size = (reqBody.size() == null || reqBody.size() <= 0) ? DEFAULT_SIZE : reqBody.size();

		String theme = reqBody.theme();

		if (theme == null || theme.isEmpty()) {
			throw new IllegalArgumentException("테마를 입력해야 합니다.");
		}

		BoardSortType boardSortType = Optional.ofNullable(reqBody.boardSortType()).orElse(BoardSortType.POPULAR);

		Pageable pageable = PageRequest.of(page, size);

		List<Board> boards = switch (boardSortType) {
			case POPULAR -> boardRepository.findByThemeOrderByBoardHitsDesc(theme, pageable).getContent();
			case RECENT -> boardRepository.findByThemeOrderByUpdatedTimeDesc(theme, pageable).getContent();
			default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다. boardSortType: " + boardSortType);
		};

		return enrichWithProfileImage(boards);
	}

	// 지역 별 게시판 목록 조회
	@Transactional
	public List<BoardResBody> searchBoardByRegion(BoardSearchReqBody reqBody) {

		int page = (reqBody.page() == null || reqBody.page() < 0) ? DEFAULT_PAGE : reqBody.page();
		int size = (reqBody.size() == null || reqBody.size() <= 0) ? DEFAULT_SIZE : reqBody.size();

		String region = reqBody.region();

		if (region == null || region.isEmpty()) {
			throw new IllegalArgumentException("지역을 입력해야 합니다.");
		}

		BoardSortType boardSortType = Optional.ofNullable(reqBody.boardSortType()).orElse(BoardSortType.POPULAR);

		Pageable pageable = PageRequest.of(page, size);

		List<Board> boards = switch (boardSortType) {
			case POPULAR -> boardRepository.findByRegionOrderByBoardHitsDesc(region, pageable).getContent();
			case RECENT -> boardRepository.findByRegionOrderByUpdatedTimeDesc(region, pageable).getContent();
			default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다. boardSortType: " + boardSortType);
		};

		return enrichWithProfileImage(boards);
	}

	// ======================= 게시글 관리 ( 생성 수정 삭제 ) ======================= //

	// 게시글 작성 ( 생성 )
	@Transactional
	public BoardResBody writeBoard(BoardCreateReqBody reqBody) {

		// 입력된 게시판 필수 입력 누락정보 확인
		validateBoardCreate(reqBody);

		try {
			// 사용자 인증 확인, 게시글 작성자 값 결정
			String email = SecurityUtil.getUserIdentifierFromAuthentication();
			Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

			// HTML content escape처리 ( 특수문자 인식 오류 방지, XSS공격 방지 )
			String escapedContent = StringEscapeUtils.escapeHtml4(reqBody.content());

			// 저장할 엔티티로 변환, 작성자 정보 기입
			Board board = Board.toCreateEntity(reqBody, member, escapedContent);

			Board savedBoard = boardRepository.save(board);
			return BoardResBody.from(savedBoard, null);

		} catch (Exception e) {
			throw new ResourceCreationException("게시글 저장 실패. : \n" + e.getMessage());
		}
	}

	// 게시글 수정
	@Transactional
	public BoardResBody updateBoard(BoardUpdateReqBody reqBody) {
		// 수정자가 작성자와 일치하는지 확인
		String email = SecurityUtil.getUserIdentifierFromAuthentication();

		// 기존 게시글 조회 (없으면 예외 발생)
		Board originalBoard = boardRepository.findById(reqBody.id())
			.orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));

		if (!email.equals(originalBoard.getWriter().getEmail())) {
			throw new ForbiddenResourceAccessException("이 글의 작성자만 이 게시글을 수정할 수 있습니다.");
		}
		// 들어온 수정데이터 유효성 확인
		validateBoardUpdate(reqBody);

		try {
			// HTML escape처리
			String escapedContent = reqBody.content() != null ? StringEscapeUtils.escapeHtml4(reqBody.content()) : null;
			originalBoard.toUpdateEntity(reqBody, escapedContent);

			return BoardResBody.from(originalBoard, null);
		} catch (Exception e) {
			throw new ResourceUpdateException("게시글 수정 실패. : \n" + e.getMessage());
		}
	}

	// 게시글 삭제
	@Transactional
	public void deleteBoard(Long id) {
		Board board = boardRepository.findById(id).orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다."));

		// 사용자 인증 정보 확인
		String email = SecurityUtil.getUserIdentifierFromAuthentication();
		if (!email.equals(board.getWriter().getEmail())) {
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
	private List<BoardResBody> enrichWithProfileImage(List<Board> boards) {
		return boards.stream()
			.map(board -> BoardResBody.from(board, board.getWriter().getProfileImageUrl()))
			.toList();
	}

	private void validateBoardCreate(BoardCreateReqBody req) {
		validateTheme(req.theme());
		validateRegion(req.region());
	}

	private void validateBoardUpdate(BoardUpdateReqBody req) {

		if (req.theme() != null) {
			validateTheme(req.theme());
		}

		if (req.region() != null) {
			validateRegion(req.region());
		}

		if (req.title() != null && req.title().isBlank()) {
			throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
		}

		if (req.content() != null && req.content().isBlank()) {
			throw new IllegalArgumentException("내용은 비어 있을 수 없습니다.");
		}
	}

	private void validateTheme(String theme) {
		if (!placeCategoryService.existsByTheme(theme)) {
			throw new IllegalArgumentException("해당 테마는 지원하지 않는 테마입니다.");
		}
	}

	private void validateRegion(String region) {
		if (!tourRegionService.existsByRegion(region)) {
			throw new IllegalArgumentException("해당 지역은 지원하지 않는 지역입니다.");
		}
	}
}
