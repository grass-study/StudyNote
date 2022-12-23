package com.ciny.studynote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SocialRequestDto {
    private String provider;
    private String clientId;
    private String redirectUri;
    private String tokenUrl;
    private String profileUrl;

    // 네이버 전용
    private String clientSecret;
}
