package com.umc.hwaroak.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public interface S3Service {
    /** PUT Presigned URL 생성 (Content-Type 고정 포함) */
    URL createPutPresignedUrl(String objectKey, String contentType, Duration ttl);

    /** 객체 존재 확인(없으면 예외) */
    void headObjectOrThrow(String objectKey);

    /** objectKey로 직접 삭제 */
    void deleteObjectByKey(String objectKey);
}