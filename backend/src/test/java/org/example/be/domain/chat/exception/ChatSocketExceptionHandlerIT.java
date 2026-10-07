package org.example.be.domain.chat.exception;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.exception.GroupErrorCode;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.global.exception.code.CommonErrorCode;
import org.example.be.global.jwt.util.JwtUt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// STOMP 예외 통보 계약 통합 테스트 - Task 2-5 의 안전망.
//
// 이 테스트가 고정하는 것은 '코드를 읽어서는 보이지 않는' 두 규칙이다.
// 1. WebSocketConfig 의 enableSimpleBroker 에 "/queue" 가 있어야 '/user/queue/errors' 가 전달된다.
//    브로커는 등록된 prefix 로 시작하지 않는 목적지를 예외 없이 버리므로, 빠뜨려도 컴파일과 부팅이 모두 정상이다.
// 2. @SendToUser(broadcast = false) 여야 오류가 '보낸 세션'에만 간다. 기본값(true)은 같은 사용자의 모든 세션에 보낸다.
//
// webEnvironment = RANDOM_PORT 인 이유: MockMvc 로는 WebSocket 핸드셰이크와 STOMP 프레임 왕복을 재현할 수 없다.
// 실제 톰캣에 StandardWebSocketClient 로 붙어 SockJS 의 raw WebSocket 경로('/ws/websocket')를 쓴다.
//
// GroupRepository 만 mock 으로 갈아끼운다. 핸드셰이크의 멤버십 검사와 메시지 저장이 같은 리포지토리를 쓰므로,
// '그룹 이름별 스텁' 하나로 DB 픽스처 없이 성공/실패 분기를 만들 수 있다.
// 테스트가 유발하는 경로는 모두 저장 이전에 실패하므로 실제 DB 에 채팅 메시지가 쌓이지 않는다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
@DisplayName("STOMP 예외 통보 계약 통합 테스트")
class ChatSocketExceptionHandlerIT {

	private static final long USER_ID = 91L;
	private static final String USER_EMAIL = "chat-socket-it@example.com";

	// 핸드셰이크(?groupName=)를 통과하는 그룹. 발신 목적지는 테스트마다 따로 고른다.
	private static final String MEMBER_GROUP = "it-member-group";
	private static final String MISSING_GROUP = "it-missing-group";
	private static final String DENIED_GROUP = "it-denied-group";
	private static final String BOOM_GROUP = "it-boom-group";

	private static final String ERROR_SUBSCRIPTION = "/user/queue/errors";
	private static final String VALID_PAYLOAD = "{\"content\":\"hello\",\"type\":\"TEXT\"}";
	private static final String BROKEN_PAYLOAD = "{\"content\":";

	// SUBSCRIBE 와 SEND 는 인바운드 채널의 서로 다른 스레드에서 처리돼 순서가 보장되지 않는다.
	// 구독이 등록되기 전에 보낸 오류는 사라지므로, 받을 때까지 다시 보낸다. (같은 요청이라 부작용이 없다)
	private static final int SEND_ATTEMPTS = 10;
	private static final long RECEIVE_TIMEOUT_MS = 500L;
	private static final long SILENCE_TIMEOUT_MS = 1_000L;
	private static final long CONNECT_TIMEOUT_SECONDS = 5L;

	@LocalServerPort
	private int port;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${spring.jwt.secret}")
	private String jwtSecret;

	@MockitoBean
	private GroupRepository groupRepository;

	private WebSocketStompClient stompClient;
	private final List<StompSession> openedSessions = new ArrayList<>();

