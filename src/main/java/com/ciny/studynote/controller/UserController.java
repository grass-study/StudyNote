package com.ciny.studynote.controller;

import com.ciny.studynote.dto.SignupRequestDto;
import com.ciny.studynote.service.SocialUserService;
import com.ciny.studynote.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final SocialUserService socialUserService;

    // 회원 로그인 페이지
    @GetMapping("/user/login")
    public String login() {
        return "login";
    }

    // 회원 가입 페이지
    @GetMapping("/user/signup")
    public String signup() {
        return "signup";
    }

    // 회원 가입 요청 처리
    @PostMapping("/user/signup")
    public String registerUser(SignupRequestDto requestDto) {
        userService.registerUser(requestDto);
        return "redirect:/user/login";
    }

    @GetMapping("/user/{provider}/callback")
    public String socialLogin(@RequestParam String code, @PathVariable String provider) throws JsonProcessingException {
        socialUserService.socialLogin(code, provider);

        return "redirect:/";
    }
}
