package com.umc.hwaroak.serviceImpl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.umc.hwaroak.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String upload(byte[] bytes, String key, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        amazonS3.putObject(bucket, key, inputStream, metadata);
        return amazonS3.getUrl(bucket, key).toString();
    }
}
