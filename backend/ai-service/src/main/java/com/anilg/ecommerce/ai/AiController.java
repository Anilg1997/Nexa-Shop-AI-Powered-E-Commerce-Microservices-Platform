package com.anilg.ecommerce.ai;

import com.anilg.ecommerce.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final ChatClient chatClient;
    private final KnowledgeBase knowledgeBase;
    private final RestClient restClient;

    public AiController(ChatClient.Builder chatClientBuilder, KnowledgeBase knowledgeBase) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are an ecommerce AI agent for Nexa Shop.
                        Help customers find products, compare options, understand orders,
                        and answer support questions. Be concise and practical.
                        Use the retrieved knowledge context to give accurate answers.
                        """)
                .build();
        this.knowledgeBase = knowledgeBase;
        this.restClient = RestClient.builder().baseUrl("http://localhost:8080").build();
    }

    @PostMapping("/chat")
    public ApiResponse<AiAnswer> chat(@RequestBody AiQuestion question) {
        String context = String.join("\n\n", knowledgeBase.search(question.message()));
        String answer = chatClient.prompt()
                .user("""
                        Customer question: %s
                        Retrieved knowledge context: %s
                        """.formatted(question.message(), context))
                .call()
                .content();
        return ApiResponse.ok(new AiAnswer(answer, context));
    }

    @PostMapping("/agent/plan")
    public ApiResponse<AgentPlan> plan(@RequestBody AiQuestion question) {
        String plan = chatClient.prompt()
                .user("""
                        Create a step-by-step agent plan to handle this customer request: %s
                        Return a numbered list of concrete steps the AI agent should take.
                        """.formatted(question.message()))
                .call()
                .content();
        return ApiResponse.ok(new AgentPlan("ecommerce-shopping-agent",
                List.of(plan.split("\n")).stream().filter(l -> l.matches(".*\\d+\\..*")).toList()));
    }

    @PostMapping("/agent/analyze")
    public ApiResponse<AiAnswer> analyze(@RequestBody AiQuestion question) {
        Object dashboard = restClient.get()
                .uri("/api/analytics/dashboard")
                .retrieve()
                .body(Object.class);
        String context = String.join("\n", knowledgeBase.search("analytics events dashboard"));
        String answer = chatClient.prompt()
                .user("""
                        Analyze this real-time ecommerce platform dashboard.
                        User request: %s
                        Dashboard data: %s
                        Knowledge context: %s
                        Explain funnel health, risky stages, and the next best action.
                        """.formatted(question.message(), dashboard, context))
                .call()
                .content();
        return ApiResponse.ok(new AiAnswer(answer, String.valueOf(dashboard)));
    }

    @PostMapping("/rag/search")
    public ApiResponse<Map<String, Object>> search(@RequestBody AiQuestion question) {
        List<String> docs = knowledgeBase.search(question.message());
        return ApiResponse.ok(Map.of("query", question.message(), "documents", docs, "count", docs.size()));
    }

    @PostMapping("/mcp/execute")
    public ApiResponse<MCPToolResult> executeMcp(@RequestBody MCPRequest request) {
        return switch (request.tool()) {
            case "dashboard_analysis" -> {
                Object dashboard = restClient.get().uri("/api/analytics/dashboard").retrieve().body(Object.class);
                yield ApiResponse.ok(new MCPToolResult("dashboard_analysis",
                        "Dashboard data retrieved: " + dashboard, "success"));
            }
            case "product_search" -> {
                Object products = restClient.get().uri("/api/catalog/products").retrieve().body(Object.class);
                yield ApiResponse.ok(new MCPToolResult("product_search",
                        "Products retrieved: " + products, "success"));
            }
            case "system_knowledge" -> {
                String context = String.join("\n", knowledgeBase.search(request.params().toString()));
                yield ApiResponse.ok(new MCPToolResult("system_knowledge", context, "success"));
            }
            default -> ApiResponse.ok(new MCPToolResult("unknown", "Tool not found: " + request.tool(), "error"));
        };
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("ai-service ready | Ollama: " +
                System.getenv().getOrDefault("OLLAMA_BASE_URL", "http://localhost:11434"));
    }

    public record AiQuestion(String message) {}
    public record AiAnswer(String answer, String retrievedContext) {}
    public record AgentPlan(String agent, List<String> steps) {}
    public record MCPRequest(String tool, Map<String, Object> params) {}
    public record MCPToolResult(String tool, String result, String status) {}
}
