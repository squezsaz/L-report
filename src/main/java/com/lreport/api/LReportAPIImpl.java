package com.lreport.api;

import com.lreport.Main;
import com.lreport.Report;
import com.lreport.Report.ReportStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class LReportAPIImpl implements LReportAPI {

    private final Main plugin;

    public LReportAPIImpl(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public Report createReport(String reporterName, String reportedName, String reason, String evidence) {
        ReportCreateEvent event = new ReportCreateEvent(reporterName, reportedName, reason, evidence);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;

        Report report = new Report(reporterName, reportedName, reason, evidence);
        plugin.getReportManager().createReport(reporterName, reportedName, reason, evidence);

        Report created = plugin.getReportManager().getReport(report.getId());
        if (created != null) {
            Bukkit.getPluginManager().callEvent(new ReportStatusChangeEvent(
                created, ReportStatus.PENDING, ReportStatus.PENDING
            ));
        }
        return created;
    }

    @Override
    public Report getReport(UUID id) {
        return plugin.getReportManager().getReport(id);
    }

    @Override
    public List<Report> getAllReports() {
        return plugin.getReportManager().getAllReports();
    }

    @Override
    public List<Report> getPendingReports() {
        return plugin.getReportManager().getPendingReports();
    }

    @Override
    public List<Report> getResolvedReports() {
        return plugin.getReportManager().getResolvedReports();
    }

    @Override
    public List<Report> getReportsByReporter(String playerName) {
        return plugin.getReportManager().getAllReports().stream()
            .filter(r -> r.getReporterName().equalsIgnoreCase(playerName))
            .collect(Collectors.toList());
    }

    @Override
    public List<Report> getReportsByReported(String playerName) {
        return plugin.getReportManager().getAllReports().stream()
            .filter(r -> r.getReportedName().equalsIgnoreCase(playerName))
            .collect(Collectors.toList());
    }

    @Override
    public boolean acceptReport(UUID id, String adminName) {
        Report report = plugin.getReportManager().getReport(id);
        if (report == null || report.getStatus() != ReportStatus.PENDING) return false;

        ReportStatus oldStatus = report.getStatus();
        plugin.getReportManager().acceptReport(id, adminName);

        Bukkit.getPluginManager().callEvent(new ReportStatusChangeEvent(
            plugin.getReportManager().getReport(id), oldStatus, ReportStatus.ACCEPTED
        ));
        return true;
    }

    @Override
    public boolean rejectReport(UUID id, String adminName) {
        Report report = plugin.getReportManager().getReport(id);
        if (report == null || report.getStatus() != ReportStatus.PENDING) return false;

        ReportStatus oldStatus = report.getStatus();
        plugin.getReportManager().rejectReport(id, adminName);

        Bukkit.getPluginManager().callEvent(new ReportStatusChangeEvent(
            plugin.getReportManager().getReport(id), oldStatus, ReportStatus.REJECTED
        ));
        return true;
    }

    @Override
    public boolean deleteReport(UUID id) {
        Report report = plugin.getReportManager().getReport(id);
        if (report == null) return false;
        plugin.getReportManager().deleteReport(id);
        return true;
    }

    @Override
    public boolean hasPendingReport(String reporterName, String reportedName) {
        return plugin.getReportManager().hasPendingReport(reporterName, reportedName);
    }

    @Override
    public boolean canCreateReport(String reporterName) {
        return plugin.getReportManager().canCreateReport(reporterName);
    }

    @Override
    public int getRemainingReports(String reporterName) {
        return plugin.getReportManager().getRemainingReports(reporterName);
    }

    @Override
    public boolean hasCooldown(String reporterName, String reportedName) {
        return plugin.getReportManager().hasCooldown(reporterName, reportedName);
    }

    @Override
    public long getCooldownRemaining(String reporterName, String reportedName) {
        return plugin.getReportManager().getCooldownRemaining(reporterName, reportedName);
    }

    @Override
    public int getTotalReportCount() {
        return plugin.getReportManager().getTotalReportCount();
    }

    @Override
    public int getPendingReportCount() {
        return plugin.getReportManager().getReportCountByStatus("PENDING");
    }

    @Override
    public int getAcceptedReportCount() {
        return plugin.getReportManager().getReportCountByStatus("ACCEPTED");
    }

    @Override
    public int getRejectedReportCount() {
        return plugin.getReportManager().getReportCountByStatus("REJECTED");
    }

    @Override
    public int getTodayReportCount() {
        return plugin.getReportManager().getTodayReportCount();
    }

    @Override
    public int getTodayReportCountByPlayer(String playerName) {
        return plugin.getReportManager().getTodayReportCountByPlayer(playerName);
    }

    @Override
    public int getThisWeekReportCount() {
        return plugin.getReportManager().getThisWeekReportCount();
    }

    @Override
    public int getThisMonthReportCount() {
        return plugin.getReportManager().getThisMonthReportCount();
    }

    @Override
    public int getReportCountByPlayer(String playerName) {
        return plugin.getReportManager().getReportCountByPlayer(playerName);
    }

    @Override
    public int getReportsMadeByPlayer(String playerName) {
        return plugin.getReportManager().getReportsMadeByPlayer(playerName);
    }

    @Override
    public int getReportsAgainstPlayer(String playerName) {
        return plugin.getReportManager().getReportsAgainstPlayer(playerName);
    }

    @Override
    public String getMostCommonReason() {
        return plugin.getReportManager().getMostCommonReason();
    }

    @Override
    public double getAcceptanceRate() {
        return plugin.getReportManager().getAcceptanceRate();
    }

    @Override
    public double getRejectionRate() {
        return plugin.getReportManager().getRejectionRate();
    }

    @Override
    public String getTopReporter() {
        return plugin.getReportManager().getTopReporter();
    }

    @Override
    public String getTopHandler() {
        return plugin.getReportManager().getTopHandler();
    }

    @Override
    public String getMostReportedPlayer() {
        return plugin.getReportManager().getMostReportedPlayer();
    }

    @Override
    public List<Map.Entry<String, Integer>> getTopReporters(int limit) {
        return plugin.getReportManager().getTopReporters(limit);
    }

    @Override
    public List<Map.Entry<String, Integer>> getTopReportedPlayers(int limit) {
        return plugin.getReportManager().getTopReportedPlayers(limit);
    }

    @Override
    public List<Map.Entry<String, Integer>> getTopHandlers(int limit) {
        return plugin.getReportManager().getTopHandlers(limit);
    }

    @Override
    public boolean isRewardEnabled() {
        return plugin.getRewardManager() != null && plugin.getRewardManager().isEnabled();
    }

    @Override
    public void giveReward(Player player) {
        if (plugin.getRewardManager() != null) {
            plugin.getRewardManager().giveReward(player);
        }
    }

    @Override
    public boolean claimReward(Player player, UUID reportId) {
        Report report = plugin.getReportManager().getReport(reportId);
        if (report == null) return false;
        if (report.getStatus() != ReportStatus.ACCEPTED) return false;
        if (report.isRewardClaimed()) return false;
        if (!isRewardEnabled()) return false;

        plugin.getReportManager().setRewardClaimed(reportId, true);
        giveReward(player);
        return true;
    }

    @Override
    public boolean isRewardClaimed(UUID reportId) {
        Report report = plugin.getReportManager().getReport(reportId);
        return report != null && report.isRewardClaimed();
    }

    @Override
    public void setRewardClaimed(UUID id, boolean claimed) {
        plugin.getReportManager().setRewardClaimed(id, claimed);
    }

    @Override
    public boolean isFrozen(Player player) {
        return plugin.isFrozen(player);
    }

    @Override
    public void freezePlayer(Player player) {
        plugin.freezePlayer(player);
    }

    @Override
    public void unfreezePlayer(Player player) {
        plugin.unfreezePlayer(player);
    }

    @Override
    public boolean reload() {
        try {
            plugin.reloadConfig();
            plugin.getLanguageManager().reload();
            plugin.reloadWebhookSettings();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ReportStatus getReportStatus(UUID id) {
        Report report = plugin.getReportManager().getReport(id);
        return report != null ? report.getStatus() : null;
    }

    @Override
    public Map<UUID, Report> getReportMap() {
        return plugin.getReportManager().getReportMap();
    }
}
