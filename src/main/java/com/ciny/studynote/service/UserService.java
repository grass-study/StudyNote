package com.ciny.studynote.service;

import com.ciny.studynote.dto.SignupRequestDto;
import com.ciny.studynote.dto.SocialUserInfoDto;
import com.ciny.studynote.model.User;
import com.ciny.studynote.model.UserRoleEnum;
import com.ciny.studynote.repository.UserRepository;
import com.ciny.studynote.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    @Value("${spring.security.kakao.client_id}")
    private String clientId;

    @Value("${spring.security.kakao.redirect_uri}")
    private String redirectUri;

    @Value("${spring.security.kakao.request_url}")
    private String requestUrl;

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

    @Transactional
    public void kakaoLogin(String code) throws JsonProcessingException {
        String accessToken = getAccessToken(code);
        SocialUserInfoDto socialUserInfoDto = getSocialUserInfo(accessToken);

        // DB에 중복된 카카오ID 가 있는지 확인
        Long kakaoId = socialUserInfoDto.getId();
        User kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null);

        // 중복 유저가 없을 경우에만 회원가입 진행
        if (kakaoUser == null) {
            String nickname = socialUserInfoDto.getNickname();
            String password = UUID.randomUUID().toString();
            String encodePassword = passwordEncoder.encode(password);
            String email = socialUserInfoDto.getEmail();
            UserRoleEnum role = UserRoleEnum.USER;

            kakaoUser = User.builder()
                    .username(nickname)
                    .password(encodePassword)
                    .email(email)
                    .role(role)
                    .kakaoId(kakaoId)
                    .build();

            userRepository.save(kakaoUser);
        }

        // 강제 로그인 처리
        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getAccessToken(String code) throws JsonProcessingException {
        // HTTP Headers 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange("https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoRequest,
                String.class
        );

        // HTTP 응답 받기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String accessToken = jsonNode.get("access_token").asText();

        return accessToken;
    }

    private SocialUserInfoDto getSocialUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Headers 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                requestUrl,
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties").get("nickname").asText();
        String email = jsonNode.get("kakao_account").get("email").asText();

        return new SocialUserInfoDto(id, nickname, email);
    }
}
