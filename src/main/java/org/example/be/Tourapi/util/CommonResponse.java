package org.example.be.Tourapi.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private boolean success;    // 성공 여부
    private String message;     // 메시지 (성공/실패 사유 등)
    private T data;             // 실제 응답 데이터


 /*


     이 클래스 는 API 응답을 표준화하여 일관된 성공/실패 응답을 제공합니다.

     GlobalExceptionHandler 는 전역에서 발생하는 예외들을 잡아 메시지를 응답해주고

     특히 유효성 검사 실패 시 사용자에게 친절한 메시지를 전달합니다.

    모든 API 응답은 이 클래스를 통해 success, message, data 세 가지 필드를 포함해 반환됩니다.

    이렇게 하면 API 응답 구조가 일관성 있게 유지되어 클라이언트에서 응답 처리하기 편합니다.

    success가 true이면 data가 있고, 실패 시에는 message만 전달하는 구조입니다.

    */



    // 성공 응답 생성
    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(true, message, data);
    }

    // 실패 응답 생성
    public static <T> CommonResponse<T> fail(String message) {
        return new CommonResponse<>(false, message, null);
    }
}