package com.ciny.studynote.service;

import com.ciny.studynote.dto.SocialRequestDto;
import com.ciny.studynote.dto.SocialUserProfileDto;
import com.ciny.studynote.global.component.SocialRequestInfo;
import com.ciny.studynote.model.User;
import com.ciny.studynote.model.UserRoleEnum;
import com.ciny.studynote.repository.UserRepository;
import com.ciny.studynote.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialUserService {
    private final SocialRequestInfo socialRequestInfo;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public void socialLogin(String code, String provider) throws JsonProcessingException {
        // 1. 소셜로그인 종류 및 정보 생성하기
        SocialRequestDto socialRequestDto = socialRequestInfo.getSocialRequest(provider);
        // 2. 인가코드(code)로 액세스 토큰 요청
        String accessToken = getAccessToken(code, socialRequestDto);
        // 3. 액세스 토큰으로 소셜유저 정보 가져오기
        SocialUserProfileDto socialUserProfileDto = getSocialUserProfile(accessToken, socialRequestDto);
        // 4. 소셜유저 정보가 DB에 없다면 회원가입 진행
        User socialUser = registerSocialUserIfNeeded(socialUserProfileDto);
        // 5. 강제 로그인 처리
        forceLogin(socialUser);
    }

    private String getAccessToken(String code, SocialRequestDto socialRequestDto) throws JsonProcessingException {
        // HTTP Headers 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = getSocialRequestBody(code, socialRequestDto);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> socialRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(socialRequestDto.getTokenUrl(),
                HttpMethod.POST,
                socialRequest,
                String.class
        );

        // HTTP 응답 받기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String accessToken = jsonNode.get("access_token").asText();

        return accessToken;
    }

    private MultiValueMap<String, String> getSocialRequestBody(String code, SocialRequestDto socialRequestDto) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        if (socialRequestDto.getProvider().equals("kakao")) {
            body.add("grant_type", "authorization_code");
            body.add("client_id", socialRequestDto.getClientId());
            body.add("redirect_uri", socialRequestDto.getRedirectUri());
            body.add("code", code);
        } else if (socialRequestDto.getProvider().equals("naver")) {
            body.add("grant_type", "authorization_code");
            body.add("client_id", socialRequestDto.getClientId());
            body.add("redirect_uri", socialRequestDto.getRedirectUri());
            body.add("client_secret", socialRequestDto.getClientSecret());
            body.add("code", code);
        }

        return body;
    }

    private SocialUserProfileDto getSocialUserProfile(String accessToken, SocialRequestDto socialRequestDto) throws JsonProcessingException {
        // HTTP Headers 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> socialUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                socialRequestDto.getProfileUrl(),
                HttpMethod.POST,
                socialUserInfoRequest,
                String.class
        );

        // 응답값에서 프로필 추출 결과 리턴
        return extractProfile(response, socialRequestDto.getProvider());
    }

    private SocialUserProfileDto extractProfile(ResponseEntity<String> response, String provider) throws JsonProcessingException {
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (provider.equals("kakao")) {
            String id = jsonNode.get("id").asText();
            String nickname = jsonNode.get("properties").get("nickname").asText();
            String email = jsonNode.get("kakao_account").get("email").asText();

            return SocialUserProfileDto.builder()
                    .id(id)
                    .nickname(nickname)
                    .email(email)
                    .build();
        } else if (provider.equals("naver")) {
            String id = jsonNode.get("response").get("id").asText();
            String nickname = jsonNode.get("response").get("nickname").asText();
            String email = jsonNode.get("response").get("email").asText();

            return SocialUserProfileDto.builder()
                    .id(id)
                    .nickname(nickname)
                    .email(email)
                    .build();
        } else return null;
    }

    private User registerSocialUserIfNeeded(SocialUserProfileDto socialUserProfileDto) {
        // DB에 중복된 소셜 회원이 있는지 확인
        String socialId = socialUserProfileDto.getId();
        User socialUser = userRepository.findBySocialId(socialId).orElse(null);

        // 중복 유저가 없을 경우에만 회원가입 진행
        if (socialUser == null) {
            String nickname = socialUserProfileDto.getNickname();
            String password = UUID.randomUUID().toString();
            String encodePassword = passwordEncoder.encode(password);
            String email = socialUserProfileDto.getEmail();
            UserRoleEnum role = UserRoleEnum.USER;

            socialUser = User.builder()
                    .username(nickname)
                    .password(encodePassword)
                    .email(email)
                    .role(role)
                    .socialId(socialId)
                    .build();

            userRepository.save(socialUser);
        }

        return socialUser;
    }

    private void forceLogin(User socialUser) {
        // 강제 로그인 처리
        UserDetails userDetails = new UserDetailsImpl(socialUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
