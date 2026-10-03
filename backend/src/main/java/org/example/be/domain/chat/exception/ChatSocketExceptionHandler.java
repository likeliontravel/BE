package org.example.be.domain.chat.exception;

import org.example.be.domain.chat.controller.ChatMessageSocketController;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * STOMP(@MessageMapping) 처리 중 발생한 예외를 '보낸 세션'에만 CommonResponse 규격으로 돌려주는 advice.
 *
 * HTTP advice 4종 (@RestControllerAdvice) 은 DispatcherServlet 계층 전용이라 @MessageMapping 예외에 닿지 않는다.
 * 이 advice 가 없으면 스프링이 "Unhandled exception from message handler method" 를 ERROR 로 남길 뿐, 클라이언트는 아무것도 모른다.
 *
 * 클라이언트는 '/user/queue/errors' 를 구독해야 이 메시지를 받는다.
 * 구독하지 않으면 기존 동작(무통보)과 같으므로 하위 호환이 깨지지 않는다.
 * 바디는 HTTP 에러 응답과 같은 CommonResponse 라 프론트가 같은 파서를 쓸 수 있다. (STOMP 에는 상태코드가 없어 status 는 바디에만 실린다.)
 *
 * 전달 조건 두 가지 - 코드를 읽어서는 보이지 않으므로 ChatSocketExceptionHandlerIT 가 고정한다.
 * 1. WebSocketConfig 의 enableSimpleBroker 에 "/queue" 가 있어야 한다.
 * '/user/queue/errors' 는 '/queue/errors-user{세션id}' 로 바뀌어 브로커로 가는데, 브로커는 등록된 prefix 로 시작하지 않는 목적지를 조용히 버린다.
 * "/queue" 를 빼도 컴파일, 부팅 모두 정상이고 오류 메시지만 사라진다.
 * 2. broadcast = false 로 '보낸 세션'에만 보낸다.
 * 기본값(true)은 같은 사용자의 모든 세션에 보내는데, 프론트는 채팅방마다 세션을 따로 연다.
 * 기본값이면 A 방에서 난 오류가 같은 사용자가 열어 둔 B 방 탭에도 뜬다.
 *
 * HTTP advice 순서표 (BusinessExceptionHandler 상단) 에 넣지 않는 이유
 * 이 클래스에는 @ExceptionHandler 가 없어 HTTP 쪽 (ExceptionHandlerExceptionResolver) 은 이 advice 를 등록하지 않는다.
 * STOMP 쪽 advice 는 이것 하나뿐이라 @Order 도 필요 없다. 같은 advice 안에서는 더 구체적인 예외 타입의 핸들러가 먼저 선택된다.
 *
 * 한계 - ChatMessageSocketController 는 principal 이 null 이면 예외를 던지지 않고 return 한다.
 * 예외가 없으므로 이 advice 가 발동하지 않아 그 경로는 여전히 무통보다. (인증 실패를 예외로 바꿀지는 채팅 도메인에서 판단할 몫)
 *
 * 응답 문구는 ErrorCode.getMessage(), 로그는 e.getMessage() 로 분리한다. (이유: BusinessExceptionHandler 상단 주석)
 */
@Slf4j
@ControllerAdvice(assignableTypes = ChatMessageSocketController.class)
public class ChatSocketExceptionHandler {

	// 클라이언트 구독 경로는 앞에 사용자 prefix 가 붙은 '/user/queue/errors' 다.
	private static final String ERROR_DESTINATION = "/queue/errors";

	/**
	 * 비즈니스 예외 - 없는 그룹 (GROUP_NOT_FOUND), 그룹 비멤버 발신 (GROUP_ACCESS_DENIED) 등.
	 * 로그 레벨은 BusinessExceptionHandler 와 같이 상태 코드로 가른다. (5xx: ERROR + stack / 4xx: WARN)
	 */
	@MessageExceptionHandler(BusinessException.class)
	@SendToUser(destinations = ERROR_DESTINATION, broadcast = false)
	public CommonResponse<Void> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();

		if (errorCode.getStatus().is5xxServerError()) {
			log.error("[ChatSocket] code={}, detail={}", errorCode.name(), e.getMessage(), e);
		} else {
			log.warn("[ChatSocket] code={}, detail={}", errorCode.name(), e.getMessage());
		}

		return CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage());
	}

	/**
	 * 페이로드 변환 실패 - 깨진 JSON, type 에 없는 enum 값 등. 400으로 응답한다.
	 * HTTP 쪽 RequestExceptionHandler 의 HttpMessageNotReadableException 처리와 같은 역할이다.
	 * 여기서 잡지 않으면 아래 catch-all 로 가서, 클라이언트 입력 문제가 500 + ERROR 스택이 된다.
	 */
	@MessageExceptionHandler(MessageConversionException.class)
	@SendToUser(destinations = ERROR_DESTINATION, broadcast = false)
	public CommonResponse<Void> handleMessageConversionException(MessageConversionException e) {
		ErrorCode errorCode = ErrorCode.INVALID_REQUEST_BODY;
		log.warn("[ChatSocket] code={}, detail={}", errorCode.name(), e.getMessage());
		return CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage());
	}

	/**
	 * 예상하지 못한 예외 - 500 으로 응답한다. 여기 도달했다는 것은 분류하지 못한 예외라는 뜻이므로 반드시 스택을 남긴다.
	 */
	@MessageExceptionHandler(Exception.class)
	@SendToUser(destinations = ERROR_DESTINATION, broadcast = false)
	public CommonResponse<Void> handleException(Exception e) {
		log.error("[ChatSocket] 처리되지 않은 예외 - message={}", e.getMessage(), e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage());
	}
}
