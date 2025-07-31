package com.umc.hwaroak.serviceImpl;

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
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String uploadProfileImage(MultipartFile file, String directoryName) {

        String fileName = directoryName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new GeneralException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    @Override
    public void deleteFile(String fileUrl) {
        String fileKey = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
        amazonS3.deleteObject(bucket, fileKey);
    }
}
