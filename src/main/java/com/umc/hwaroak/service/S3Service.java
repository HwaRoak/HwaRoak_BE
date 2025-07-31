package com.umc.hwaroak.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadProfileImage(MultipartFile file, String directoryName);
    void deleteFile(String fileUrl);
}