	@BeforeEach
	void setUp() {
		// 핸드셰이크는 MEMBER_GROUP 으로만 통과한다.
		when(groupRepository.existsByGroupNameAndMembers_Id(eq(MEMBER_GROUP), anyLong())).thenReturn(true);
		// 그룹은 있으나 멤버가 아닌 상태 - exists 는 스텁하지 않아 기본값 false 가 GROUP_ACCESS_DENIED 를 만든다.
		when(groupRepository.findByGroupName(DENIED_GROUP)).thenReturn(Optional.of(mock(Group.class)));
		// 분류되지 않은 예외(500) 유발용.
		when(groupRepository.findByGroupName(BOOM_GROUP)).thenThrow(new IllegalStateException("테스트용 미분류 예외"));
		// MISSING_GROUP 은 스텁하지 않는다 - Mockito 기본값 Optional.empty() 가 그대로 GROUP_NOT_FOUND 가 된다.

		stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		// TaskScheduler 를 주지 않으므로 하트비트를 끈다. (켜진 채로 두면 CONNECT 단계에서 바로 예외가 난다)
		stompClient.setDefaultHeartbeat(new long[] {0, 0});
	}

	@AfterEach
	void tearDown() {
		for (StompSession session : openedSessions) {
			if (session.isConnected()) {
				session.disconnect();
			}
		}
		openedSessions.clear();
	}

	@Test
	@DisplayName("★ 없는 그룹으로 발신하면 /user/queue/errors 로 404 GROUP_NOT_FOUND 가 온다")
	void businessException_groupNotFound_isDeliveredToUserQueue() throws Exception {
		// 이 테스트가 시간 초과로 실패하면 대개 WebSocketConfig 의 브로커 prefix 에서 "/queue" 가 빠진 것이다.
		StompSession session = connect();
		BlockingQueue<byte[]> received = subscribeErrors(session);

		JsonNode body = sendUntilReceived(session, MISSING_GROUP, VALID_PAYLOAD, received);

		assertThat(body.get("success").asBoolean()).isFalse();
		assertThat(body.get("status").asInt()).isEqualTo(404);
		assertThat(body.get("code").asText()).isEqualTo(GroupErrorCode.GROUP_NOT_FOUND.name());
		assertThat(body.get("message").asText()).isEqualTo(GroupErrorCode.GROUP_NOT_FOUND.getMessage());
		// 응답에는 debugMessage(로그용 detail)가 실리지 않는다.
		assertThat(body.has("data")).isFalse();
	}

