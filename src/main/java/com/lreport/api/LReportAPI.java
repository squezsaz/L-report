package com.lreport.api;

import com.lreport.Report;
import com.lreport.Report.ReportStatus;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LReportAPI {

    Report createReport(String reporterName, String reportedName, String reason, String evidence);

    Report getReport(UUID id);

    List<Report> getAllReports();

    List<Report> getPendingReports();

    List<Report> getResolvedReports();

    List<Report> getReportsByReporter(String playerName);

    List<Report> getReportsByReported(String playerName);

    boolean acceptReport(UUID id, String adminName);

    boolean rejectReport(UUID id, String adminName);

    boolean deleteReport(UUID id);

    boolean hasPendingReport(String reporterName, String reportedName);

    boolean canCreateReport(String reporterName);

    int getRemainingReports(String reporterName);

    boolean hasCooldown(String reporterName, String reportedName);

    long getCooldownRemaining(String reporterName, String reportedName);

    int getTotalReportCount();

    int getPendingReportCount();

    int getAcceptedReportCount();

    int getRejectedReportCount();

    int getTodayReportCount();

    int getTodayReportCountByPlayer(String playerName);

    int getThisWeekReportCount();

    int getThisMonthReportCount();

    int getReportCountByPlayer(String playerName);

    int getReportsMadeByPlayer(String playerName);

    int getReportsAgainstPlayer(String playerName);

    String getMostCommonReason();

    double getAcceptanceRate();

    double getRejectionRate();

    String getTopReporter();

    String getTopHandler();

    String getMostReportedPlayer();

    List<Map.Entry<String, Integer>> getTopReporters(int limit);

    List<Map.Entry<String, Integer>> getTopReportedPlayers(int limit);

    List<Map.Entry<String, Integer>> getTopHandlers(int limit);

    boolean isRewardEnabled();

    void giveReward(Player player);

    boolean claimReward(Player player, UUID reportId);

    boolean isRewardClaimed(UUID reportId);

    void setRewardClaimed(UUID id, boolean claimed);

    boolean isFrozen(Player player);

    void freezePlayer(Player player);

    void unfreezePlayer(Player player);

    boolean reload();

    ReportStatus getReportStatus(UUID id);

    Map<UUID, Report> getReportMap();
}
