package dev.citi.ingesta.controller;

import dev.citi.ingesta.service.FileValidationService;
import dev.citi.ingesta.service.S3Service;
import dev.citi.ingesta.service.TextractService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final S3Service s3Service;
    private final TextractService textractService;
    private final FileValidationService fileValidationService;

    @Value("${aws.s3.bucket}") private String bucketName;

    public UploadController(S3Service s3Service, TextractService textractService, FileValidationService fileValidationService) {
        this.s3Service = s3Service;
        this.textractService = textractService;
        this.fileValidationService = fileValidationService;
    }

    @PostMapping
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String s3Key = s3Service.upload(file);
            return ResponseEntity.ok("Uploaded as: " + s3Key);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/with-ocr")
    public ResponseEntity<String> uploadAndExtract(@RequestParam("file") MultipartFile file) {
        try {
            fileValidationService.validateFile(file);

            String key = s3Service.upload(file);
            String text = textractService.extractText(bucketName, key);
            return ResponseEntity.ok(text);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Validation failed: " + ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OCR failed: " + e.getMessage());
        }
    }
}
