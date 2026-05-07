package com.lreport.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReportCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final String reporterName;
    private final String reportedName;
    private final String reason;
    private final String evidence;

    public ReportCreateEvent(String reporterName, String reportedName, String reason, String evidence) {
        this.reporterName = reporterName;
        this.reportedName = reportedName;
        this.reason = reason;
        this.evidence = evidence;
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
