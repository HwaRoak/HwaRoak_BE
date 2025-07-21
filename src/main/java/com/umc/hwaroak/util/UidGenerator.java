package com.umc.hwaroak.util;

import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class UidGenerator {

    public static String generateShortUid(String kakaoId) {
        try {
            // byte[] 변환 후 SHA-256 해싱
            byte[] inputBytes = kakaoId.getBytes(StandardCharsets.UTF_8);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(inputBytes);

            // Base64 URL-safe encoding 후 앞 16자리 사용
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(hash)
                    .substring(0, 16);

        } catch (NoSuchAlgorithmException e) {
            throw new GeneralException(ErrorCode.FAILED_UUID);
        }
    }
}
