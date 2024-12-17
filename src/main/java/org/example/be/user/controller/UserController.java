package org.example.be.user.controller;


import lombok.RequiredArgsConstructor;
import org.example.be.response.CommonResponse;
import org.example.be.user.dto.UserDTO;
import org.example.be.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/SignUp")
    public ResponseEntity<CommonResponse<String>> signUp(@RequestBody UserDTO userDTO) {

        userService.signUp(userDTO);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "회원 가입 성공"));
    }

    // 회원 수정
    @PutMapping("/update")
    public ResponseEntity<CommonResponse<String>> updateUser(@RequestBody UserDTO userDTO) {

        try {

            userService.updateUser(userDTO);

            return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null,"회원 수정 성공"));

        } catch (NoSuchElementException e) {

            // 회원을 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponse.error(404,e.getMessage()));

        } catch (IllegalArgumentException e) {

            // 요청 데이터가 없는 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(400,e.getMessage()));
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<CommonResponse<String>> deleteUser(@RequestBody UserDTO userDTO) {

        try {

            userService.deleteUser(userDTO);

            return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null,"회원 삭제 성공"));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponse.error(404,e.getMessage()));
        }
    }

}
