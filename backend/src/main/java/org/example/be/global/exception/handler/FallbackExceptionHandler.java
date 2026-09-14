package org.example.be.global.exception.handler;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 어디에도 잡히지 않은 예외의 최종 수신처 (catch-all) advice.
 *
 * advice 조회 순서 (4개 파일 상단에 글자 그대로 동일하게 유지할 것)
 *
 *       @Order(0) SseExceptionHandler : SSE 클라이언트 이탈, 비동기 타임아웃
 *       @Order(10) BusinessExceptionHandler : 우리 코드가 throw 한 BusinessException
 *       @Order(20) RequestExceptionHandler : 스프링, 톰캣이 throw 한 프레임워크 예외
 *       @Order(LOWEST_PRECEDENCE) FallbackExceptionHandler : catch-all (Exception)
 *
 * advice 사이에서는 '가장 구체적인 핸들러'가 아니라 '@Order 선착순'으로 결정된다.
 * catch-all 을 가진 Fallback 이 앞에 오면 나머지 세 advice 가 통째로 무력화되어 모든 응답이 500이 된다.
 * 순서가 '필수'인 것은 Fallback 이 마지막이라는 점 하나뿐이다. (나머지 셋은 잡는 예외가 서로 달라 겹치지 않음)
 *
 * 그 '필수' 제약이 바로 이 파일이다. @Order(Ordered.LOWEST_PRECEDENCE) 를 절대 앞당기지 말 것.
 * Exception.class 는 모든 예외에 '직접' 매칭되므로, 이 advice 가 먼저 조회되는 순간 뒤의 advice 는 기회조차 없다.
 * (@Order 를 지워도 기본값이 LOWEST_PRECEDENCE 라 동작은 같지만, 이 제약을 코드에 드러내기 위해 명시했다.)
 *
 * RequestExceptionHandler 에 등록되지 않은 프레임워크 예외도 여기로 와서 500이 된다.
 * 여기 도달했다는 것은 우리가 예상하지 못한 상황이라는 뜻이므로 반드시 스택을 남긴다.
 *
 * advice 의 사정거리 밖 - catch-all 에서도 아래는 여기에 도달하지 못한다.
 * 1. 필터에서 난 예외 : DispatcherServlet 밖이다. 필터가 잡는 BusinessException 은 ErrorResponseWriter 가
 *                  규격 JSON 으로 직접 쓰고, 그 밖의 예외는 컨테이너 error 디스패치(/error)로 가서 스프링 기본 바디로 나간다.
 * 2. @MessageMapping(STOMP) : @MessageExceptionHandler 가 따로 있어야 한다 (현재 우리 프로젝트에 없음)
 * 3. SSE 클라이언트 이탈 : 이건 SseExceptionHandler 가 @Order(0) 으로 먼저 받는다.
 *
 * 핸들러는 전부 3개 인자 CommonResponse.error(status, code, message) 를 쓴다. (이유: BusinessExceptionHandler 상단 주석)
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class FallbackExceptionHandler {

	// SSE 클라이언트 이탈 (IOException) 과 비동기 타임아웃은 SseExceptionHandler가 담당한다.
	// 전역 핸들러로 되돌리지 말 것. - IOException은 상위 타입이라, 여기에 두면
	// SSE가 아닌 모든 컨트롤러의 I/O 실패까지 로그 없이 200 빈 바디로 끝난다.

	/**
	 * 예상치 못한 예외 처리 - 500 응답
	 *
	 * 여기 잡힌 것은 우리가 분류하지 못한 예외다. 최종 Fallback 역할. code(INTERNAL_SERVER_ERROR) 는
	 * "서버에서 알 수 없는 오류가 났다"는 뜻이지 특정 원인을 가리키지 않는다.
	 * 같은 code가 로그에 반복해서 보인다면, 그 원인에 전용 핸들러를 붙이라는 신호로 읽을 것.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
		log.error("[UnhandledException] message={}", e.getMessage(), e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

}
