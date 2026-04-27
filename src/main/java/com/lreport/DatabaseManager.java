package com.lreport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private final Main plugin;
    private Connection connection;
    private final String dbPath;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.dbPath = new File(plugin.getDataFolder(), "lreport.db").getAbsolutePath();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS reports (" +
                    "id TEXT PRIMARY KEY, " +
                    "reporter_name TEXT NOT NULL, " +
                    "reported_name TEXT NOT NULL, " +
                    "reason TEXT NOT NULL, " +
                    "evidence TEXT, " +
                    "timestamp INTEGER NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'PENDING', " +
                    "handled_by TEXT" +
                    ")");
                
                stmt.execute("CREATE TABLE IF NOT EXISTS daily_counts (" +
                    "player_name TEXT NOT NULL, " +
                    "day_key INTEGER NOT NULL, " +
                    "count INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (player_name, day_key)" +
                    ")");
            }
            
            // Migrasyon: reward_claimed sütunu ekle (eski veritabanları için)
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(reports)");
                boolean hasColumn = false;
                while (rs.next()) {
                    if (rs.getString("name").equals("reward_claimed")) {
                        hasColumn = true;
                        break;
                    }
                }
                if (!hasColumn) {
                    stmt.execute("ALTER TABLE reports ADD COLUMN reward_claimed INTEGER NOT NULL DEFAULT 0");
                }
            }
            
            migrateFromYaml();
            
        } catch (Exception e) {
            plugin.getLogger().severe("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void migrateFromYaml() {
        File reportsFile = new File(plugin.getDataFolder(), "reports.yml");
        if (!reportsFile.exists()) return;
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(reportsFile);
            if (!config.contains("reports")) return;
            
            try (Statement stmt = connection.createStatement()) {
                for (String key : config.getConfigurationSection("reports").getKeys(false)) {
                    String path = "reports." + key;
                    String id = UUID.randomUUID().toString();
                    String reporter = config.getString(path + ".reporter");
                    String reported = config.getString(path + ".reported");
                    String reason = config.getString(path + ".reason");
                    String evidence = config.getString(path + ".evidence");
                    long timestamp = config.getLong(path + ".timestamp");
                    String status = config.getString(path + ".status", "PENDING");
                    
                    try (PreparedStatement ps = connection.prepareStatement(
                            "INSERT OR IGNORE INTO reports (id, reporter_name, reported_name, reason, evidence, timestamp, status) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, id);
                        ps.setString(2, reporter);
                        ps.setString(3, reported);
                        ps.setString(4, reason);
                        ps.setString(5, evidence);
                        ps.setLong(6, timestamp);
                        ps.setString(7, status);
                        ps.executeUpdate();
                    }
                }
            }
            
            File backupFile = new File(plugin.getDataFolder(), "reports_backup.yml");
            reportsFile.renameTo(backupFile);
            plugin.getLogger().info("Reports migrated from YAML to SQLite database");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Migration failed: " + e.getMessage());
        }
    }

    public void saveReport(Report report) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO reports (id, reporter_name, reported_name, reason, evidence, timestamp, status, handled_by, reward_claimed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, report.getId().toString());
            ps.setString(2, report.getReporterName());
            ps.setString(3, report.getReportedName());
            ps.setString(4, report.getReason());
            ps.setString(5, report.getEvidence());
            ps.setLong(6, report.getTimestamp());
            ps.setString(7, report.getStatus().name());
            ps.setString(8, report.getHandledBy());
            ps.setInt(9, report.isRewardClaimed() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save report: " + e.getMessage());
        }
    }

    public void updateReportStatus(UUID id, Report.ReportStatus status, String handledBy) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE reports SET status = ?, handled_by = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setString(2, handledBy);
            ps.setString(3, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update report status: " + e.getMessage());
        }
    }

    public void updateRewardClaimed(UUID id, boolean claimed) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE reports SET reward_claimed = ? WHERE id = ?")) {
            ps.setInt(1, claimed ? 1 : 0);
            ps.setString(2, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update reward claimed: " + e.getMessage());
        }
    }

    public void deleteReport(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM reports WHERE id = ?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete report: " + e.getMessage());
        }
    }

    public Report getReport(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM reports WHERE id = ?")) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return extractReport(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get report: " + e.getMessage());
        }
        return null;
    }

    public java.util.List<Report> getAllReports() {
        java.util.List<Report> reports = new java.util.ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM reports ORDER BY timestamp DESC");
            while (rs.next()) {
                reports.add(extractReport(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all reports: " + e.getMessage());
        }
        return reports;
    }

    private Report extractReport(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String reporter = rs.getString("reporter_name");
        String reported = rs.getString("reported_name");
        String reason = rs.getString("reason");
        String evidence = rs.getString("evidence");
        long timestamp = rs.getLong("timestamp");
        String statusStr = rs.getString("status");
        String handledBy = rs.getString("handled_by");
        boolean rewardClaimed = rs.getInt("reward_claimed") == 1;
        
        return new Report(
            UUID.fromString(id),
            reporter,
            reported,
            reason,
            evidence,
            timestamp,
            Report.ReportStatus.valueOf(statusStr),
            handledBy,
            rewardClaimed
        );
    }

    public void incrementDailyCount(String playerName) {
        long dayKey = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO daily_counts (player_name, day_key, count) VALUES (?, ?, 1) " +
                "ON CONFLICT(player_name, day_key) DO UPDATE SET count = count + 1")) {
            ps.setString(1, playerName.toLowerCase());
            ps.setLong(2, dayKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to increment daily count: " + e.getMessage());
        }
    }

    public int getDailyCount(String playerName) {
        long dayKey = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT count FROM daily_counts WHERE player_name = ? AND day_key = ?")) {
            ps.setString(1, playerName.toLowerCase());
            ps.setLong(2, dayKey);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get daily count: " + e.getMessage());
        }
        return 0;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database: " + e.getMessage());
        }
    }

    public int getTotalReportCount() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM reports");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get total report count: " + e.getMessage());
        }
        return 0;
    }

    public int getPendingReportCount() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM reports WHERE status = 'PENDING'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get pending report count: " + e.getMessage());
        }
        return 0;
    }

    public int getAcceptedReportCount() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM reports WHERE status = 'ACCEPTED'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get accepted report count: " + e.getMessage());
        }
        return 0;
    }

    public int getRejectedReportCount() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM reports WHERE status = 'REJECTED'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get rejected report count: " + e.getMessage());
        }
        return 0;
    }

    public int getReportCountByHandler(String handlerName) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM reports WHERE handled_by = ?")) {
            ps.setString(1, handlerName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get handler report count: " + e.getMessage());
        }
        return 0;
    }

    public java.util.List<java.util.Map.Entry<String, Integer>> getTopHandlers(int limit) {
        java.util.List<java.util.Map.Entry<String, Integer>> topHandlers = new java.util.ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT handled_by, COUNT(*) as count FROM reports WHERE handled_by IS NOT NULL GROUP BY handled_by ORDER BY count DESC LIMIT " + limit);
            while (rs.next()) {
                topHandlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get top handlers: " + e.getMessage());
        }
        return topHandlers;
    }

    public java.util.List<java.util.Map.Entry<String, Integer>> getAllHandlers() {
        java.util.List<java.util.Map.Entry<String, Integer>> handlers = new java.util.ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT handled_by, COUNT(*) as count FROM reports WHERE handled_by IS NOT NULL GROUP BY handled_by ORDER BY count DESC");
            while (rs.next()) {
                handlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all handlers: " + e.getMessage());
        }
        return handlers;
    }

    public java.util.List<java.util.Map.Entry<String, Integer>> getAllHandlersDaily(int days) {
        java.util.List<java.util.Map.Entry<String, Integer>> handlers = new java.util.ArrayList<>();
        long startTime = (System.currentTimeMillis() / (24 * 60 * 60 * 1000) - days) * 24 * 60 * 60 * 1000;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM reports WHERE handled_by IS NOT NULL AND timestamp >= ? GROUP BY handled_by ORDER BY count DESC")) {
            ps.setLong(1, startTime);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                handlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all handlers daily: " + e.getMessage());
        }
        return handlers;
    }

    public java.util.List<java.util.Map.Entry<String, Integer>> getAllHandlersWeekly(int days) {
        java.util.List<java.util.Map.Entry<String, Integer>> handlers = new java.util.ArrayList<>();
        long startTime = (System.currentTimeMillis() / (24 * 60 * 60 * 1000) - days) * 24 * 60 * 60 * 1000;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM reports WHERE handled_by IS NOT NULL AND timestamp >= ? GROUP BY handled_by ORDER BY count DESC")) {
            ps.setLong(1, startTime);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                handlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all handlers weekly: " + e.getMessage());
        }
        return handlers;
    }

    public java.util.List<java.util.Map.Entry<String, Integer>> getAllHandlersMonthly(int days) {
        java.util.List<java.util.Map.Entry<String, Integer>> handlers = new java.util.ArrayList<>();
        long startTime = (System.currentTimeMillis() / (24 * 60 * 60 * 1000) - days) * 24 * 60 * 60 * 1000;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM reports WHERE handled_by IS NOT NULL AND timestamp >= ? GROUP BY handled_by ORDER BY count DESC")) {
            ps.setLong(1, startTime);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                handlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all handlers monthly: " + e.getMessage());
        }
        return handlers;
    }

