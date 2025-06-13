package dev.citi.ingesta.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileValidationService {
    private static final List<String> ALLOWED_TYPES = List.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_PDF_VALUE // OCR fails for PDF now
    );

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB max

    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, PDF.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the 5MB limit.");
        }
    }
}
