package com.hackathon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hackathon.dto.ResumeAnalysis;
import com.hackathon.exception.BadRequestException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ResumeAnalysisService {

    private static final int MAX_RESUME_TEXT_CHARS = 20_000;
    private static final int MAX_SKILLS_SUMMARY_CHARS = 255;

    private static final Logger log = LoggerFactory.getLogger(ResumeAnalysisService.class);

    private final Tika tika = new Tika();
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final GeminiCircuitBreaker circuitBreaker;

    public ResumeAnalysisService(RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model,
            GeminiCircuitBreaker circuitBreaker) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.circuitBreaker = circuitBreaker;
    }

    public ResumeAnalysis analyze(MultipartFile resume, Integer experienceYears) {
        if (resume == null || resume.isEmpty()) {
            log.info("ResumeAnalysis phase=no_resume");
            return emptyAnalysis();
        }

        log.info("ResumeAnalysis phase=text_extraction_start filename={} size={}",
                resume.getOriginalFilename(), resume.getSize());
        String resumeText = extractText(resume);
        log.info("ResumeAnalysis phase=text_extraction_done chars={}", resumeText.length());

        log.info("ResumeAnalysis phase=gemini_start model={}", model);
        JsonNode analysis = analyzeWithGemini(resumeText, experienceYears);
        log.info("ResumeAnalysis phase=gemini_done skillsCount={} extractedExperience={}",
                analysis.path("skills").size(), analysis.path("experienceYearsDetected").asInt(0));

        return toResumeAnalysis(analysis);
    }

    private ResumeAnalysis emptyAnalysis() {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("summary", "No resume uploaded.");
        json.putArray("skills");
        json.put("recommendation", "Resume required for AI analysis.");
        json.putArray("strengths");
        json.putArray("gaps");
        json.put("experienceYearsDetected", 0);
        return new ResumeAnalysis("", json.toString());
    }

    private String extractText(MultipartFile resume) {
        try {
            String text = tika.parseToString(resume.getInputStream());
            if (!StringUtils.hasText(text)) {
                throw new BadRequestException("Could not extract text from resume.");
            }
            return text.length() > MAX_RESUME_TEXT_CHARS ? text.substring(0, MAX_RESUME_TEXT_CHARS) : text;
        } catch (IOException | TikaException exception) {
            throw new BadRequestException("Could not extract text from resume: " + exception.getMessage());
        }
    }

    private JsonNode analyzeWithGemini(String resumeText, Integer experienceYears) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BadRequestException("Gemini API key is not configured. Set GEMINI_API_KEY or gemini.api-key.");
        }
        if (circuitBreaker.isOpen()) {
            throw new BadRequestException(
                    "Gemini API circuit breaker is open — too many recent failures. Will retry automatically.");
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent")
                .queryParam("key", apiKey)
                .buildAndExpand(model)
                .toUriString();

        ObjectNode request = objectMapper.createObjectNode();
        ArrayNode contents = request.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt(resumeText, experienceYears));

        ObjectNode generationConfig = request.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("temperature", 0.2);

        try {
            // Retrieve as raw String to avoid content-type mismatch errors
            // (Gemini may return application/octet-stream even when the body is JSON)
            String rawBody = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            if (!StringUtils.hasText(rawBody)) {
                throw new BadRequestException("Gemini returned an empty response.");
            }
            log.info("ResumeAnalysis phase=gemini_raw_response_received chars={}", rawBody.length());

            JsonNode response = objectMapper.readTree(rawBody);
            String jsonText = response.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text").asText(null);
            if (!StringUtils.hasText(jsonText)) {
                log.error("ResumeAnalysis gemini_response_body={}", rawBody);
                throw new BadRequestException("Gemini did not return resume analysis JSON.");
            }
            log.info("ResumeAnalysis phase=gemini_response_received chars={}", jsonText.length());
            JsonNode result = normalizeAnalysis(objectMapper.readTree(jsonText));
            circuitBreaker.recordSuccess();
            return result;
        } catch (IOException exception) {
            circuitBreaker.recordFailure();
            throw new BadRequestException("Gemini returned invalid resume analysis JSON: " + exception.getMessage());
        } catch (RestClientException exception) {
            circuitBreaker.recordFailure();
            throw new BadRequestException("Gemini resume analysis failed: " + exception.getMessage());
        }
    }

    private String prompt(String resumeText, Integer experienceYears) {
        int declaredExperience = experienceYears == null ? 0 : experienceYears;
        return """
                You are a strict technical recruiter evaluating a resume for a competitive hackathon/recruitment event.
                Be HARSH and OBJECTIVE. Do not give benefit of the doubt. Only reward what is clearly evidenced.

                Return ONLY valid JSON with this exact shape — no markdown, no explanation:
                {
                  "summary": "2-3 sentence objective assessment of the candidate",
                  "skills": ["skill1", "skill2"],
                  "recommendation": "Use one of these neutral labels followed by one short sentence of justification: STRONG_FIT | POTENTIAL_FIT | NEEDS_STRONGER_EVIDENCE | NOT_A_FIT_FOR_THIS_ROUND",
                  "strengths": ["specific strength backed by evidence in resume"],
                  "gaps": ["specific gap or concern"],
                  "redFlags": ["any of: buzzword stuffing, unexplained gaps, vague claims, inflated titles, no measurable outcomes"],
                  "experienceYearsDetected": 0,
                  "scores": {
                    "technicalDepth": 0,
                    "projectQuality": 0,
                    "achievementClarity": 0,
                    "skillsAuthenticity": 0,
                    "consistencyScore": 0
                  }
                }

                SCORING RULES — rate each dimension 0 to 10:
                technicalDepth: Depth of technical knowledge. 8-10 only if candidate demonstrates deep expertise
                  with architecture decisions, advanced concepts, or specialized knowledge — NOT just listing frameworks.
                  Listing React + Node + Python with no depth evidence = max 4.
                projectQuality: Complexity and real-world impact of projects. 8-10 only for production systems,
                  significant scale, or novel work. Tutorial/CRUD projects = max 3. Academic projects = max 5.
                achievementClarity: Are outcomes MEASURABLE? (e.g., "reduced latency by 40%%", "served 10k users").
                  Vague claims like "improved performance" = 2. No achievements at all = 0.
                skillsAuthenticity: Do the listed skills appear to be genuinely used in projects/work?
                  Skills listed with zero supporting evidence = 1 each. Penalize buzzword-only resumes heavily.
                consistencyScore: Does the timeline make sense? Do skills match claimed experience level?
                  Mismatch between declared (%d years) and what resume evidence suggests = deduct heavily.

                STRICT RULES:
                - skills: list only top 10 skills that are EVIDENCED in the resume. Do not list skills mentioned once with no context.
                - strengths: only add a strength if there is EXPLICIT evidence for it in the resume text.
                - gaps: be thorough — missing quantification, shallow projects, no leadership, narrow skillset etc.
                - redFlags: flag anything suspicious. Err on the side of caution.
                - experienceYearsDetected: infer from dates in resume ONLY. If no dates, return 0.

                Declared experience years: %d
                Resume text:
                %s
                """
                .formatted(declaredExperience, declaredExperience, resumeText);
    }

    private JsonNode normalizeAnalysis(JsonNode analysis) {
        ObjectNode normalized = objectMapper.createObjectNode();
        normalized.put("summary", text(analysis, "summary"));
        normalized.set("skills", array(analysis, "skills"));
        normalized.put("recommendation",
                normalizeRecommendation(text(analysis, "recommendation"), text(analysis, "summary")));
        normalized.set("strengths", array(analysis, "strengths"));
        normalized.set("gaps", array(analysis, "gaps"));
        normalized.set("redFlags", array(analysis, "redFlags"));
        normalized.put("experienceYearsDetected", Math.max(0, analysis.path("experienceYearsDetected").asInt(0)));

        // Normalise scores sub-object; clamp each dimension to [0, 10]
        JsonNode rawScores = analysis.path("scores");
        ObjectNode scores = objectMapper.createObjectNode();
        scores.put("technicalDepth", clamp(rawScores.path("technicalDepth").asInt(0), 0, 10));
        scores.put("projectQuality", clamp(rawScores.path("projectQuality").asInt(0), 0, 10));
        scores.put("achievementClarity", clamp(rawScores.path("achievementClarity").asInt(0), 0, 10));
        scores.put("skillsAuthenticity", clamp(rawScores.path("skillsAuthenticity").asInt(0), 0, 10));
        scores.put("consistencyScore", clamp(rawScores.path("consistencyScore").asInt(0), 0, 10));
        normalized.set("scores", scores);
        return normalized;
    }

    private ResumeAnalysis toResumeAnalysis(JsonNode analysis) {
        List<String> skills = new ArrayList<>();
        analysis.path("skills").forEach(skill -> {
            if (StringUtils.hasText(skill.asText())) {
                skills.add(skill.asText());
            }
        });
        String skillsSummary = truncate(String.join(", ", skills), MAX_SKILLS_SUMMARY_CHARS);
        return new ResumeAnalysis(
                skillsSummary,
                analysis.toString());
    }

    private String normalizeRecommendation(String recommendation, String fallbackSummary) {
        if (!StringUtils.hasText(recommendation)) {
            return StringUtils.hasText(fallbackSummary) ? "Needs review — " + fallbackSummary : "Needs review";
        }

        String normalized = recommendation.trim();
        String upper = normalized.toUpperCase(Locale.ROOT);
        if (upper.contains("STRONG_HIRE") || upper.contains("STRONG FIT") || upper.contains("STRONG_FIT")) {
            return "Strong fit" + appendExplanation(normalized, fallbackSummary);
        }
        if (upper.contains("HIRE") || upper.contains("POTENTIAL") || upper.contains("POTENTIAL_FIT")) {
            return "Potential fit" + appendExplanation(normalized, fallbackSummary);
        }
        if (upper.contains("BORDERLINE") || upper.contains("NEEDS_STRONGER_EVIDENCE")
                || upper.contains("NEEDS STRONGER EVIDENCE") || upper.contains("NEEDS IMPROVEMENT")
                || upper.contains("NEEDS_IMPROVEMENT")) {
            return "Needs stronger evidence" + appendExplanation(normalized, fallbackSummary);
        }
        if (upper.contains("REJECT") || upper.contains("NOT_A_FIT") || upper.contains("NOT A FIT")
                || upper.contains("NOT_A_FIT_FOR_THIS_ROUND") || upper.contains("NOT FIT FOR THIS ROUND")) {
            return "Not a fit for this round" + appendExplanation(normalized, fallbackSummary);
        }
        return normalized;
    }

    private String appendExplanation(String recommendation, String fallbackSummary) {
        String explanation = extractExplanation(recommendation);
        if (StringUtils.hasText(explanation)) {
            return " — " + explanation;
        }
        if (StringUtils.hasText(fallbackSummary)) {
            return " — " + fallbackSummary;
        }
        return "";
    }

    private String extractExplanation(String recommendation) {
        String trimmed = recommendation.trim();
        for (String separator : List.of(" — ", " - ", ": ")) {
            int index = trimmed.indexOf(separator);
            if (index >= 0 && index + separator.length() < trimmed.length()) {
                String explanation = trimmed.substring(index + separator.length()).trim();
                if (StringUtils.hasText(explanation)) {
                    return explanation;
                }
            }
        }
        return "";
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText("");
        return StringUtils.hasText(value) ? value : "";
    }

    private ArrayNode array(JsonNode node, String field) {
        ArrayNode values = objectMapper.createArrayNode();
        JsonNode source = node.path(field);
        if (source.isArray()) {
            source.forEach(value -> {
                if (StringUtils.hasText(value.asText())) {
                    values.add(value.asText());
                }
            });
        }
        return values;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
