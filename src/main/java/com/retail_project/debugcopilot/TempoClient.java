package com.retail_project.debugcopilot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class TempoClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${TEMPO_QUERY_URL:http://localhost:3200}")
    private String tempoBaseUrl;

    public record SpanFact(String service, String name, long durationMs, String status, long startTimeUnixNano) {}

    public record TraceSummary(String traceId, String rootService, String rootName, long durationMs) {}

    /**
     * Fetches every span in a trace, flattened and time-ordered.
     * Tempo returns OTLP-shaped JSON (batches[].scopeSpans[].spans[]) — parsed directly,
     * no client SDK needed for a read-only query.
     */
    public List<SpanFact> getTraceSpans(String traceId) {
        List<SpanFact> facts = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tempoBaseUrl + "/api/traces/" + traceId))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("Tempo returned {} for trace {}", response.statusCode(), traceId);
                return facts;
            }
            JsonNode root = objectMapper.readTree(response.body());
            for (JsonNode batch : root.path("batches")) {
                String serviceName = extractServiceName(batch.path("resource"));
                for (JsonNode scopeSpan : batch.path("scopeSpans")) {
                    for (JsonNode span : scopeSpan.path("spans")) {
                        long start = span.path("startTimeUnixNano").asLong(0);
                        long end = span.path("endTimeUnixNano").asLong(0);
                        long durationMs = Math.max(0, (end - start) / 1_000_000);
                        String statusCode = span.path("status").path("code").asText("STATUS_CODE_UNSET");
                        facts.add(new SpanFact(serviceName, span.path("name").asText("unknown"), durationMs, statusCode, start));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch trace {} from Tempo: {}", traceId, e.getMessage());
        }
        facts.sort(Comparator.comparingLong(SpanFact::startTimeUnixNano));
        return facts;
    }

    /**
     * TraceQL search for recent traces belonging to a service, optionally error-status only.
     */
    public List<TraceSummary> findRecentTraces(String serviceName, int lookbackMinutes, boolean errorsOnly) {
        List<TraceSummary> summaries = new ArrayList<>();
        try {
            long nowSec = System.currentTimeMillis() / 1000;
            long startSec = nowSec - (lookbackMinutes * 60L);
            String traceQl = errorsOnly
                    ? "{resource.service.name=\"%s\" && status=error}".formatted(serviceName)
                    : "{resource.service.name=\"%s\"}".formatted(serviceName);
            String url = "%s/api/search?q=%s&start=%d&end=%d&limit=20".formatted(
                    tempoBaseUrl,
                    URLEncoder.encode(traceQl, StandardCharsets.UTF_8),
                    startSec, nowSec);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("Tempo search returned {}: {}", response.statusCode(), response.body());
                return summaries;
            }
            JsonNode root = objectMapper.readTree(response.body());
            for (JsonNode trace : root.path("traces")) {
                summaries.add(new TraceSummary(
                        trace.path("traceID").asText(),
                        trace.path("rootServiceName").asText(""),
                        trace.path("rootTraceName").asText(""),
                        trace.path("durationMs").asLong(0)
                ));
            }
        } catch (Exception e) {
            log.warn("Failed to search Tempo for service {}: {}", serviceName, e.getMessage());
        }
        return summaries;
    }

    private String extractServiceName(JsonNode resource) {
        for (JsonNode attr : resource.path("attributes")) {
            if ("service.name".equals(attr.path("key").asText())) {
                return attr.path("value").path("stringValue").asText("unknown");
            }
        }
        return "unknown";
    }
}
