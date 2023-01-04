package com.ciny.studynote.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenResponseDto {
    private String grantType;
    private long accessTokenLifetime;
    private long refreshTokenLifetime;
}
