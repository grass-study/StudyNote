package com.ciny.studynote.global.component;

import com.ciny.studynote.dto.SocialRequestDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialRequestInfo {

    private final KakaoInfo kakaoInfo;
    private final NaverInfo naverInfo;

    public SocialRequestDto getSocialRequest(String provider) {
        if (provider.equals("kakao")) {
            return SocialRequestDto.builder()
                    .provider(provider)
                    .clientId(kakaoInfo.getClientId())
                    .redirectUri(kakaoInfo.getRedirectUri())
                    .tokenUrl(kakaoInfo.getTokenUrl())
                    .profileUrl(kakaoInfo.getProfileUrl())
                    .build();
        } else if (provider.equals("naver")) {
            return SocialRequestDto.builder()
                    .provider(provider)
                    .clientId(naverInfo.getClientId())
                    .redirectUri(naverInfo.getRedirectUri())
                    .tokenUrl(naverInfo.getTokenUrl())
                    .profileUrl(naverInfo.getProfileUrl())
                    .clientSecret(naverInfo.getClientSecret())
                    .build();
        } else return null;
    }

    @Getter
    @Component
    static class KakaoInfo {
        @Value("${spring.social.kakao.client_id}")
        private String clientId;

        @Value("${spring.social.kakao.redirect_uri}")
        private String redirectUri;

        @Value("${spring.social.kakao.token_url}")
        private String tokenUrl;

        @Value("${spring.social.kakao.profile_url}")
        private String profileUrl;
    }

    @Getter
    @Component
    static class NaverInfo {
        @Value("${spring.social.naver.client_id}")
        private String clientId;

        @Value("${spring.social.naver.client_secret}")
        private String clientSecret;

        @Value("${spring.social.naver.redirect_uri}")
        private String redirectUri;

        @Value("${spring.social.naver.token_url}")
        private String tokenUrl;

        @Value("${spring.social.naver.profile_url}")
        private String profileUrl;
    }
}
