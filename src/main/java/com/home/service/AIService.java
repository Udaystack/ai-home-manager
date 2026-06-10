package com.home.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.model.*;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class AIService {

    private static final String API_URL   = "https://api.anthropic.com/v1/messages";
    private static final String MODEL     = "claude-opus-4-6";
    private static final MediaType JSON   = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final ObjectMapper mapper;
    private final String apiKey;

    public AIService(String apiKey) {
        this.apiKey = apiKey;
        this.http   = new OkHttpClient.Builder()
                .callTimeout(java.time.Duration.ofSeconds(90))
                .build();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    /** Full home health report — tasks, bills, appliances reviewed together. */
    public String generateHomeHealthReport(HomeProfile profile,
                                           List<MaintenanceTask> tasks,
                                           List<Bill> bills,
                                           List<Appliance> appliances) throws IOException {
        String ctx = buildContext(profile, tasks, bills, appliances);
        String prompt = """
                You are an expert home manager and property consultant.
                Review the following home data and produce a comprehensive Home Health Report:
                
                %s
                
                Please provide:
                1. **Overall Home Health Score** (1–10) with brief reasoning
                2. **Urgent Attention Needed** — anything that poses risk or is seriously overdue
                3. **Maintenance Forecast** — predict upcoming maintenance needs for next 6 months based on home age and appliance data
                4. **Cost Analysis** — breakdown of spending patterns and projected annual home maintenance budget
                5. **Appliance Watch List** — appliances nearing end-of-life or with expiring warranties
                6. **Money-Saving Tips** — specific, actionable ways to reduce home running costs
                7. **Seasonal Checklist** — what to do this season based on typical home care calendars
                
                Be specific, practical, and prioritize by impact.
                """.formatted(ctx);
        return callClaude(prompt);
    }

    /** Ask any home-related question with full context. */
    public String askHomeQuestion(HomeProfile profile,
                                  List<MaintenanceTask> tasks,
                                  List<Bill> bills,
                                  List<Appliance> appliances,
                                  String question) throws IOException {
        String ctx = buildContext(profile, tasks, bills, appliances);
        String prompt = """
                You are a knowledgeable home improvement and property management expert.
                Here is the user's home data:
                
                %s
                
                The user asks: %s
                
                Give a clear, practical answer tailored to their specific home situation.
                Include estimated costs, DIY feasibility, and when to call a professional where relevant.
                """.formatted(ctx, question);
        return callClaude(prompt);
    }

    /** Diagnose a specific problem and suggest fixes. */
    public String diagnoseProblem(HomeProfile profile, String problemDescription) throws IOException {
        String prompt = """
                You are an experienced home inspector and contractor.
                
                Home: %s
                
                The homeowner reports this problem: %s
                
                Please provide:
                1. **Likely Causes** — what could be causing this issue (most likely first)
                2. **Severity** — how urgent is this? Could it get worse or cause damage?
                3. **DIY Fix** — can the homeowner fix this themselves? Step-by-step if so.
                4. **Professional Help** — when should they call a professional, and what type?
                5. **Estimated Cost** — rough cost range for DIY vs. professional repair
                6. **Prevention** — how to prevent this from happening again
                """.formatted(profile != null ? profile.toString() : "Unknown", problemDescription);
        return callClaude(prompt);
    }

    /** Generate a seasonal maintenance checklist. */
    public String generateSeasonalChecklist(HomeProfile profile) throws IOException {
        String season = getCurrentSeason();
        String prompt = """
                You are a home maintenance expert creating a seasonal checklist.
                
                Home details: %s
                Current season: %s
                
                Generate a detailed %s maintenance checklist for this specific home.
                Consider the home type, age, features (basement, garage, pool), and systems.
                
                Format as prioritized tasks with:
                - Task name
                - Why it matters right now
                - Estimated time to complete
                - Rough cost (if any)
                - DIY or hire out?
                
                Include safety checks, energy efficiency tips, and anything time-sensitive for this season.
                """.formatted(profile != null ? profile.toString() : "Unknown", season, season);
        return callClaude(prompt);
    }

    /** Analyze bill spending and suggest savings. */
    public String analyzeBillSpending(HomeProfile profile, List<Bill> bills) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (profile != null) sb.append("Home: ").append(profile).append("\n\n");
        sb.append("=== BILL HISTORY ===\n");
        bills.forEach(b -> sb.append(b).append("\n"));

        String prompt = """
                You are a home finance and energy efficiency expert.
                
                %s
                
                Analyze this household's bills and provide:
                1. **Spending Benchmark** — how does this compare to average for a similar home?
                2. **Biggest Saving Opportunities** — where is money being wasted?
                3. **Energy Efficiency Tips** — specific to their heating/cooling setup
                4. **Provider Alternatives** — should they shop around for any services?
                5. **Annual Projection** — what will they likely spend this year?
                6. **Quick Wins** — changes they can make this week to save money
                """.formatted(sb.toString());
        return callClaude(prompt);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildContext(HomeProfile profile, List<MaintenanceTask> tasks,
                                List<Bill> bills, List<Appliance> appliances) {
        StringBuilder sb = new StringBuilder();

        if (profile != null) {
            sb.append("=== HOME PROFILE ===\n").append(profile).append("\n");
            sb.append("Age: ").append(profile.getHomeAgeYears()).append(" years\n");
            sb.append("Features: ");
            if (profile.isHasBasement()) sb.append("Basement, ");
            if (profile.isHasGarage())   sb.append("Garage, ");
            if (profile.isHasPool())     sb.append("Pool, ");
            sb.append("\nHeating: ").append(profile.getHeatingType());
            sb.append(" | Cooling: ").append(profile.getCoolingType()).append("\n\n");
        }

        sb.append("=== MAINTENANCE TASKS (").append(tasks.size()).append(" total) ===\n");
        tasks.forEach(t -> sb.append(t).append(" | Status: ").append(t.getStatus()).append("\n"));

        sb.append("\n=== BILLS (").append(bills.size()).append(" total) ===\n");
        bills.forEach(b -> sb.append(b).append("\n"));

        sb.append("\n=== APPLIANCES (").append(appliances.size()).append(" total) ===\n");
        appliances.forEach(a -> sb.append(a).append("\n"));

        return sb.toString();
    }

    private String getCurrentSeason() {
        int month = java.time.LocalDate.now().getMonthValue();
        return switch (month) {
            case 12, 1, 2 -> "Winter";
            case 3, 4, 5  -> "Spring";
            case 6, 7, 8  -> "Summer";
            default        -> "Fall";
        };
    }

    private String callClaude(String userMessage) throws IOException {
        String body = mapper.writeValueAsString(new ClaudeRequest(MODEL, 2048, userMessage));
        Request req = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(body, JSON))
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = http.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                String rb = response.body() != null ? response.body().string() : "empty";
                throw new IOException("API error " + response.code() + ": " + rb);
            }
            JsonNode root = mapper.readTree(response.body().string());
            return root.path("content").get(0).path("text").asText();
        }
    }

    static class ClaudeRequest {
        public String model;
        public int max_tokens;
        public Message[] messages;
        ClaudeRequest(String model, int max, String msg) {
            this.model = model; this.max_tokens = max;
            this.messages = new Message[]{ new Message("user", msg) };
        }
    }

    static class Message {
        public String role, content;
        Message(String r, String c) { this.role = r; this.content = c; }
    }
}
