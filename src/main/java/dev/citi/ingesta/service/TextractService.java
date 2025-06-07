package dev.citi.ingesta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.util.stream.Collectors;

@Service
public class TextractService {

    private final TextractClient textract;

    public TextractService(@Value("${aws.accessKeyId}") String accessKey,
                           @Value("${aws.secretAccessKey}") String secretKey,
                           @Value("${aws.region}") String region,
                           @Value("${aws.s3.bucket}") String bucket) {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKey,
                secretKey
        );
        this.textract = TextractClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String extractText(String bucket, String key) {
        S3Object s3Object = S3Object.builder()
                .bucket(bucket)
                .name(key)
                .build();

        Document s3Doc = Document.builder()
                .s3Object(s3Object)
                .build();

        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(s3Doc)
                .build();

        DetectDocumentTextResponse response = textract.detectDocumentText(request);

        return response.blocks().stream()
                .filter(b -> b.blockType().equals(BlockType.LINE))
                .map(Block::text)
                .collect(Collectors.joining("\n"));
    }
}
