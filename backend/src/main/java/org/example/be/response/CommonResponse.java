package org.example.be.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private boolean success; //요청 성공 여부
    private int status; // HTTP 상태 코드
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

    // 실패 응답을 위한 정적 메서드
    public static <T> CommonResponse<T> error(int code, String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .status(code)
                .message(message)
                .build();
    }
}
