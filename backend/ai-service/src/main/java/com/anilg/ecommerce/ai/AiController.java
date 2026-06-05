package com.anilg.ecommerce.ai;

import com.anilg.ecommerce.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final ChatClient chatClient;
    private final KnowledgeBase knowledgeBase;
    private final RestClient restClient;

    public AiController(ChatClient.Builder chatClientBuilder, KnowledgeBase knowledgeBase) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are an ecommerce AI agent for AI Commerce.
                        Help customers find products, compare options, understand orders,
                        and answer support questions. Be concise and practical.
                        """)
                .build();
        this.knowledgeBase = knowledgeBase;
        this.restClient = RestClient.builder().baseUrl("http://localhost:8080").build();
    }

    @PostMapping("/chat")
    public ApiResponse<AiAnswer> chat(@RequestBody AiQuestion question) {
        String context = String.join("\n", knowledgeBase.search(question.message()));
        String answer = chatClient.prompt()
                .user("""
                        Customer question:
                        %s

                        Use this ecommerce knowledge when useful:
                        %s
                        """.formatted(question.message(), context))
                .call()
                .content();
        return ApiResponse.ok(new AiAnswer(answer, context));
    }

    @PostMapping("/agent/plan")
    public ApiResponse<AgentPlan> plan(@RequestBody AiQuestion question) {
        List<String> steps = List.of(
                "Understand customer intent: " + question.message(),
                "Search catalog-service for matching products",
                "Compare price, stock, and delivery constraints",
                "Recommend products and offer checkout action",
                "Create order through order-service after customer confirmation"
        );
        return ApiResponse.ok(new AgentPlan("ecommerce-shopping-agent", steps));
    }

    @PostMapping("/agent/analyze")
    public ApiResponse<AiAnswer> analyze(@RequestBody AiQuestion question) {
        Object dashboard = restClient.get()
                .uri("/api/analytics/dashboard")
                .retrieve()
                .body(Object.class);
        String answer = chatClient.prompt()
                .user("""
                        Analyze this real-time ecommerce platform dashboard.
                        User request: %s
                        Dashboard/API data: %s

                        Explain funnel health, risky stages, and the next best action.
                        """.formatted(question.message(), dashboard))
                .call()
                .content();
        return ApiResponse.ok(new AiAnswer(answer, String.valueOf(dashboard)));
    }

    @PostMapping("/rag/search")
    public ApiResponse<Map<String, Object>> search(@RequestBody AiQuestion question) {
        List<String> docs = knowledgeBase.search(question.message());
        return ApiResponse.ok(Map.of("query", question.message(), "documents", docs));
    }

    public record AiQuestion(String message) {
    }

    public record AiAnswer(String answer, String retrievedContext) {
    }

    public record AgentPlan(String agent, List<String> steps) {
    }
}
