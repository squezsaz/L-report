package com.lreport;

import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WebhookManager {
    private final Main plugin;
    private final LanguageManager lang;
    private boolean enabled;
    private String webhookUrl;
    private String webhookUsername;
    private int webhookColor;
    private boolean sendNewReport;
    private boolean sendReportAccepted;
    private boolean sendReportRejected;
    private boolean sendReportDeleted;

    private static final int COLOR_NEW_REPORT = 15105570;
    private static final int COLOR_ACCEPTED = 5767199;
    private static final int COLOR_REJECTED = 15548984;
    private static final int COLOR_DELETED = 16440904;

    public WebhookManager(Main plugin, LanguageManager lang) {
        this.plugin = plugin;
        this.lang = lang;
        loadSettings();
    }

    public void loadSettings() {
        this.enabled = plugin.getConfig().getBoolean("webhook-enabled", false);
        this.webhookUrl = plugin.getConfig().getString("webhook-url", "");
        this.webhookUsername = plugin.getConfig().getString("webhook-username", "L-Report Bot");
        this.webhookColor = plugin.getConfig().getInt("webhook-color", 15105570);
        this.sendNewReport = plugin.getConfig().getBoolean("webhook-new-report", true);
        this.sendReportAccepted = plugin.getConfig().getBoolean("webhook-report-accepted", true);
        this.sendReportRejected = plugin.getConfig().getBoolean("webhook-report-rejected", true);
        this.sendReportDeleted = plugin.getConfig().getBoolean("webhook-report-deleted", false);
    }

    public boolean isEnabled() {
        return enabled && webhookUrl != null && !webhookUrl.isEmpty();
    }

    public void sendNewReportMessage(String reporterName, String reportedName, String reason, String evidence) {
        if (!isEnabled() || !sendNewReport) return;

        List<Map<String, String>> fields = new ArrayList<>();
        
        Map<String, String> reporterField = new HashMap<>();
        reporterField.put("name", lang.get("webhook.field_reporter"));
        reporterField.put("value", "**" + reporterName + "**");
        reporterField.put("inline", "true");
        fields.add(reporterField);
        
        Map<String, String> reportedField = new HashMap<>();
        reportedField.put("name", lang.get("webhook.field_reported"));
        reportedField.put("value", "**" + reportedName + "**");
        reportedField.put("inline", "true");
        fields.add(reportedField);
        
        Map<String, String> reasonField = new HashMap<>();
        reasonField.put("name", lang.get("webhook.field_reason"));
        reasonField.put("value", getReasonDisplay(reason));
        reasonField.put("inline", "true");
        fields.add(reasonField);
        
        Map<String, String> evidenceField = new HashMap<>();
        evidenceField.put("name", lang.get("webhook.field_evidence"));
        String evidenceValue = evidence != null && !evidence.isEmpty() ? evidence : lang.get("webhook.no_evidence");
        evidenceField.put("value", evidenceValue.length() > 1024 ? evidenceValue.substring(0, 1021) + "..." : evidenceValue);
        evidenceField.put("inline", "false");
        fields.add(evidenceField);

        sendEmbed(
            lang.get("webhook.new_report_title"),
            lang.get("webhook.new_report_desc"),
            COLOR_NEW_REPORT,
            fields,
            ":inbox_tray:",
            null
        );
    }

    public void sendReportAcceptedMessage(String reporterName, String reportedName, String reason, String handlerName) {
        if (!isEnabled() || !sendReportAccepted) return;

        List<Map<String, String>> fields = new ArrayList<>();
        
        Map<String, String> reporterField = new HashMap<>();
        reporterField.put("name", lang.get("webhook.field_reporter"));
        reporterField.put("value", "**" + reporterName + "**");
        reporterField.put("inline", "true");
        fields.add(reporterField);
        
        Map<String, String> reportedField = new HashMap<>();
        reportedField.put("name", lang.get("webhook.field_reported"));
        reportedField.put("value", "**" + reportedName + "**");
        reportedField.put("inline", "true");
        fields.add(reportedField);
        
        Map<String, String> reasonField = new HashMap<>();
        reasonField.put("name", lang.get("webhook.field_reason"));
        reasonField.put("value", getReasonDisplay(reason));
        reasonField.put("inline", "true");
        fields.add(reasonField);
        
        Map<String, String> handlerField = new HashMap<>();
        handlerField.put("name", lang.get("webhook.field_handler"));
        handlerField.put("value", "**" + handlerName + "**");
        handlerField.put("inline", "true");
        fields.add(handlerField);

        sendEmbed(
            lang.get("webhook.report_accepted_title"),
            lang.get("webhook.report_accepted_desc"),
            COLOR_ACCEPTED,
            fields,
            ":white_check_mark:",
            null
        );
    }

    public void sendReportRejectedMessage(String reporterName, String reportedName, String reason, String handlerName) {
        if (!isEnabled() || !sendReportRejected) return;

        List<Map<String, String>> fields = new ArrayList<>();
        
        Map<String, String> reporterField = new HashMap<>();
        reporterField.put("name", lang.get("webhook.field_reporter"));
        reporterField.put("value", "**" + reporterName + "**");
        reporterField.put("inline", "true");
        fields.add(reporterField);
        
        Map<String, String> reportedField = new HashMap<>();
        reportedField.put("name", lang.get("webhook.field_reported"));
        reportedField.put("value", "**" + reportedName + "**");
        reportedField.put("inline", "true");
        fields.add(reportedField);
        
        Map<String, String> reasonField = new HashMap<>();
        reasonField.put("name", lang.get("webhook.field_reason"));
        reasonField.put("value", getReasonDisplay(reason));
        reasonField.put("inline", "true");
        fields.add(reasonField);
        
        Map<String, String> handlerField = new HashMap<>();
        handlerField.put("name", lang.get("webhook.field_handler"));
        handlerField.put("value", "**" + handlerName + "**");
        handlerField.put("inline", "true");
        fields.add(handlerField);

        sendEmbed(
            lang.get("webhook.report_rejected_title"),
            lang.get("webhook.report_rejected_desc"),
            COLOR_REJECTED,
            fields,
            ":x:",
            null
        );
    }

    public void sendReportDeletedMessage(String reporterName, String reportedName, String reason, String handlerName) {
        if (!isEnabled() || !sendReportDeleted) return;

        List<Map<String, String>> fields = new ArrayList<>();
        
        Map<String, String> reporterField = new HashMap<>();
        reporterField.put("name", lang.get("webhook.field_reporter"));
        reporterField.put("value", "**" + reporterName + "**");
        reporterField.put("inline", "true");
        fields.add(reporterField);
        
        Map<String, String> reportedField = new HashMap<>();
        reportedField.put("name", lang.get("webhook.field_reported"));
        reportedField.put("value", "**" + reportedName + "**");
        reportedField.put("inline", "true");
        fields.add(reportedField);
        
        Map<String, String> reasonField = new HashMap<>();
        reasonField.put("name", lang.get("webhook.field_reason"));
        reasonField.put("value", getReasonDisplay(reason));
        reasonField.put("inline", "true");
        fields.add(reasonField);
        
        Map<String, String> handlerField = new HashMap<>();
        handlerField.put("name", lang.get("webhook.field_handler"));
        handlerField.put("value", "**" + handlerName + "**");
        handlerField.put("inline", "true");
        fields.add(handlerField);

        sendEmbed(
            lang.get("webhook.report_deleted_title"),
            lang.get("webhook.report_deleted_desc"),
            COLOR_DELETED,
            fields,
            ":wastebasket:",
            null
        );
    }

    private String getReasonDisplay(String reason) {
        if (reason == null || reason.isEmpty()) {
            return lang.get("webhook.no_reason");
        }
        switch (reason.toLowerCase()) {
            case "hack": return ":crossed_swords: Hack";
            case "toxic": return ":angry: Toxic";
            case "spam": return ":loud_sound: Spam";
            case "ad": return ":mega: Advertising";
            case "insult": return ":face_with_symbols_on_mouth: Insult";
            default: return ":grey_question: " + reason;
        }
    }

    private void sendEmbed(String title, String description, int color, List<Map<String, String>> fields, String emoji, String thumbnailUrl) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "L-Report/1.0");
                connection.setDoOutput(true);

                String jsonPayload = buildEmbedJson(title, description, color, fields, emoji, thumbnailUrl);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != 200 && responseCode != 204) {
                    plugin.getLogger().warning("Discord webhook returned code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    private String buildEmbedJson(String title, String description, int color, List<Map<String, String>> fields, String emoji, String thumbnailUrl) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"username\": \"").append(escapeJson(webhookUsername)).append("\",");
        json.append("\"embeds\": [");
        json.append("{");
        json.append("\"title\": \"").append(escapeJson(emoji + " " + title)).append("\",");
        json.append("\"description\": \"").append(escapeJson(description)).append("\",");
        json.append("\"color\": ").append(color).append(",");
        
        if (thumbnailUrl != null) {
            json.append("\"thumbnail\": {\"url\": \"").append(escapeJson(thumbnailUrl)).append("\"},");
        }
        
        json.append("\"fields\": [");
        for (int i = 0; i < fields.size(); i++) {
            Map<String, String> field = fields.get(i);
            json.append("{");
            json.append("\"name\": \"").append(escapeJson(field.get("name"))).append("\",");
            json.append("\"value\": \"").append(escapeJson(field.get("value"))).append("\",");
            json.append("\"inline\": ").append(field.get("inline")).append("");
            json.append("}");
            if (i < fields.size() - 1) json.append(",");
        }
        json.append("],");
        
        json.append("\"footer\": {");
        json.append("\"text\": \"L-Report Plugin | ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append("\"");
        json.append("},");
        json.append("\"timestamp\": \"").append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date())).append("\"");
        json.append("}");
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}