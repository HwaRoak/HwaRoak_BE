package com.umc.hwaroak.util;

import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ErrorCode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class UidGenerator {

    public String generatedUid(String kakaoId) {
        try {
            // 1. SHA-256 해시
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(kakaoId.getBytes(StandardCharsets.UTF_8));

            // 2. encoding by Base64
            String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            // 3. 일부만 사용
            return base64.substring(0, 16);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.FAILED_UUID);
        }
    }
}
