package com.ciny.studynote.controller;

import com.ciny.studynote.dto.LoginRequestDto;
import com.ciny.studynote.dto.TokenInfo;
import com.ciny.studynote.global.exception.RestApiException;
import com.ciny.studynote.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/user/login")
    public ResponseEntity login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        TokenInfo tokenInfo;
        try {
            tokenInfo = userService.login(loginRequestDto.getUsername(), loginRequestDto.getPassword());
        } catch (BadCredentialsException ex) {
            return new ResponseEntity(RestApiException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .errorMessage(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

        // accessToken reponse header에 담아 보내기
        response.setHeader("selectshop_access_token", tokenInfo.getAccessToken());

        // 리프레시토큰 쿠키에 담아 보내기
        Cookie cookie = new Cookie("selectshop_refresh_token", tokenInfo.getRefreshToken());
        response.addCookie(cookie);
        cookie.setMaxAge((int) tokenInfo.getRefreshTokenLifetime());
        cookie.setSecure(true);
        cookie.setHttpOnly(true);

        return ResponseEntity.ok("Success Create Token");
    }
}
