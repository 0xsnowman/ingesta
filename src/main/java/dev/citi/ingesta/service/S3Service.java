package dev.citi.ingesta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Client s3;
    private final String bucket;

    public S3Service(@Value("${aws.accessKeyId}") String accessKey,
                     @Value("${aws.secretAccessKey}") String secretKey,
                     @Value("${aws.region}") String region,
                     @Value("${aws.s3.bucket}") String bucket) {
        this.bucket = bucket;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String upload(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        return key;
    }
}
