package com.lastmile.delivery.service;

import com.lastmile.delivery.entity.Order;
import com.lastmile.delivery.entity.OrderStatus;
import com.lastmile.delivery.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUser;

    @Value("${app.email-from:LastMile Delivery <noreply@lastmile.com>}")
    private String emailFrom;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.fast2sms.api-key:}")
    private String fast2smsApiKey;

    private static final Map<OrderStatus, String> STATUS_LABELS = new HashMap<>();

    static {
        STATUS_LABELS.put(OrderStatus.CREATED, "Order Created");
        STATUS_LABELS.put(OrderStatus.AGENT_ASSIGNED, "Agent Assigned");
        STATUS_LABELS.put(OrderStatus.PICKED_UP, "Package Picked Up");
        STATUS_LABELS.put(OrderStatus.IN_TRANSIT, "In Transit");
        STATUS_LABELS.put(OrderStatus.OUT_FOR_DELIVERY, "Out for Delivery");
        STATUS_LABELS.put(OrderStatus.DELIVERED, "Delivered 🎉");
        STATUS_LABELS.put(OrderStatus.FAILED, "Delivery Failed");
        STATUS_LABELS.put(OrderStatus.RESCHEDULED, "Rescheduled");
        STATUS_LABELS.put(OrderStatus.CANCELLED, "Cancelled");
    }

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void notifyCustomer(Order order, OrderStatus status, String note) {
        User customer = order.getCustomer();
        if (customer == null) return;

        String statusLabel = STATUS_LABELS.getOrDefault(status, status.name());
        String trackingUrl = frontendUrl + "/track/" + order.getId();
        String noteText = (note != null && !note.isEmpty()) ? note : "";

        // 1. Send Email Notification
        sendEmailNotification(customer.getEmail(), customer.getName(), order.getId(), statusLabel, status, noteText, trackingUrl);

        // 2. Send SMS Notification
        if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
            String smsMessage = String.format("LastMile Delivery: Order #%s - %s. %s",
                    order.getId().substring(Math.max(0, order.getId().length() - 8)).toUpperCase(),
                    statusLabel,
                    noteText);
            sendSMSNotification(customer.getPhone(), smsMessage);
        }
    }

    private void sendEmailNotification(String to, String name, String orderId, String statusLabel, OrderStatus status, String note, String trackingUrl) {
        if (mailSender == null || mailUser == null || mailUser.isEmpty()) {
            logger.info("[EMAIL SKIP] Email not configured. Would send: {} to {}", statusLabel, to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(String.format("Order Update: %s — #%s", statusLabel, orderId.substring(Math.max(0, orderId.length() - 8)).toUpperCase()));
            
            String htmlTemplate = buildEmailTemplate(name, orderId, statusLabel, status, note, trackingUrl);
            helper.setText(htmlTemplate, true);

            mailSender.send(message);
            logger.info("[EMAIL] Sent status={} to {}", status, to);
        } catch (Exception e) {
            logger.error("[EMAIL ERROR] Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private void sendSMSNotification(String phone, String message) {
        if (fast2smsApiKey == null || fast2smsApiKey.isEmpty()) {
            logger.info("[SMS SKIP] Fast2SMS not configured or no phone. Would send to {}: {}", phone, message);
            return;
        }

        try {
            String url = "https://www.fast2sms.com/dev/bulkV2";
            
            Map<String, Object> request = new HashMap<>();
            request.put("route", "q");
            request.put("message", message);
            request.put("language", "english");
            request.put("flash", 0);
            request.put("numbers", phone);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("authorization", fast2smsApiKey);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(request, headers);
            
            restTemplate.postForObject(url, entity, String.class);
            logger.info("[SMS] Sent to {}", phone);
        } catch (Exception e) {
            logger.error("[SMS ERROR] Failed to send SMS to {}: {}", phone, e.getMessage());
        }
    }

    private String buildEmailTemplate(String customerName, String orderId, String statusLabel, OrderStatus status, String note, String trackingUrl) {
        String displayOrderId = orderId.substring(Math.max(0, orderId.length() - 8)).toUpperCase();
        String failedSection = (status == OrderStatus.FAILED) ? 
            "<div style=\"background: #fef2f2; border-radius: 8px; padding: 16px; margin: 16px 0; border-left: 4px solid #ef4444;\">" +
            "  <p style=\"color: #991b1b; margin: 0; font-weight: 600;\">Delivery attempt failed.</p>" +
            "  <p style=\"color: #dc2626; margin: 8px 0 0 0;\">Please visit your order page to reschedule a new delivery date.</p>" +
            "</div>" : "";

        String noteSection = (!note.isEmpty()) ? String.format("<p style=\"color: #64748b; margin: 8px 0 0 0;\">%s</p>", note) : "";

        return String.format(
            "<div style=\"font-family: 'Inter', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8fafc; padding: 20px;\">" +
            "  <div style=\"background: linear-gradient(135deg, #2563eb 0%, #0891b2 100%); border-radius: 12px; padding: 30px; text-align: center; margin-bottom: 20px;\">" +
            "    <h1 style=\"color: white; margin: 0; font-size: 24px;\">LastMile Delivery</h1>" +
            "    <p style=\"color: rgba(255,255,255,0.8); margin: 5px 0 0 0;\">Order Status Update</p>" +
            "  </div>" +
            "  " +
            "  <div style=\"background: white; border-radius: 12px; padding: 30px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);\">" +
            "    <h2 style=\"color: #1e293b; margin-top: 0;\">Hi %s,</h2>" +
            "    <p style=\"color: #475569; font-size: 16px;\">Your order <strong>#%s</strong> status has been updated:</p>" +
            "    " +
            "    <div style=\"background: #f1f5f9; border-radius: 8px; padding: 20px; margin: 20px 0; border-left: 4px solid #2563eb;\">" +
            "      <h3 style=\"margin: 0; color: #1e293b; font-size: 20px;\">%s</h3>" +
            "      %s" +
            "    </div>" +
            "    " +
            "    %s" +
            "    " +
            "    <a href=\"%s\" style=\"display: inline-block; background: linear-gradient(135deg, #2563eb 0%, #0891b2 100%); color: white; text-decoration: none; padding: 12px 28px; border-radius: 8px; font-weight: 600; margin-top: 10px;\">" +
            "      Track Your Order →" +
            "    </a>" +
            "  </div>" +
            "  " +
            "  <p style=\"color: #94a3b8; text-align: center; font-size: 12px; margin: 0;\">" +
            "    This is an automated notification from LastMile Delivery Tracker." +
            "  </p>" +
            "</div>",
            customerName, displayOrderId, statusLabel, noteSection, failedSection, trackingUrl
        );
    }
}
