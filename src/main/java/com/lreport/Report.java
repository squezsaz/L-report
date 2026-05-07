package com.lreport;

import java.util.UUID;

public class Report {
    private UUID id;
    private final String reporterName;
    private final String reportedName;
    private final String reason;
    private final String evidence;
    private final long timestamp;
    private ReportStatus status;
    private String handledBy;
    private boolean rewardClaimed;

    public Report(String reporterName, String reportedName, String reason, String evidence) {
        this.id = UUID.randomUUID();
        this.reporterName = reporterName;
        this.reportedName = reportedName;
        this.reason = reason;
        this.evidence = evidence;
        this.timestamp = System.currentTimeMillis();
        this.status = ReportStatus.PENDING;
        this.handledBy = null;
        this.rewardClaimed = false;
    }

    public Report(UUID id, String reporterName, String reportedName, String reason, String evidence, long timestamp, ReportStatus status, String handledBy) {
        this.id = id;
        this.reporterName = reporterName;
        this.reportedName = reportedName;
        this.reason = reason;
        this.evidence = evidence;
        this.timestamp = timestamp;
        this.status = status;
        this.handledBy = handledBy;
        this.rewardClaimed = false;
    }

    public Report(UUID id, String reporterName, String reportedName, String reason, String evidence, long timestamp, ReportStatus status, String handledBy, boolean rewardClaimed) {
        this.id = id;
        this.reporterName = reporterName;
        this.reportedName = reportedName;
        this.reason = reason;
        this.evidence = evidence;
        this.timestamp = timestamp;
        this.status = status;
        this.handledBy = handledBy;
        this.rewardClaimed = rewardClaimed;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getReportedName() {
        return reportedName;
    }

    public String getReason() {
        return reason;
    }

    public String getEvidence() {
        return evidence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void setRewardClaimed(boolean rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }

    public enum ReportStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}