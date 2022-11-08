package com.ciny.studynote.service;

import com.ciny.studynote.dto.SignupRequestDto;
import com.ciny.studynote.model.User;
import com.ciny.studynote.model.UserRoleEnum;
import com.ciny.studynote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private static final String ADMIN_TOKEN = "///Select_Shop_ADMIN///";

    @Transactional
    public void registerUser(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();

        Optional<User> found = userRepository.findByUsername(username);

        if (found.isPresent()) {
            throw new IllegalArgumentException("중복된 ID가 존재합니다.");
        }
        String password = requestDto.getPassword();
        password = passwordEncoder.encode(requestDto.getPassword());
        String email = requestDto.getEmail();

        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) {
            if (!requestDto.getAdminToken().equals(ADMIN_TOKEN)) {
                throw new IllegalArgumentException("관리자 암호가 달라 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        User user = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .role(role)
                .build();

        userRepository.save(user);
    }


}
