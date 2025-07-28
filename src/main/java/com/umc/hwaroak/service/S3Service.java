package com.umc.hwaroak.service;

public interface S3Service {
    /**
     * 이미지 바이트를 받아서 S3에 업로드 후 URL 반환
     * @param bytes 업로드할 이미지 byte 배열
     * @param key S3에 저장할 경로 (예: users/{userId}/profile.png)
     * @param contentType MIME 타입 (예: image/png)
     * @return 업로드된 이미지의 S3 URL
     */
    String upload(byte[] bytes, String key, String contentType);
}