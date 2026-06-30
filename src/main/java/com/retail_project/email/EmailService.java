package com.retail_project.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${MAIL_PASSWORD:disabled}")
    private String resendApiKey;

    @Value("${app.mail.from:NovaMart <onboarding@resend.dev>}")
    private String fromAddress;

    @Async
    public void sendPasswordReset(String toEmail, String firstName, String resetLink) {
        send(toEmail, "Reset your NovaMart password", buildPasswordResetHtml(firstName, resetLink));
    }

    @Async
    public void sendOrderConfirmation(String toEmail, String firstName,
                                      String orderReference, BigDecimal total,
                                      List<String> itemLines) {
        send(toEmail, "Your NovaMart order is confirmed!", buildOrderHtml(firstName, orderReference, total, itemLines));
    }

    private void send(String toEmail, String subject, String html) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "from", fromAddress,
                    "to", List.of(toEmail),
                    "subject", subject,
                    "html", html
            ));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("Resend API error sending to {}: {} {}", toEmail, response.statusCode(), response.body());
            } else {
                log.info("Email sent to {} via Resend (status {})", toEmail, response.statusCode());
            }
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildPasswordResetHtml(String firstName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px;">
                  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                    <div style="background:#1a1a2e;padding:30px;text-align:center;">
                      <h1 style="color:#f5c518;margin:0;font-size:24px;">NovaMart</h1>
                    </div>
                    <div style="padding:30px;">
                      <h2 style="color:#333;margin-top:0;">Hi %s, reset your password</h2>
                      <p style="color:#555;">We received a request to reset your password. Click the button below — this link expires in 1 hour.</p>
                      <div style="text-align:center;margin:30px 0;">
                        <a href="%s"
                           style="background:#f5c518;color:#1a1a2e;padding:12px 28px;border-radius:6px;text-decoration:none;font-weight:bold;">
                          Reset Password
                        </a>
                      </div>
                      <p style="color:#999;font-size:13px;">If you didn't request this, you can safely ignore this email.</p>
                    </div>
                    <div style="background:#f4f4f4;padding:16px;text-align:center;">
                      <p style="color:#999;font-size:12px;margin:0;">© 2025 NovaMart</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(firstName, resetLink);
    }

    private String buildOrderHtml(String firstName, String orderReference,
                                   BigDecimal total, List<String> itemLines) {
        StringBuilder items = new StringBuilder();
        for (String line : itemLines) {
            items.append("<li style='padding:4px 0;'>").append(line).append("</li>");
        }
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px;">
                  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                    <div style="background:#1a1a2e;padding:30px;text-align:center;">
                      <h1 style="color:#f5c518;margin:0;font-size:24px;">NovaMart</h1>
                    </div>
                    <div style="padding:30px;">
                      <h2 style="color:#333;margin-top:0;">Hi %s, your order is confirmed!</h2>
                      <p style="color:#555;">Thank you for shopping with NovaMart. Here's a summary of your order:</p>
                      <div style="background:#f9f9f9;border-radius:6px;padding:16px;margin:20px 0;">
                        <p style="margin:0 0 8px;"><strong>Order:</strong> #%s</p>
                        <ul style="margin:10px 0;padding-left:20px;color:#555;">
                          %s
                        </ul>
                        <p style="margin:8px 0 0;font-size:18px;"><strong>Total: $%s</strong></p>
                      </div>
                      <p style="color:#555;">You can track your order status in the <strong>Orders</strong> section of your account.</p>
                      <div style="text-align:center;margin:30px 0;">
                        <a href="https://retail-novamart.netlify.app/orders"
                           style="background:#f5c518;color:#1a1a2e;padding:12px 28px;border-radius:6px;text-decoration:none;font-weight:bold;">
                          View My Orders
                        </a>
                      </div>
                    </div>
                    <div style="background:#f4f4f4;padding:16px;text-align:center;">
                      <p style="color:#999;font-size:12px;margin:0;">© 2025 NovaMart · You received this because you placed an order.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(firstName, orderReference, items, total.toPlainString());
    }
}
