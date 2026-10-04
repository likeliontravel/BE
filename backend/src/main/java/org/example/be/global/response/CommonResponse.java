package org.example.be.global.response;

import org.springframework.http.HttpStatus;

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

	// 성공 응답을 위한 정적 메서드 (200 OK)
	public static <T> CommonResponse<T> success(T data, String message) {
		return success(HttpStatus.OK, data, message);
	}

	/**
	 * 2026.10.04 update - 200이 아닌 성공 응답 (201 Created 등)용 오버로드 추가.
	 * 바디의 status 는 반드시 ResponseEntity 헤더의 상태코드와 같은 값을 넘긴다.
	 * 이전에는 status 가 200으로 하드코딩되어 헤더 201 / 바디 200 으로 어긋났다.
	 */
	public static <T> CommonResponse<T> success(HttpStatus status, T data, String message) {
		return CommonResponse.<T>builder()
			.success(true)
			.status(status.value())
			.message(message)
			.data(data)
			.build();
	}

	/**
	 * 실패 응답을 위한 정적 메서드 - advice, ErrorResponseWriter, STOMP 핸들러가 쓴다.
	 * 2026.10.04 update - code 없는 2-인자 error(int, String)를 삭제했다.
	 * @JsonInclude(NON_NULL) 때문에 code 를 빠뜨리면 응답에서 키가 조용히 사라지므로, code 전달을 컴파일러가 강제하게 한다.
	 */
	public static <T> CommonResponse<T> error(int status, String code, String message) {
		return CommonResponse.<T>builder()
			.success(false)
			.status(status)
			.code(code)
			.message(message)
			.build();
	}
}
