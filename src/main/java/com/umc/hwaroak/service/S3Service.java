package com.umc.hwaroak.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface S3Service {
    String uploadProfileImage(InputStream inputStream, String directoryName);
    void deleteFile(String fileUrl);
}