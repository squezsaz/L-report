package com.lreport.api;

import com.lreport.Report;
import com.lreport.Report.ReportStatus;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReportStatusChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Report report;
    private final ReportStatus oldStatus;
    private final ReportStatus newStatus;

    public ReportStatusChangeEvent(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
        this.report = report;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Report getReport() {
        return report;
    }

    public ReportStatus getOldStatus() {
        return oldStatus;
    }

    public ReportStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
