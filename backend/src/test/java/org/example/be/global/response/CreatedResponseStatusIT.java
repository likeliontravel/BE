package org.example.be.global.response;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.be.domain.board.dto.request.CommentCreateReqBody;
import org.example.be.domain.board.dto.request.CommentUpdateReqBody;
import org.example.be.domain.board.dto.response.CommentResBody;
import org.example.be.domain.board.service.BoardService;
import org.example.be.domain.board.service.CommentService;
import org.example.be.domain.chat.service.ChatMessageService;
import org.example.be.domain.member.dto.request.MemberJoinReqBody;
import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.service.MemberService;
import org.example.be.global.security.config.SecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

// 응답 상태 «헤더 == 바디» 계약 통합 테스트 (Task 3-2 · D2).
// CommonResponse.success() 가 status 를 200 으로 하드코딩하던 탓에, 헤더로 201 을 반환해도 바디는 200 이 나갔다.
// 이 결함은 새 컨트롤러가 생길 때마다 재생산되므로(2026-10-03 재조사에서 3곳 신규 발견) 헤더와 바디를 «둘 다» 단언해 고정한다.
// 서비스만 mock 으로 갈아끼운다 — 업로드 2곳이 실제 GCS 에 고아 객체를 남기지 않도록 하기 위함이다. (Task 3-2 사용자 결정 D2)
@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
@DisplayName("201/200 응답의 헤더·바디 status 일치 통합 테스트")
class CreatedResponseStatusIT {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MemberService memberService;

	@MockitoBean
	private BoardService boardService;

	@MockitoBean
	private ChatMessageService chatMessageService;

	@MockitoBean
	private CommentService commentService;

	private static final long USER_ID = 10L;

	// 인증된 사용자(SecurityUser principal)를 SecurityContext 에 주입. (CSRF 는 disable 이라 불필요)
	private static RequestPostProcessor authedUser() {
		SecurityUser principal = new SecurityUser(USER_ID, "tester@example.com", "pw", "tester", List.of());
		return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
	}

	private static CommentResBody sampleComment() {
		return new CommentResBody(1L, "tester", null, "댓글", 1L, null, LocalDateTime.now(), new ArrayList<>());
	}

	@Test
	@DisplayName("POST /auth/join — 헤더·바디 둘 다 201")
	void join_returns201InHeaderAndBody() throws Exception {
		// MemberDto.from 이 getRole().name() 을 호출하므로 role 이 채워진 실제 엔티티를 돌려준다
		Member member = Member.createForJoin("join-it@example.com", "tester", "encoded-pw");
		when(memberService.join(any(MemberJoinReqBody.class))).thenReturn(member);

		String body = """
			{"name":"tester","email":"join-it@example.com","password":"Password1!"}
			""";

		mockMvc.perform(post("/auth/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@DisplayName("POST /board/images — 헤더·바디 둘 다 201")
	void uploadBoardImages_returns201InHeaderAndBody() throws Exception {
		when(boardService.uploadBoardImages(anyList(), eq(USER_ID)))
			.thenReturn(List.of("https://example.com/board/a.png"));

		MockMultipartFile file = new MockMultipartFile("files", "a.png", MediaType.IMAGE_PNG_VALUE,
			new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/board/images")
				.file(file)
				.with(authedUser()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@DisplayName("POST /chat/image/upload — 헤더·바디 둘 다 201")
	void uploadChatImage_returns201InHeaderAndBody() throws Exception {
		when(chatMessageService.uploadAndGetPreview(any(), eq("it-group"), eq(USER_ID)))
			.thenReturn("https://example.com/chat/a.png");

		MockMultipartFile image = new MockMultipartFile("image", "a.png", MediaType.IMAGE_PNG_VALUE,
			new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/chat/image/upload")
				.file(image)
				.param("groupName", "it-group")
				.with(authedUser()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@DisplayName("POST /comment/{boardId} — 헤더·바디 둘 다 201")
	void writeComment_returns201InHeaderAndBody() throws Exception {
		when(commentService.writeComment(eq(1L), any(CommentCreateReqBody.class), eq(USER_ID)))
			.thenReturn(sampleComment());

		mockMvc.perform(post("/comment/1")
				.with(authedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"content":"댓글"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(201));
	}

	@Test
	@DisplayName("PUT /comment/{commentId} — 수정은 리소스를 만들지 않으므로 헤더·바디 둘 다 200")
	void updateComment_returns200InHeaderAndBody() throws Exception {
		when(commentService.updateComment(eq(1L), any(CommentUpdateReqBody.class), eq(USER_ID)))
			.thenReturn(sampleComment());

		mockMvc.perform(put("/comment/1")
				.with(authedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"content":"수정된 댓글"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200));
	}
}
