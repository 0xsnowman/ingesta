package dev.citi.ingesta.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ResumeScoringService {

    @Value("${huggingface.token}")
    private String apiKey;

    public String getScore(String resume, String jobDescription) throws IOException, InterruptedException {

        String prompt = """
            You are an advanced and highly experienced Applicant Tracking System (ATS) with specialized knowledge in the tech industry, including but not limited to [insert specific field here, e.g., software engineering, data science, data analysis, big data engineering]. Your primary task is to meticulously evaluate resumes based on the provided job description. Considering the highly competitive job market, your goal is to offer the best possible guidance for enhancing resumes.

            Responsibilities:

            1. Assess resumes with a high degree of accuracy against the job description.
            2. Identify and highlight missing keywords crucial for the role.
            3. Provide a percentage match score reflecting the resume's alignment with the job requirements on the scale of 1-100.
            4. Offer detailed feedback for improvement to help candidates stand out.
            5. Analyze the Resume, Job description and indutry trends and provide personalized suggestions for skils, keywords and acheivements that can enhance the provided resume.
            6. Provide the suggestions for improving the language, tone and clarity of the resume content.
            7. Provide users with insights into the performance of thier resumes. Track the metrices such as - a) Application Success rates b) Views c) engagement. offers valuable feedback to improve the candidate's chances in the job market use your trained knowledge of gemini trained data . Provide  a application success rate on the scale of 1-100.

            after everytime whenever a usr refersh a page, if the provided job decription and resume is same, then always give same result.

            Field-Specific Customizations:

            Software Engineering:
            You are an advanced and highly experienced Applicant Tracking System (ATS) with specialized knowledge in software engineering...

            Resume: %s
            Description: %s

            I want the only response in 4 sectors as follows:
            • Job Description Match:
            • Missing Keywords:
            • Profile Summary:
            • Personalized suggestions for skills, keywords and achievements that can enhance the provided resume:
            • Application Success rates:
            """.formatted(resume, jobDescription);

        // Escape JSON string content
        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");

        String json = """
            {
              "provider": "novita",
              "model": "deepseek/deepseek-v3-0324",
              "messages": [
                {
                  "role": "user",
                  "content": "%s"
                }
              ]
            }
            """.formatted(escapedPrompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://router.huggingface.co/novita/v3/openai/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse response
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode == 200) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText();
            } catch (Exception e) {
                return "[Error parsing successful response]: " + e.getMessage() + "\nRaw response: " + responseBody;
            }
        } else {
            return "[API Error " + statusCode + "]: " + responseBody;
        }
    }
}
