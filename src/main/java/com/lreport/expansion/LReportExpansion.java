package com.lreport;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LReportExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public LReportExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lreport";
    }

    @Override
    public @NotNull String getAuthor() {
        return "L-Report";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier == null) return "";

        ReportManager rm = plugin.getReportManager();
        DatabaseManager db = plugin.getDatabaseManager();

        switch (identifier.toLowerCase()) {
            case "total_reports":
                return String.valueOf(rm.getTotalReportCount());
            case "pending_reports":
                return String.valueOf(rm.getReportCountByStatus("PENDING"));
            case "accepted_reports":
                return String.valueOf(rm.getReportCountByStatus("ACCEPTED"));
            case "rejected_reports":
                return String.valueOf(rm.getReportCountByStatus("REJECTED"));
            case "today_reports":
                return String.valueOf(rm.getTodayReportCount());
            case "week_reports":
                return String.valueOf(rm.getThisWeekReportCount());
            case "month_reports":
                return String.valueOf(rm.getThisMonthReportCount());
            case "remaining_reports":
                if (player != null) {
                    int max = plugin.getConfig().getInt("max-reports-per-day", 5);
                    int used = rm.getTodayReportCountByPlayer(player.getName());
                    return String.valueOf(Math.max(0, max - used));
                }
                return "0";

            case "my_reports":
                return player != null ? String.valueOf(rm.getReportsMadeByPlayer(player.getName())) : "0";
            case "my_pending_reports":
                return player != null ? String.valueOf(rm.getPendingReportCountByPlayer(player.getName())) : "0";
            case "my_accepted_reports":
                return player != null ? String.valueOf(rm.getAcceptedReportCountByPlayer(player.getName())) : "0";
            case "my_rejected_reports":
                return player != null ? String.valueOf(rm.getRejectedReportCountByPlayer(player.getName())) : "0";
            case "my_today_reports":
                return player != null ? String.valueOf(rm.getTodayReportCountByPlayer(player.getName())) : "0";
            case "my_remaining_reports":
                return player != null ? String.valueOf(rm.getRemainingReports(player.getName())) : "0";
            case "my_status":
                return player != null ? (rm.getPlayerLatestReportStatus(player.getName()) != null ? rm.getPlayerLatestReportStatus(player.getName()) : "N/A") : "N/A";
            case "my_can_report":
                return player != null ? (rm.canCreateReport(player.getName()) ? "true" : "false") : "false";

            case "status":
                return player != null ? (rm.getPlayerLatestReportStatus(player.getName()) != null ? rm.getPlayerLatestReportStatus(player.getName()) : "N/A") : "N/A";

            case "top_reporter":
                return rm.getTopReporter();
            case "top_reporter_count":
                List<Map.Entry<String, Integer>> topReporters = rm.getTopReporters(1);
                return topReporters.isEmpty() ? "0" : String.valueOf(topReporters.get(0).getValue());
            case "top_reporter_2":
                List<Map.Entry<String, Integer>> topReporters2 = rm.getTopReporters(2);
                return topReporters2.size() > 1 ? topReporters2.get(1).getKey() : "N/A";
            case "top_reporter_2_count":
                List<Map.Entry<String, Integer>> topReporters2c = rm.getTopReporters(2);
                return topReporters2c.size() > 1 ? String.valueOf(topReporters2c.get(1).getValue()) : "0";
            case "top_reporter_3":
                List<Map.Entry<String, Integer>> topReporters3 = rm.getTopReporters(3);
                return topReporters3.size() > 2 ? topReporters3.get(2).getKey() : "N/A";
            case "top_reporter_3_count":
                List<Map.Entry<String, Integer>> topReporters3c = rm.getTopReporters(3);
                return topReporters3c.size() > 2 ? String.valueOf(topReporters3c.get(2).getValue()) : "0";

            case "most_reported":
                return rm.getMostReportedPlayer();
            case "most_reported_count":
                List<Map.Entry<String, Integer>> topReported = rm.getTopReportedPlayers(1);
                return topReported.isEmpty() ? "0" : String.valueOf(topReported.get(0).getValue());
            case "most_reported_2":
                List<Map.Entry<String, Integer>> topReported2 = rm.getTopReportedPlayers(2);
                return topReported2.size() > 1 ? topReported2.get(1).getKey() : "N/A";
            case "most_reported_2_count":
                List<Map.Entry<String, Integer>> topReported2c = rm.getTopReportedPlayers(2);
                return topReported2c.size() > 1 ? String.valueOf(topReported2c.get(1).getValue()) : "0";
            case "most_reported_3":
                List<Map.Entry<String, Integer>> topReported3 = rm.getTopReportedPlayers(3);
                return topReported3.size() > 2 ? topReported3.get(2).getKey() : "N/A";
            case "most_reported_3_count":
                List<Map.Entry<String, Integer>> topReported3c = rm.getTopReportedPlayers(3);
                return topReported3c.size() > 2 ? String.valueOf(topReported3c.get(2).getValue()) : "0";

            case "top_handler":
                return rm.getTopHandler();
            case "top_handler_count":
                List<Map.Entry<String, Integer>> topHandlers = rm.getTopHandlers(1);
                return topHandlers.isEmpty() ? "0" : String.valueOf(topHandlers.get(0).getValue());
            case "top_handler_2":
                List<Map.Entry<String, Integer>> topHandlers2 = rm.getTopHandlers(2);
                return topHandlers2.size() > 1 ? topHandlers2.get(1).getKey() : "N/A";
            case "top_handler_2_count":
                List<Map.Entry<String, Integer>> topHandlers2c = rm.getTopHandlers(2);
                return topHandlers2c.size() > 1 ? String.valueOf(topHandlers2c.get(1).getValue()) : "0";
            case "top_handler_3":
                List<Map.Entry<String, Integer>> topHandlers3 = rm.getTopHandlers(3);
                return topHandlers3.size() > 2 ? topHandlers3.get(2).getKey() : "N/A";
            case "top_handler_3_count":
                List<Map.Entry<String, Integer>> topHandlers3c = rm.getTopHandlers(3);
                return topHandlers3c.size() > 2 ? String.valueOf(topHandlers3c.get(2).getValue()) : "0";

            case "acceptance_rate":
                return String.format("%.1f", rm.getAcceptanceRate());
            case "rejection_rate":
                return String.format("%.1f", rm.getRejectionRate());
            case "pending_rate":
                double pendingRate = 100.0 - rm.getAcceptanceRate() - rm.getRejectionRate();
                return String.format("%.1f", pendingRate);
            case "my_acceptance_rate":
                return player != null ? String.format("%.1f", rm.getPlayerAcceptanceRate(player.getName())) : "0";

            case "most_common_reason":
                return rm.getMostCommonReason();

            case "hack_reports":
                return String.valueOf(rm.getReportCountByReason("Hack"));
            case "hack_pending":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Hack", Report.ReportStatus.PENDING));
            case "hack_accepted":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Hack", Report.ReportStatus.ACCEPTED));
            case "hack_rejected":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Hack", Report.ReportStatus.REJECTED));

            case "toxic_reports":
                return String.valueOf(rm.getReportCountByReason("Toxic"));
            case "toxic_pending":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Toxic", Report.ReportStatus.PENDING));
            case "toxic_accepted":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Toxic", Report.ReportStatus.ACCEPTED));
            case "toxic_rejected":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Toxic", Report.ReportStatus.REJECTED));

            case "spam_reports":
                return String.valueOf(rm.getReportCountByReason("Spam"));
            case "spam_pending":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Spam", Report.ReportStatus.PENDING));
            case "spam_accepted":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Spam", Report.ReportStatus.ACCEPTED));
            case "spam_rejected":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Spam", Report.ReportStatus.REJECTED));

            case "ad_reports":
                return String.valueOf(rm.getReportCountByReason("Ad"));
            case "ad_pending":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Ad", Report.ReportStatus.PENDING));
            case "ad_accepted":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Ad", Report.ReportStatus.ACCEPTED));
            case "ad_rejected":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Ad", Report.ReportStatus.REJECTED));

            case "insult_reports":
                return String.valueOf(rm.getReportCountByReason("Insult"));
            case "insult_pending":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Insult", Report.ReportStatus.PENDING));
            case "insult_accepted":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Insult", Report.ReportStatus.ACCEPTED));
            case "insult_rejected":
                return String.valueOf(rm.getReportCountByReasonAndStatus("Insult", Report.ReportStatus.REJECTED));

            case "first_report_date":
                long firstTs = rm.getFirstReportTimestamp();
                return firstTs > 0 ? new SimpleDateFormat("dd/MM/yyyy").format(new Date(firstTs)) : "N/A";
            case "first_report_time":
                long firstTs2 = rm.getFirstReportTimestamp();
                return firstTs2 > 0 ? new SimpleDateFormat("HH:mm").format(new Date(firstTs2)) : "N/A";
            case "last_report_date":
                long lastTs = rm.getLastReportTimestamp();
                return lastTs > 0 ? new SimpleDateFormat("dd/MM/yyyy").format(new Date(lastTs)) : "N/A";
            case "last_report_time":
                long lastTs2 = rm.getLastReportTimestamp();
                return lastTs2 > 0 ? new SimpleDateFormat("HH:mm").format(new Date(lastTs2)) : "N/A";
            case "last_report_datetime":
                long lastTs3 = rm.getLastReportTimestamp();
                return lastTs3 > 0 ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(lastTs3)) : "N/A";

            case "my_first_report_date":
                if (player != null) {
                    long myFirstTs = rm.getPlayerFirstReportTimestamp(player.getName());
                    return myFirstTs > 0 ? new SimpleDateFormat("dd/MM/yyyy").format(new Date(myFirstTs)) : "N/A";
                }
                return "N/A";
            case "my_first_report_time":
                if (player != null) {
                    long myFirstTs2 = rm.getPlayerFirstReportTimestamp(player.getName());
                    return myFirstTs2 > 0 ? new SimpleDateFormat("HH:mm").format(new Date(myFirstTs2)) : "N/A";
                }
                return "N/A";
            case "my_last_report_date":
                if (player != null) {
                    long myLastTs = rm.getPlayerLastReportTimestamp(player.getName());
                    return myLastTs > 0 ? new SimpleDateFormat("dd/MM/yyyy").format(new Date(myLastTs)) : "N/A";
                }
                return "N/A";
            case "my_last_report_time":
                if (player != null) {
                    long myLastTs2 = rm.getPlayerLastReportTimestamp(player.getName());
                    return myLastTs2 > 0 ? new SimpleDateFormat("HH:mm").format(new Date(myLastTs2)) : "N/A";
                }
                return "N/A";

            case "cooldown":
                return String.valueOf(plugin.getConfig().getInt("cooldown", 30));
            case "my_cooldown":
                if (player != null) {
                    return rm.getCooldowns().containsKey(player.getName().toLowerCase() + ":") ? 
                        String.valueOf(rm.getReportCooldownRemaining(player.getName())) : "0";
                }
                return "0";

            case "is_frozen":
                if (player != null) {
                    return plugin.isFrozen(player) ? "true" : "false";
                }
                return "false";
            case "frozen_count":
                return String.valueOf(plugin.getFrozenPlayers().size());

            case "db_total":
                return String.valueOf(db.getTotalReportCount());
            case "db_pending":
                return String.valueOf(db.getPendingReportCount());
            case "db_accepted":
                return String.valueOf(db.getAcceptedReportCount());
            case "db_rejected":
                return String.valueOf(db.getRejectedReportCount());
            case "db_today":
                return String.valueOf(db.getTodayReportCount());

            case "player_reports_made":
                if (player != null) {
                    return String.valueOf(rm.getReportsMadeByPlayer(player.getName()));
                }
                return "0";
            case "player_reports_received":
                if (player != null) {
                    return String.valueOf(rm.getReportsAgainstPlayer(player.getName()));
                }
                return "0";
            case "player_reports_handled":
                if (player != null) {
                    return String.valueOf(rm.getReportsHandledByPlayer(player.getName()));
                }
                return "0";

            case "plugin_version":
                return plugin.getDescription().getVersion();
            case "plugin_enabled":
                return plugin.isEnabled() ? "true" : "false";

            case "online_players":
                return String.valueOf(Bukkit.getOnlinePlayers().size());

            case "has_pending_target":
                if (player != null) {
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (!target.getName().equals(player.getName()) && rm.hasPendingReport(player.getName(), target.getName())) {
                            return "true";
                        }
                    }
                }
                return "false";

            default:
                return null;
        }
    }
}