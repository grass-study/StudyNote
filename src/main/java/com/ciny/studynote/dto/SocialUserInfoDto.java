package com.ciny.studynote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialUserInfoDto {
    private Long id;
    private String nickname;
    private String email;
}
