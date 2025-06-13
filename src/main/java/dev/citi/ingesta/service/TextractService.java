package dev.citi.ingesta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextractService {

    private final TextractClient textract;
    private final String bucket;

    public TextractService(@Value("${aws.accessKeyId}") String accessKey,
                           @Value("${aws.secretAccessKey}") String secretKey,
                           @Value("${aws.region}") String region,
                           @Value("${aws.s3.bucket}") String bucket) {

        this.bucket = bucket;

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.textract = TextractClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String extractText(String key) throws InterruptedException {
        S3Object s3Object = S3Object.builder()
                .bucket(bucket)
                .name(key)
                .build();

        DocumentLocation location = DocumentLocation.builder()
                .s3Object(s3Object)
                .build();

        StartDocumentTextDetectionRequest startRequest = StartDocumentTextDetectionRequest.builder()
                .documentLocation(location)
                .build();

        StartDocumentTextDetectionResponse startResponse = textract.startDocumentTextDetection(startRequest);
        String jobId = startResponse.jobId();

        // Polling for completion (basic wait loop)
        GetDocumentTextDetectionResponse result;
        while (true) {
            Thread.sleep(3000); // wait 3 seconds between polls

            GetDocumentTextDetectionRequest getRequest = GetDocumentTextDetectionRequest.builder()
                    .jobId(jobId)
                    .build();

            result = textract.getDocumentTextDetection(getRequest);

            if (result.jobStatus() == JobStatus.SUCCEEDED) break;
            if (result.jobStatus() == JobStatus.FAILED) throw new RuntimeException("Textract job failed");
        }

        List<Block> blocks = result.blocks();
        return blocks.stream()
                .filter(b -> b.blockType().equals(BlockType.LINE))
                .map(Block::text)
                .collect(Collectors.joining("\n"));
    }
}
