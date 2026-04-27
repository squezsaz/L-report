package com.lreport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReportManager {
    private final Main plugin;
    private final DatabaseManager db;
    private final ConcurrentHashMap<UUID, Report> reports = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    public ReportManager(Main plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        loadReports();
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    public boolean hasPendingReport(String reporterName, String reportedName) {
        String key = reporterName.toLowerCase() + ":" + reportedName.toLowerCase();
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(reporterName) && 
                report.getReportedName().equalsIgnoreCase(reportedName) && 
                report.getStatus() == Report.ReportStatus.PENDING) {
                return true;
            }
        }
        return false;
    }

    public boolean canCreateReport(String reporterName) {
        int maxReports = plugin.getConfig().getInt("max-reports-per-day", 5);
        if (maxReports <= 0) return true;

        int count = db.getDailyCount(reporterName);
        return count < maxReports;
    }

    public int getRemainingReports(String reporterName) {
        int maxReports = plugin.getConfig().getInt("max-reports-per-day", 5);
        int count = db.getDailyCount(reporterName);
        return Math.max(0, maxReports - count);
    }

    public void createReport(String reporterName, String reportedName, String reason, String evidence) {
        Report report = new Report(reporterName, reportedName, reason, evidence);
        reports.put(report.getId(), report);
        cooldowns.put(reporterName.toLowerCase() + ":" + reportedName.toLowerCase(), System.currentTimeMillis());
        
        db.saveReport(report);
        db.incrementDailyCount(reporterName);
    }

    public List<Report> getAllReports() {
        return new ArrayList<>(reports.values());
    }

    public List<Report> getPendingReports() {
        List<Report> pending = new ArrayList<>();
        for (Report report : reports.values()) {
            if (report.getStatus() == Report.ReportStatus.PENDING) {
                pending.add(report);
            }
        }
        return pending;
    }

    public List<Report> getResolvedReports() {
        List<Report> resolved = new ArrayList<>();
        for (Report report : reports.values()) {
            if (report.getStatus() != Report.ReportStatus.PENDING) {
                resolved.add(report);
            }
        }
        return resolved;
    }

    public Report getReport(UUID id) {
        return reports.get(id);
    }

    public void acceptReport(UUID id, String adminName) {
        Report report = reports.get(id);
        if (report != null) {
            report.setStatus(Report.ReportStatus.ACCEPTED);
            report.setHandledBy(adminName);
            db.updateReportStatus(id, Report.ReportStatus.ACCEPTED, adminName);
        }
    }

    public void setRewardClaimed(UUID id, boolean claimed) {
        Report report = reports.get(id);
        if (report != null) {
            report.setRewardClaimed(claimed);
            db.updateRewardClaimed(id, claimed);
        }
    }

    public void rejectReport(UUID id, String adminName) {
        Report report = reports.get(id);
        if (report != null) {
            report.setStatus(Report.ReportStatus.REJECTED);
            report.setHandledBy(adminName);
            db.updateReportStatus(id, Report.ReportStatus.REJECTED, adminName);
        }
    }

    public void deleteReport(UUID id) {
        reports.remove(id);
        db.deleteReport(id);
    }

    public boolean hasCooldown(String reporterName, String reportedName) {
        Long lastReport = cooldowns.get(reporterName.toLowerCase() + ":" + reportedName.toLowerCase());
        if (lastReport == null) return false;
        
        int cooldownMinutes = plugin.getConfig().getInt("cooldown", 30);
        long cooldownMillis = cooldownMinutes * 60 * 1000L;
        return (System.currentTimeMillis() - lastReport) < cooldownMillis;
    }

    public long getCooldownRemaining(String reporterName, String reportedName) {
        Long lastReport = cooldowns.get(reporterName.toLowerCase() + ":" + reportedName.toLowerCase());
        if (lastReport == null) return 0;
        
        int cooldownMinutes = plugin.getConfig().getInt("cooldown", 30);
        long cooldownMillis = cooldownMinutes * 60 * 1000L;
        long remaining = cooldownMillis - (System.currentTimeMillis() - lastReport);
        return remaining > 0 ? remaining / 60000 : 0;
    }

    public boolean hasAlreadyReported(String reporterName, String reportedName) {
        return cooldowns.containsKey(reporterName.toLowerCase() + ":" + reportedName.toLowerCase());
    }

    private void loadReports() {
        List<Report> savedReports = db.getAllReports();
        for (Report report : savedReports) {
            reports.put(report.getId(), report);
            cooldowns.put((report.getReporterName() + ":" + report.getReportedName()).toLowerCase(), report.getTimestamp());
        }
    }

    public void shutdown() {
        db.close();
    }

    public int getTotalReportCount() {
        return reports.size();
    }

    public int getReportCountByStatus(String status) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getStatus().name().equalsIgnoreCase(status)) {
                count++;
            }
        }
        return count;
    }

    public int getReportCountByPlayer(String playerName) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(playerName)) {
                count++;
            }
        }
        return count;
    }

    public int getPendingReportCountByPlayer(String playerName) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(playerName) && 
                report.getStatus() == Report.ReportStatus.PENDING) {
                count++;
            }
        }
        return count;
    }

    public int getAcceptedReportCountByPlayer(String playerName) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(playerName) && 
                report.getStatus() == Report.ReportStatus.ACCEPTED) {
                count++;
            }
        }
        return count;
    }

    public int getRejectedReportCountByPlayer(String playerName) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(playerName) && 
                report.getStatus() == Report.ReportStatus.REJECTED) {
                count++;
            }
        }
        return count;
    }

    public int getTodayReportCount() {
        long todayStart = getTodayStartTimestamp();
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getTimestamp() >= todayStart) {
                count++;
            }
        }
        return count;
    }

    public int getTodayReportCountByPlayer(String playerName) {
        long todayStart = getTodayStartTimestamp();
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(playerName) && 
                report.getTimestamp() >= todayStart) {
                count++;
            }
        }
        return count;
    }

    public int getThisWeekReportCount() {
        long weekStart = getWeekStartTimestamp();
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getTimestamp() >= weekStart) {
                count++;
            }
        }
        return count;
    }

    public int getThisMonthReportCount() {
        long monthStart = getMonthStartTimestamp();
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getTimestamp() >= monthStart) {
                count++;
            }
        }
        return count;
    }

    public int getReportCountByReason(String reason) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReason().equalsIgnoreCase(reason)) {
                count++;
            }
        }
        return count;
    }

    public int getReportCountByReasonAndStatus(String reason, Report.ReportStatus status) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReason().equalsIgnoreCase(reason) && report.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public String getMostCommonReason() {
        Map<String, Integer> counts = new HashMap<>();
        for (Report report : reports.values()) {
            counts.merge(report.getReason(), 1, Integer::sum);
        }
        
        return counts.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    public int getReportsMadeByPlayer(String playerName) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(playerName)) {
                count++;
            }
        }
        return count;
    }

    public int getReportsAgainstPlayer(String playerName) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getReportedName().equalsIgnoreCase(playerName)) {
                count++;
            }
        }
        return count;
    }

    public int getReportsHandledByPlayer(String adminName) {
        int count = 0;
        for (Report report : reports.values()) {
            if (report.getHandledBy() != null && report.getHandledBy().equalsIgnoreCase(adminName)) {
                count++;
            }
        }
        return count;
    }

    public long getFirstReportTimestamp() {
        if (reports.isEmpty()) return 0;
        return reports.values().stream()
            .mapToLong(Report::getTimestamp)
            .min()
            .orElse(0);
    }

    public long getLastReportTimestamp() {
        if (reports.isEmpty()) return 0;
        return reports.values().stream()
            .mapToLong(Report::getTimestamp)
            .max()
            .orElse(0);
    }

    public long getPlayerFirstReportTimestamp(String playerName) {
        List<Report> playerReports = reports.values().stream()
            .filter(r -> r.getReporterName().equalsIgnoreCase(playerName))
            .collect(Collectors.toList());
        if (playerReports.isEmpty()) return 0;
        return playerReports.stream()
            .mapToLong(Report::getTimestamp)
            .min()
            .orElse(0);
    }

    public long getPlayerLastReportTimestamp(String playerName) {
        List<Report> playerReports = reports.values().stream()
            .filter(r -> r.getReporterName().equalsIgnoreCase(playerName))
            .collect(Collectors.toList());
        if (playerReports.isEmpty()) return 0;
        return playerReports.stream()
            .mapToLong(Report::getTimestamp)
            .max()
            .orElse(0);
    }

    public double getAcceptanceRate() {
        int total = reports.size();
        if (total == 0) return 0.0;
        int accepted = getReportCountByStatus("ACCEPTED");
        return (accepted * 100.0) / total;
    }

    public double getRejectionRate() {
        int total = reports.size();
        if (total == 0) return 0.0;
        int rejected = getReportCountByStatus("REJECTED");
        return (rejected * 100.0) / total;
    }

    public double getPlayerAcceptanceRate(String playerName) {
        int total = getReportsMadeByPlayer(playerName);
        if (total == 0) return 0.0;
        int accepted = getAcceptedReportCountByPlayer(playerName);
        return (accepted * 100.0) / total;
    }

    public boolean hasReportedPlayer(String reporterName, String reportedName) {
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(reporterName) && 
                report.getReportedName().equalsIgnoreCase(reportedName)) {
                return true;
            }
        }
        return false;
    }

    public Report getReportBetweenPlayers(String reporterName, String reportedName) {
        for (Report report : reports.values()) {
            if (report.getReporterName().equalsIgnoreCase(reporterName) && 
                report.getReportedName().equalsIgnoreCase(reportedName)) {
                return report;
            }
        }
        return null;
    }

    public List<Map.Entry<String, Integer>> getTopReporters(int limit) {
        Map<String, Integer> counts = new HashMap<>();
        for (Report report : reports.values()) {
            counts.merge(report.getReporterName(), 1, Integer::sum);
        }
        
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return sorted.stream().limit(limit).collect(Collectors.toList());
    }

    public List<Map.Entry<String, Integer>> getTopReportedPlayers(int limit) {
        Map<String, Integer> counts = new HashMap<>();
        for (Report report : reports.values()) {
            counts.merge(report.getReportedName(), 1, Integer::sum);
        }
        
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return sorted.stream().limit(limit).collect(Collectors.toList());
    }

    public List<Map.Entry<String, Integer>> getTopHandlers(int limit) {
        Map<String, Integer> counts = new HashMap<>();
        for (Report report : reports.values()) {
            if (report.getHandledBy() != null && !report.getHandledBy().isEmpty()) {
                counts.merge(report.getHandledBy(), 1, Integer::sum);
            }
        }
        
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return sorted.stream().limit(limit).collect(Collectors.toList());
    }

    public String getTopReporter() {
        Map<String, Integer> counts = new HashMap<>();
        for (Report report : reports.values()) {
            counts.merge(report.getReporterName(), 1, Integer::sum);
        }
        
        return counts.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    public String getTopHandler() {
        Map<String, Integer> counts = new HashMap<>();
        for (Report report : reports.values()) {
            if (report.getHandledBy() != null && !report.getHandledBy().isEmpty()) {
                counts.merge(report.getHandledBy(), 1, Integer::sum);
            }
        }
        
        return counts.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    public String getMostReportedPlayer() {
        Map<String, Integer> counts = new HashMap<>();
        for (Report report : reports.values()) {
            counts.merge(report.getReportedName(), 1, Integer::sum);
        }
        
        return counts.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    public String getPlayerLatestReportStatus(String playerName) {
        List<Report> playerReports = reports.values().stream()
            .filter(r -> r.getReporterName().equalsIgnoreCase(playerName))
            .sorted(Comparator.comparingLong(Report::getTimestamp).reversed())
            .collect(Collectors.toList());

        if (!playerReports.isEmpty()) {
            return playerReports.get(0).getStatus().name();
        }
        return null;
    }

    public long getReportCooldownRemaining(String reporterName) {
        int cooldownMinutes = plugin.getConfig().getInt("cooldown", 30);
        if (cooldownMinutes <= 0) return 0;

        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(reporterName.toLowerCase() + ":")) {
                long diff = System.currentTimeMillis() - entry.getValue();
                long remaining = (cooldownMinutes * 60 * 1000) - diff;
                return remaining > 0 ? remaining / 60000 : 0;
            }
        }
        return 0;
    }

    private long getTodayStartTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getWeekStartTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getMonthStartTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}