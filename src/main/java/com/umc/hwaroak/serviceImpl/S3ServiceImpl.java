package com.umc.hwaroak.serviceImpl;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.umc.hwaroak.response.ErrorCode;

import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Override
    public URL createPutPresignedUrl(String objectKey, String contentType, Duration ttl) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + ttl.toMillis());

            // v1 SDK에서 PUT presign에 Content-Type 고정을 포함하려면
            // 요청에 해당 헤더를 서명에 포함해야 함.
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, objectKey)
                    .withMethod(HttpMethod.PUT)
                    .withExpiration(expiration);

            // Content-Type을 서명에 포함시키기 위해 request header에 추가
            // (클라이언트는 동일한 Content-Type 헤더로 PUT 해야 함)
            req.addRequestParameter("Content-Type", contentType);

            URL url = amazonS3.generatePresignedUrl(req);
            return url;
        } catch (AmazonS3Exception e) {
            log.error("Presigned URL 생성 실패. key={}, ct={}, status={}, error={}",
                    objectKey, contentType, e.getStatusCode(), e.getErrorMessage(), e);
            throw new GeneralException(ErrorCode.PRESIGNED_URL_CREATE_FAILED);
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패(기타). key={}, ct={}", objectKey, contentType, e);
            throw new GeneralException(ErrorCode.PRESIGNED_URL_CREATE_FAILED);
        }
    }

    @Override
    public void headObjectOrThrow(String objectKey) {
        try {
            // 존재하지 않으면 404 또는 NoSuchKey 등 예외
            amazonS3.getObjectMetadata(bucket, objectKey);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                log.warn("S3 객체 없음(404). key={}", objectKey);
                throw new GeneralException(ErrorCode.OBJECT_NOT_FOUND);
            }
            log.error("S3 HEAD 실패. key={}, status={}, error={}", objectKey, e.getStatusCode(), e.getErrorMessage(), e);
            throw new GeneralException(ErrorCode.S3_HEAD_FAILED);
        } catch (SdkClientException e) {
            log.error("S3 HEAD 클라이언트 예외. key={}", objectKey, e);
            throw new GeneralException(ErrorCode.S3_HEAD_FAILED);
        }
    }

    @Override
    public void deleteObjectByKey(String objectKey) {
            if (objectKey == null || objectKey.isBlank()) {
                log.warn("deleteObjectByKey called with empty key, skip delete");
                return; // 혹은 throw new IllegalArgumentException("key is blank");
            }
            amazonS3.deleteObject(bucket, objectKey);
    }

    /** S3 퍼블릭 URL 생성 (버킷 정책에서 profiles/* GET 허용 시 바로 접근 가능) */
    private String buildPublicUrl(String objectKey) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;
    }

    /** 퍼블릭 URL -> objectKey 역추출 (CloudFront 없음 가정) */
    private String extractKeyFromUrl(String url) {
        if (url == null || url.isBlank()) return null;
        String host = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
        if (!url.startsWith(host)) return null;
        return url.substring(host.length());
    }

}
