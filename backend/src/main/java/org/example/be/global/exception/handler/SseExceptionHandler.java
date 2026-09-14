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
 * advice 조회 순서 (4개 파일 상단에 글자 그대로 동일하게 유지할 것)
 *        @Order(0) SseExceptionHandler : SSE 클라이언트 이탈, 비동기 타임아웃
 *        @Order(10) BusinessExceptionHandler : 우리 코드가 throw 한 BusinessException
 *        @Order(20) RequestExceptionHandler : 스프링, 톰캣이 throw한 프레임워크 예외
 *        @Order(LOWEST_PRECEDENCE) FallbackExceptionHandler : catch-all (Exception)
 *
 * advice 사이에서는 '가장 구체적인 핸들러'가 아니라 '@Order 선착순'으로 결정된다.
 * catch-all 을 가진 Fallback 이 앞에 오면 나머지 advice 가 통째로 무력화되어 모든 응답이 500이 된다.
 * 순서가 '필수'인 것은 Fallback 이 마지막이라는 점 하나뿐이다. (나머지 셋은 잡는 예외가 서로 달라 겹치지 않음)
 *
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
