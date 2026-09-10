package org.example.be.global.exception.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * DispatcherServlet 바깥에서 CommonResponse 규격 에러 JSON을 쓰는 유일한 통로.
 *
 * 필터·시큐리티 엔트리포인트·WebSocket 핸드셰이크는 @RestControllerAdvice에 도달하지 못한다.
 * 각자 JSON을 만들면 형식이 조용히 어긋나므로 (실제로 401 문구가 경로마다 3가지였다),
 * 그 지점들은 전부 이 클래스를 거치도록 한다. 응답 포맷을 바꿀 때 고칠 파일은 여기 하나다.
 *
 * ObjectMapper를 주입받는 이유: 스프링이 이미 설정한 인스턴스(날짜 포맷 등)를 그대로 쓰기 위해서다.
 * 정적 유틸(JsonUt)을 쓰면 설정이 갈라지고 테스트에서 대체하기도 어렵다.
 */
@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

	private final ObjectMapper objectMapper;

	public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		write(response, errorCode, errorCode.getMessage());
	}

	public void write(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
		// 이미 커밋된 응답에는 쓸 수 없다. SSE 이탈 등 정상 시나리오이므로 조용히 반환된다.
		if (response.isCommitted()) {
			return;
		}

		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		CommonResponse<Void> body = CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message);

		objectMapper.writeValue(response.getWriter(), body);
	}
}
