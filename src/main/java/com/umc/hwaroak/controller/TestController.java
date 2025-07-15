package com.umc.hwaroak.controller;

import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ApiResponse;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    //private final TestService testService;

    @GetMapping("/success")
    public void getSuccess() {
        //return testService.successTest();
    }

    @GetMapping("/success/data")
    public ResponseEntity<?> getSuccessData() {
        return ResponseEntity.ok(
                ApiResponse.of(SuccessCode.OK, "성공 응답 예시 확인")
        );
    }

    @GetMapping("/error")
    public ResponseEntity<?> getError() {
        throw new GeneralException(ErrorCode.TEST_ERROR);
    }
}