package dev.citi.ingesta.controller;

import dev.citi.ingesta.service.FileValidationService;
import dev.citi.ingesta.service.ResumeScoringService;
import dev.citi.ingesta.service.S3Service;
import dev.citi.ingesta.service.TextractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/score-api")
public class ScoreController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private TextractService textractService;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private ResumeScoringService resumeScoringService;

    @PostMapping("/upload-and-score")
    public ResponseEntity<String> uploadAndScore(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription
    ) {
        try {
            fileValidationService.validateFile(file);

            String key = s3Service.upload(file);
            String resume = textractService.extractText(key);
            String result = resumeScoringService.getScore(resume, jobDescription);
            return ResponseEntity.ok("Score: " + result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Validation failed: " + ex.getMessage());
        }  catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
