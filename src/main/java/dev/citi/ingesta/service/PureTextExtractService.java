package dev.citi.ingesta.service;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class TextExtractService {

    public static String extractTextFromDoc(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(fis)) {
            return new WordExtractor(doc).getText();
        }
    }

    public static String extractTextFromDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument docx = new XWPFDocument(fis)) {
            return new XWPFWordExtractor(docx).getText();
        }
    }

    public static String extractTextFromTxt(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }
}
