package com.rchat.randomChat.member.controller;

import com.rchat.randomChat.global.ApiResponse;
import com.rchat.randomChat.member.dto.CheckEmailDto;
import com.rchat.randomChat.member.dto.SignUpDto;
import com.rchat.randomChat.member.dto.UserInfoResponse;
import com.rchat.randomChat.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/signup")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserInfoResponse>> signUp(@RequestBody SignUpDto signupDto) {
        log.info(signupDto.toString());
        UserInfoResponse userInfoResponse = new UserInfoResponse(memberService.handleSignup(signupDto));
        ApiResponse<UserInfoResponse> apiResponse = ApiResponse.createSuccess("", userInfoResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/checkEmail")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestBody CheckEmailDto emailDto) {
        if (memberService.checkEmail(emailDto)) {
            return ResponseEntity.ok(ApiResponse.createSuccess("사용 가능한 이메일 입니다.", true));
        }
        return ResponseEntity.ok(ApiResponse.createFail("사용 가능한 이메일 입니다.", false));
    }
}
