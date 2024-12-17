package org.example.be.user.service;

import lombok.RequiredArgsConstructor;
import org.example.be.user.domain.User;
import org.example.be.user.dto.UserDTO;
import org.example.be.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    // 의존성 주입
    private final UserRepository userRepository;

    // 회원 가입 로직
    public void signUp(UserDTO userDTO) {

        User user = new User();

        user.setUserEmail(userDTO.getEmail());
        user.setUserPwd(userDTO.getPassword());
        user.setUserName(userDTO.getName());
        user.setUserRole("ROLE_USER");

        userRepository.save(user);
    }

    // 회원 수정 로직
    public void updateUser(UserDTO userDTO) {

        User user = userRepository.findByUserEmail(userDTO.getEmail())
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다"));

        // 유효하지 않은 요청 데이터인 경우
        if (userDTO.getPassword() == null && userDTO.getName() == null) {

            throw new IllegalArgumentException("요청 데이터에 유요한 정보가 없습니다.");
        }

        // 필요한 데이터만 업데이트
        if (userDTO.getPassword() != null) {
            user.setUserPwd(userDTO.getPassword());
        }

        if (userDTO.getName() != null) {
            user.setUserName(userDTO.getName());
        }

        userRepository.save(user);
    }

    // 회원 탈퇴 로직
    public void deleteUser(UserDTO userDTO) {

        Optional<User> userOptional = userRepository.findByUserEmail(userDTO.getEmail());

        if (userOptional.isPresent()) {

            User user = userOptional.get();

            userRepository.delete(user);

        } else {

            throw new NoSuchElementException("회원을 찾을 수 없습니다.");
        }
    }
}
