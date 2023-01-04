package com.ciny.studynote.service;

import com.ciny.studynote.dto.SignupRequestDto;
import com.ciny.studynote.dto.TokenInfo;
import com.ciny.studynote.global.component.JwtTokenProvider;
import com.ciny.studynote.model.User;
import com.ciny.studynote.model.UserRoleEnum;
import com.ciny.studynote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    @Value("${spring.security.user.admin_token}")
    private static String ADMIN_TOKEN;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Transactional
    public TokenInfo login(String username, String password) {
        // 1. 로그인 id와 password 기반으로 authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        // 2. 실제 검증
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("아이디 혹은 비밀번호가 잘못되었습니다.");
        }
        // 3. 인증 정보를 기반으로 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenInfo;
    }

    @Transactional(readOnly = true)
    public User findLoginUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다"));
    }
}