	@Test
	@DisplayName("그룹 비멤버가 발신하면 403 GROUP_ACCESS_DENIED 가 온다")
	void businessException_groupAccessDenied_isDeliveredToUserQueue() throws Exception {
		StompSession session = connect();
		BlockingQueue<byte[]> received = subscribeErrors(session);

		JsonNode body = sendUntilReceived(session, DENIED_GROUP, VALID_PAYLOAD, received);

		assertThat(body.get("status").asInt()).isEqualTo(403);
		assertThat(body.get("code").asText()).isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED.name());
	}

	@Test
	@DisplayName("깨진 JSON 페이로드는 400 INVALID_REQUEST_BODY 이며 500 으로 새지 않는다")
	void messageConversionException_brokenPayload_returns400() throws Exception {
		// 전용 핸들러가 없으면 catch-all 로 가서 클라이언트 입력 문제가 500 + ERROR 스택이 된다.
		StompSession session = connect();
		BlockingQueue<byte[]> received = subscribeErrors(session);

		JsonNode body = sendUntilReceived(session, MEMBER_GROUP, BROKEN_PAYLOAD, received);

		assertThat(body.get("status").asInt()).isEqualTo(400);
		assertThat(body.get("code").asText()).isEqualTo(CommonErrorCode.INVALID_REQUEST_BODY.name());
	}

	@Test
	@DisplayName("분류되지 않은 예외는 catch-all 이 500 INTERNAL_SERVER_ERROR 로 받는다")
	void unclassifiedException_returns500() throws Exception {
		// 테스트 로그에 [ChatSocket] 처리되지 않은 예외 ERROR 와 스택이 찍히는 것이 정상이다.
		StompSession session = connect();
		BlockingQueue<byte[]> received = subscribeErrors(session);

		JsonNode body = sendUntilReceived(session, BOOM_GROUP, VALID_PAYLOAD, received);

		assertThat(body.get("status").asInt()).isEqualTo(500);
		assertThat(body.get("code").asText()).isEqualTo(CommonErrorCode.INTERNAL_SERVER_ERROR.name());
	}

	@Test
	@DisplayName("★ 오류는 보낸 세션에만 간다 - 같은 사용자의 다른 세션은 받지 않는다 (broadcast = false)")
	void error_isNotBroadcastToOtherSessionsOfSameUser() throws Exception {
		StompSession sender = connect();
		BlockingQueue<byte[]> senderReceived = subscribeErrors(sender);
		StompSession bystander = connect();
		BlockingQueue<byte[]> bystanderReceived = subscribeErrors(bystander);

		// 먼저 방관자 세션이 '자기' 오류를 받아 구독이 살아 있음을 증명한다. 이 단계가 없으면
		// "못 받았다"가 broadcast 설정 때문인지 구독이 아직 등록되지 않아서인지 구분할 수 없다.
		sendUntilReceived(bystander, MISSING_GROUP, VALID_PAYLOAD, bystanderReceived);
		senderReceived.clear();
		bystanderReceived.clear();

		JsonNode body = sendUntilReceived(sender, MISSING_GROUP, VALID_PAYLOAD, senderReceived);

		assertThat(body.get("code").asText()).isEqualTo(GroupErrorCode.GROUP_NOT_FOUND.name());
		assertThat(bystanderReceived.poll(SILENCE_TIMEOUT_MS, TimeUnit.MILLISECONDS)).isNull();
	}

	// ===== 헬퍼 =====

	private StompSession connect() throws Exception {
		WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
		// 인터셉터는 accessToken 쿠키의 claims 만으로 인증한다. (DB·Redis 조회 없음 - AuthTokenService.payload)
		handshakeHeaders.add(HttpHeaders.COOKIE, "accessToken=" + signedAccessToken());
		// SockJS 의 허용 오리진 목록에 있는 값을 명시한다. (오리진 없는 요청의 동의어 처리에 기대지 않는다)
		handshakeHeaders.setOrigin("https://localhost:3000");

		String url = "ws://localhost:" + port + "/ws/websocket?groupName=" + MEMBER_GROUP;
		StompSession session = stompClient
			.connectAsync(url, handshakeHeaders, new StompSessionHandlerAdapter() {
			})
			.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

		openedSessions.add(session);
		return session;
	}

	private String signedAccessToken() {
		return JwtUt.toString(jwtSecret, 60, Map.of(
			"id", USER_ID,
			"email", USER_EMAIL,
			"name", "chat-socket-it",
			"role", "USER"
		));
	}

	private BlockingQueue<byte[]> subscribeErrors(StompSession session) {
		BlockingQueue<byte[]> received = new LinkedBlockingQueue<>();
		session.subscribe(ERROR_SUBSCRIPTION, new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				// 클라이언트 기본 컨버터(SimpleMessageConverter)는 변환하지 않고 원본 바이트를 넘긴다.
				return byte[].class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				received.add((byte[])payload);
			}
		});
		return received;
	}

	// 오류 메시지를 받을 때까지 같은 발신을 반복한다. 받은 첫 프레임을 JSON 으로 돌려준다.
	private JsonNode sendUntilReceived(StompSession session, String groupName, String payload,
		BlockingQueue<byte[]> received) throws Exception {

		for (int attempt = 0; attempt < SEND_ATTEMPTS; attempt++) {
			send(session, groupName, payload);
			byte[] frame = received.poll(RECEIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			if (frame != null) {
				return objectMapper.readTree(frame);
			}
		}

		throw new AssertionError(ERROR_SUBSCRIPTION + " 으로 오류 메시지가 오지 않았다. (groupName=" + groupName
			+ ") WebSocketConfig 의 enableSimpleBroker 에 \"/queue\" 가 있는지 확인할 것.");
	}

	private void send(StompSession session, String groupName, String payload) {
		StompHeaders headers = new StompHeaders();
		headers.setDestination("/pub/chat/" + groupName);
		headers.setContentType(MediaType.APPLICATION_JSON);
		session.send(headers, payload.getBytes(StandardCharsets.UTF_8));
	}
}