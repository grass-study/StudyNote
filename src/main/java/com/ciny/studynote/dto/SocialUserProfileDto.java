package com.ciny.studynote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class SocialUserProfileDto {
    private String id;
    private String nickname;
    private String email;
}
