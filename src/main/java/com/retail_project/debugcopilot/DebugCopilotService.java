package com.retail_project.debugcopilot;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebugCopilotService {

    private static final String MODEL = "gpt-4o-mini";
    private static final int DEFAULT_LOOKBACK_MINUTES = 30;

    private final TempoClient tempoClient;
    private final OpenAIClient openAIClient;

    @CircuitBreaker(name = "openai", fallbackMethod = "analyzeFallback")
    public DebugCopilotResponse analyze(DebugCopilotRequest req) {
        String facts = req.traceId() != null && !req.traceId().isBlank()
                ? buildTraceFacts(req.traceId())
                : buildRecentIssuesFacts(req.serviceName(), req.lookbackMinutes());

        var params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addSystemMessage("""
                        You are a backend debugging assistant. You will be given real span/trace data
                        pulled directly from a distributed tracing backend (Tempo) — not a description,
                        the actual recorded spans. Rules:
                        1. Explain what happened using ONLY the facts given. Never invent a span, a
                           service, or a cause that isn't supported by the data.
                        2. If spans show STATUS_CODE_ERROR, call that out first and explicitly.
                        3. If nothing is abnormal (all spans OK, no unusual latency), say so plainly —
                           do not manufacture a problem to sound useful.
                        4. When explaining latency, point at the specific span(s) consuming the most
                           time, using their real names and durations.
                        5. Keep the answer to 3-6 sentences, plain English, no markdown.
                        """)
                .addUserMessage(facts)
                .build();

        var response = openAIClient.chat().completions().create(params);
        String answer = response.choices().get(0).message().content()
                .orElse("No spans were found for the given input.");

        return new DebugCopilotResponse(answer, facts);
    }

    private String buildTraceFacts(String traceId) {
        List<TempoClient.SpanFact> spans = tempoClient.getTraceSpans(traceId);
        if (spans.isEmpty()) {
            return "No spans found for traceId=" + traceId + ". The trace may not exist, may have expired, or Tempo may be unreachable.";
        }
        StringBuilder sb = new StringBuilder("Trace ").append(traceId).append(" — ")
                .append(spans.size()).append(" spans, in chronological order:\n");
        for (TempoClient.SpanFact span : spans) {
            sb.append("- [%s] %s — %dms (%s)%n"
                    .formatted(span.service(), span.name(), span.durationMs(), span.status()));
        }
        return sb.toString();
    }

    private String buildRecentIssuesFacts(String serviceName, Integer lookbackMinutes) {
        if (serviceName == null || serviceName.isBlank()) {
            return "No traceId or serviceName provided — nothing to analyze.";
        }
        int minutes = lookbackMinutes != null ? lookbackMinutes : DEFAULT_LOOKBACK_MINUTES;

        List<TempoClient.TraceSummary> errors = tempoClient.findRecentTraces(serviceName, minutes, true);
        List<TempoClient.TraceSummary> all = tempoClient.findRecentTraces(serviceName, minutes, false);

        StringBuilder sb = new StringBuilder("Service '%s', last %d minutes:%n".formatted(serviceName, minutes));
        sb.append("- ").append(all.size()).append(" total traces found\n");
        sb.append("- ").append(errors.size()).append(" traces with error status\n");

        if (!errors.isEmpty()) {
            sb.append("Error traces:\n");
            for (TempoClient.TraceSummary t : errors) {
                sb.append("  - traceId=%s, root=%s, duration=%dms%n"
                        .formatted(t.traceId(), t.rootName(), t.durationMs()));
            }
        }

        List<TempoClient.TraceSummary> slowest = all.stream()
                .sorted((a, b) -> Long.compare(b.durationMs(), a.durationMs()))
                .limit(5)
                .toList();
        if (!slowest.isEmpty()) {
            sb.append("Slowest traces in this window:\n");
            for (TempoClient.TraceSummary t : slowest) {
                sb.append("  - traceId=%s, root=%s, duration=%dms%n"
                        .formatted(t.traceId(), t.rootName(), t.durationMs()));
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unused")
    private DebugCopilotResponse analyzeFallback(DebugCopilotRequest req, Throwable t) {
        log.warn("Debug copilot circuit open, OpenAI unavailable: {}", t.getMessage());
        return new DebugCopilotResponse(
                "The debug assistant is temporarily unavailable — the underlying trace data is still visible directly in Grafana.",
                "");
    }
}
