package org.example.be.global.exception.handler;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import lombok.extern.slf4j.Slf4j;

/**
 * SSE 전용 예외처리.
 *
 * 여기서만 void 반환이 허용된다. SSE 이탈 시점에는 응답이 이미 커밋돼 바디를 쓸 수 없고,
 * 이것은 오류가 아니라 정상 시나리오이기 때문이다.
 * (실제로 catch-all이 이 예외를 잡으면 Content-Type이 text/event-stream이라
 * CommonResponse를 직렬화할 컨버터가 없어 2차 예외까지 난다)
 *
 * 잡는 타입을 IOException이 아니라 AsyncRequestNotUsableException으로 좁힌 이유 -
 * IOException은 상위 타입이라 SSE가 아닌 모든 컨트롤러의 I/O 실패까지 삼킨다.
 * 파일 · 외부 연동 실패처럼 진짜 서버 오류인 I/O 실패는 catch-all로 가서 500이 되어야 한다.
 * AsyncRequestNotUsableException은 "비동기 응답이 더 이상 쓸 수 없는 상태"일 때만 발생하므로
 * 이 앱에서는 SSE 이탈과 정확히 일치한다.
 *
 * 컨트롤러 범위 (assignableTypes)로 좁히지 않는 이유 - 실측 결과 동작하지 않는다.
 * emitter.completeWithError()는 /error INCLUDE 디스패치를 일으키는데,
 * 그 시점의 핸들러는 NotificationController가 아니라 BasicErrorController다.
 *
 * advice 조회 순서 - advice 사이에서는 예외 타입의 최적 매칭이 아니라 @Order 선착순이다.
 * 그래서 순서를 코드에 명시한다. 아래 셋은 advice 분리 작업에서 신설되고,
 * 그때까지는 GlobalExceptionHandler가 그 역할을 겸한다 (@Order 없음 = LOWEST_PRECEDENCE)
 *
 * 	0					SseExceptionHandler			(이 파일)
 * 	10					BusinessExceptionHandler
 * 	20					RequestExceptionHandler		(프레임워크 예외)
 * 	LOWEST_PRECEDENCE	FallbackExceptionHandler	(catch-all)
 */
@Slf4j
@Order(0)
@RestControllerAdvice
public class SseExceptionHandler {

	/**
	 * 클라이언트가 브라우저 종료 및 네트워크 끊김 등으로 이탈한 경우.
	 * SseEmitterService가 IOException을 자체적으로 잡아 completeWithError()를 호출하면,
	 * 비동기 ERROR 디스패치를 거쳐 이 핸들러로 들어온다.
	 * 톰캣의 ClientAbortException은 이 예외의 cause로 감싸여 들어온다.
	 */
	@ExceptionHandler(AsyncRequestNotUsableException.class)
	public void handleClientAbort(AsyncRequestNotUsableException e) {
		log.debug("[SSE] 클라이언트 이탈 - {}", e.getMessage());
	}

	/**
	 * 비동기 요청이 타임아웃된 경우. 스트림이 이미 끊긴 상태라 응답 바디를 쓰지 않는다.
	 */
	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public void handleAsyncTimeout(AsyncRequestTimeoutException e) {
		log.debug("[SSE] 비동기 타임아웃 - {}", e.getMessage());
	}
}
