package com.umc.hwaroak.service.serviceImpl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.umc.hwaroak.response.ErrorCode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String uploadProfileImage(InputStream inputStream, String directoryName) {
        String fileName = directoryName + "/" + UUID.randomUUID() + "_profile.jpg"; // 확장자 고정

        try {
            byte[] bytes = inputStream.readAllBytes(); // 한 번에 읽어야 ContentLength 정확하게 설정 가능
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            metadata.setContentType("image/jpeg"); // 또는 image/webp

            ByteArrayInputStream uploadStream = new ByteArrayInputStream(bytes); // 다시 감싸기

            amazonS3.putObject(bucket, fileName, uploadStream, metadata);
        } catch (IOException e) {
            throw new GeneralException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }


    @Override
    public void deleteFile(String fileUrl) {
        try {
            String fileKey = fileUrl.substring(fileUrl.indexOf(".com/") + 5); // 안전하게 key 추출
            log.info("S3 삭제 요청: bucket={}, key={}", bucket, fileKey);
            amazonS3.deleteObject(bucket, fileKey);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
            throw new GeneralException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

}
