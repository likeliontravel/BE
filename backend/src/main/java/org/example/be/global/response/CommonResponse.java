package org.example.be.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

	private boolean success; //요청 성공 여부
	private int status; // HTTP 상태 코드
	/**
	 * 2026.08.31 update - code 필드 추가. 에러 응답에만 포함.
	 * 에러 식별자 (= ErrorCode.name()). 성공 응답 시에는 포함하지 않는다. null로 직렬화에서 제외되도록 한다.
	 *
	 * message는 사람이 읽는 글이고, code는 프론트가 분기에 사용할 수 있는 기계용 식별자다.
	 * 이 분리 덕분에 message 문구를 고치더라도 프론트의 분기가 깨지지 않는다.
	 * code값은 공개 계약이다. 앞으로는 ErrorCode enum의 상수 이름을 바꾸면 계약이 바뀌게 된다.
	 */
	private String code;
	private String message; // 응답 메세지
	private T data; // 실제 데이터 (제네릭 타입)

	// 성공 응답을 위한 정적 메서드
	public static <T> CommonResponse<T> success(T data, String message) {
		return CommonResponse.<T>builder()
			.success(true)
			.status(200)
			.message(message)
			.data(data)
			.build();
	}

	// 실패 응답을 위한 정적 메서드 (code를 모르는 호출부용 - 필터 등)
	public static <T> CommonResponse<T> error(int status, String message) {
		return error(status, null, message);
	}

	// 실패 응답을 위한 정적 메서드 (code 포함 - advice · ErrorResponseWriter가 쓴다)
	public static <T> CommonResponse<T> error(int status, String code, String message) {
		return CommonResponse.<T>builder()
			.success(false)
			.status(status)
			.code(code)
			.message(message)
			.build();
	}
}