public java.util.List<java.util.Map.Entry<String, Integer>> getTopHandlersDaily(int days, int limit) {
        java.util.List<java.util.Map.Entry<String, Integer>> topHandlers = new java.util.ArrayList<>();
        long startTime = (System.currentTimeMillis() / (24 * 60 * 60 * 1000) - days) * 24 * 60 * 60 * 1000;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM reports WHERE handled_by IS NOT NULL AND timestamp >= ? GROUP BY handled_by ORDER BY count DESC LIMIT ?")) {
            ps.setLong(1, startTime);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                topHandlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get top handlers daily: " + e.getMessage());
        }
        return topHandlers;
    }

    public java.util.List<java.util.Map.Entry<String, Integer>> getTopHandlersWeekly(int days, int limit) {
        java.util.List<java.util.Map.Entry<String, Integer>> topHandlers = new java.util.ArrayList<>();
        long startTime = (System.currentTimeMillis() / (24 * 60 * 60 * 1000) - days) * 24 * 60 * 60 * 1000;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM reports WHERE handled_by IS NOT NULL AND timestamp >= ? GROUP BY handled_by ORDER BY count DESC LIMIT ?")) {
            ps.setLong(1, startTime);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                topHandlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get top handlers weekly: " + e.getMessage());
        }
        return topHandlers;
    }

    public java.util.List<java.util.Map.Entry<String, Integer>> getTopHandlersMonthly(int days, int limit) {
        java.util.List<java.util.Map.Entry<String, Integer>> topHandlers = new java.util.ArrayList<>();
        long startTime = (System.currentTimeMillis() / (24 * 60 * 60 * 1000) - days) * 24 * 60 * 60 * 1000;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM reports WHERE handled_by IS NOT NULL AND timestamp >= ? GROUP BY handled_by ORDER BY count DESC LIMIT ?")) {
            ps.setLong(1, startTime);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                topHandlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get top handlers monthly: " + e.getMessage());
        }
        return topHandlers;
    }

    public int getTodayReportCount() {
        long dayKey = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM reports WHERE timestamp >= ?")) {
            ps.setLong(1, dayKey * 24 * 60 * 60 * 1000);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get today report count: " + e.getMessage());
        }
        return 0;
    }
}