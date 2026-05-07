package com.lreport;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private final Main plugin;
    private Connection connection;
    private boolean isMySQL = false;
    private String tablePrefix = "";

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        String dbType = plugin.getConfig().getString("database-type", "sqlite").toLowerCase();
        if (dbType.equals("mysql")) {
            isMySQL = true;
            initializeMySQL();
        } else {
            initializeSQLite();
        }
    }

    private void initializeSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = new java.io.File(plugin.getDataFolder(), "reports.db").getPath();
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
            
            // Migrasyon: reward_claimed sütunu ekle
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
            plugin.getLogger().severe("SQLite initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeMySQL() {
        try {
            ConfigurationSection mysql = plugin.getConfig().getConfigurationSection("mysql");
            String host = mysql.getString("host", "localhost");
            int port = mysql.getInt("port", 3306);
            String database = mysql.getString("database", "lreport");
            String username = mysql.getString("username", "root");
            String password = mysql.getString("password", "");
            tablePrefix = mysql.getString("table-prefix", "lreport_");
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);
            
            try (Statement stmt = connection.createStatement()) {
                String reportsTable = tablePrefix + "reports";
                stmt.execute("CREATE TABLE IF NOT EXISTS " + reportsTable + " (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "reporter_name VARCHAR(16) NOT NULL, " +
                    "reported_name VARCHAR(16) NOT NULL, " +
                    "reason VARCHAR(50) NOT NULL, " +
                    "evidence TEXT, " +
                    "timestamp BIGINT NOT NULL, " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                    "handled_by VARCHAR(16), " +
                    "reward_claimed TINYINT(1) NOT NULL DEFAULT 0" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                
                String dailyTable = tablePrefix + "daily_counts";
                stmt.execute("CREATE TABLE IF NOT EXISTS " + dailyTable + " (" +
                    "player_name VARCHAR(16) NOT NULL, " +
                    "day_key BIGINT NOT NULL, " +
                    "count INT NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (player_name, day_key)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            }
            
            plugin.getLogger().info("MySQL database connected successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("MySQL initialization failed: " + e.getMessage());
            plugin.getLogger().info("Falling back to SQLite...");
            isMySQL = false;
            initializeSQLite();
        }
    }

    public boolean isMySQL() {
        return isMySQL;
    }

    public void saveReport(Report report) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO " + getTable("reports") + " (id, reporter_name, reported_name, reason, evidence, timestamp, status, handled_by, reward_claimed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
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
                "UPDATE " + getTable("reports") + " SET status = ?, handled_by = ? WHERE id = ?")) {
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
                "UPDATE " + getTable("reports") + " SET reward_claimed = ? WHERE id = ?")) {
            ps.setInt(1, claimed ? 1 : 0);
            ps.setString(2, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update reward claimed: " + e.getMessage());
        }
    }

    public Report getReport(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + getTable("reports") + " WHERE id = ?")) {
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

    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + getTable("reports") + " ORDER BY timestamp DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reports.add(extractReport(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all reports: " + e.getMessage());
        }
        return reports;
    }

    public int getTotalReportCount() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + getTable("reports"))) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get total count: " + e.getMessage());
        }
        return 0;
    }

    public int getPendingReportCount() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + getTable("reports") + " WHERE status = 'PENDING'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get pending count: " + e.getMessage());
        }
        return 0;
    }

    public int getAcceptedReportCount() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + getTable("reports") + " WHERE status = 'ACCEPTED'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get accepted count: " + e.getMessage());
        }
        return 0;
    }

    public int getRejectedReportCount() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + getTable("reports") + " WHERE status = 'REJECTED'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get rejected count: " + e.getMessage());
        }
        return 0;
    }

    public int getTodayReportCount() {
        long todayStart = getTodayStartTimestamp();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + getTable("reports") + " WHERE timestamp >= ?")) {
            ps.setLong(1, todayStart);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get today count: " + e.getMessage());
        }
        return 0;
    }

    public int getDailyCount(String playerName) {
        long dayKey = getDayKey();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT count FROM " + getTable("daily_counts") + " WHERE player_name = ? AND day_key = ?")) {
            ps.setString(1, playerName);
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

    public void incrementDailyCount(String playerName) {
        long dayKey = getDayKey();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO " + getTable("daily_counts") + " (player_name, day_key, count) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE count = count + 1")) {
            ps.setString(1, playerName);
            ps.setLong(2, dayKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to increment daily count: " + e.getMessage());
        }
    }

    public void deleteReport(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM " + getTable("reports") + " WHERE id = ?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete report: " + e.getMessage());
        }
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

    private String getTable(String table) {
        if (isMySQL) {
            return tablePrefix + table;
        }
        return table;
    }

    private long getTodayStartTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getDayKey() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void migrateFromYaml() {
        File reportsFile = new File(plugin.getDataFolder(), "reports.yml");
        if (!reportsFile.exists()) return;
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(reportsFile);
            if (!config.contains("reports")) return;
            
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
            
            File backupFile = new File(plugin.getDataFolder(), "reports_backup.yml");
            reportsFile.renameTo(backupFile);
            plugin.getLogger().info("Reports migrated from YAML to SQLite database");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Migration failed: " + e.getMessage());
        }
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

    public List<Map.Entry<String, Integer>> getTopHandlers(int limit) {
        List<Map.Entry<String, Integer>> topHandlers = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM " + getTable("reports") + " WHERE handled_by IS NOT NULL GROUP BY handled_by ORDER BY count DESC LIMIT ?")) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                topHandlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get top handlers: " + e.getMessage());
        }
        return topHandlers;
    }

    public List<Map.Entry<String, Integer>> getAllHandlers() {
        List<Map.Entry<String, Integer>> handlers = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT handled_by, COUNT(*) as count FROM " + getTable("reports") + " WHERE handled_by IS NOT NULL GROUP BY handled_by ORDER BY count DESC");
            while (rs.next()) {
                handlers.add(new java.util.AbstractMap.SimpleEntry<>(rs.getString("handled_by"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all handlers: " + e.getMessage());
        }
        return handlers;
    }

    public List<Map.Entry<String, Integer>> getAllHandlersDaily(int days) {
        List<Map.Entry<String, Integer>> handlers = new ArrayList<>();
        long startTime = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM " + getTable("reports") + " WHERE handled_by IS NOT NULL AND timestamp >= ? GROUP BY handled_by ORDER BY count DESC")) {
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

    public List<Map.Entry<String, Integer>> getTopHandlersDaily(int days, int limit) {
        List<Map.Entry<String, Integer>> topHandlers = new ArrayList<>();
        long startTime = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT handled_by, COUNT(*) as count FROM " + getTable("reports") + " WHERE handled_by IS NOT NULL AND timestamp >= ? GROUP BY handled_by ORDER BY count DESC LIMIT ?")) {
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
}