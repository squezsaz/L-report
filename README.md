# L-Report

<div align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.18--1.21+-brightgreen.svg)
![Spigot](https://img.shields.io/badge/Spigot-Paper-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-orange.svg)

</div>

> Advanced Minecraft reporting system that allows players to report other players via GUI.

---

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Config Settings](#config-settings)
- [Discord Integration](#discord-integration)
- [FAQ](#faq)
- [License](#license)
- [PlaceholderAPI](#placeholderapi)
- [API Documentation](#api-documentation)

---

## Features

| Feature | Description |
|---------|-------------|
| GUI Report System | Report players easily via GUI |
| Reason Selection | Hack, Toxic, Spam, Advertising, Insult, Other |
| Evidence System | Add evidence with report |
| Daily Limit | Daily report limit per player |
| Cooldown | Cooldown for re-reporting same player |
| Admin Panel | View all reports, accept/reject |
| Statistics | View report statistics |
| Freeze System | Freeze reported player |
| Report History | View your own reports |
| Offline Reporting | Report offline players (configurable) |
| Multi-Language | Turkish and English support |
| Discord Webhook | Send notifications to Discord |
| Reward | A system where players can receive rewards for their accepted reports |

---

## Installation

### 1. Download
Place the plugin JAR file in the `plugins/` folder.

### 2. Start
Start the server or use `/reload` command.

### 3. Auto-Generated Files
```
plugins/L-report/
├── config.yml          # Main config file
├── lang/
│   ├── tr.yml       # Turkish language file
│   └── en.yml       # English language file
├── lreport.db      # SQLite database
└── reports/       # Report data
```

---

## Commands

### Player Commands

| Command | Description |
|---------|-------------|
| `/report` | Opens main menu |
| `/report <player>` | Report player via GUI |
| `<playername>` | Report offline player (config must be enabled) |

### Admin Commands

| Command | Description |
|---------|-------------|
| `/reportadmin` | Opens admin panel |
| `/reportadmin search <player>` | Search player's reports |
| `/reportadmin stats` | Show statistics |
| `/report reload` | Reload config and language files |
| `/report lang tr\|en` | Change language |

---

## Permissions

| Permission | Description |
|------------|-------------|
| `lreport.player` | Can create reports |
| `lreport.admin` | Can use admin commands |

---

## Config Settings

```yaml
# ============================================
# L-Report Configuration
# ============================================

# Language setting
# Options: tr (Turkish), en (English)
language: tr

# Message prefix shown before all plugin messages
# Supports color codes with & character
# Example: "&8[&cRapor&8] &7" -> [Rapor]
prefix: "&8[&cRapor&8] &7"

# Cooldown time in minutes between reporting the same player
# Set to 0 to disable cooldown
cooldown: 30

# Maximum number of reports a player can create per day
# Set to 0 for unlimited reports
max-reports-per-day: 5

# Broadcast new reports to all online admins
# true = Enabled, false = Disabled
broadcast-reports: false

# Notify online admins when a new report is created
# true = Enabled, false = Disabled
notify-admins: true

# Allow reporting offline players via /report <player> command
# true = Enabled, false = Disabled
allow-offline-report: true

# ============================================
# Discord Webhook Settings
# ============================================

# Enable Discord webhook notifications
# true = Enabled, false = Disabled
webhook-enabled: false

# Discord webhook URL
# Get this from Discord channel settings -> Integrations -> Webhooks
webhook-url: ""

# Webhook username shown in Discord messages
webhook-username: "L-Report Bot"

# Embed color (in decimal)
# Red: 15548984, Green: 5767199, Yellow: 16440904, Blue: 3447003
webhook-color: 15548984

# Which events to send to Discord
# true = Send, false = Don't send
webhook-new-report: true
webhook-report-accepted: true
webhook-report-rejected: true
webhook-report-deleted: false

# ============================================
# Reward System
# ============================================

# Enable reward system when reports are accepted
# true = Enabled, false = Disabled
reward-enabled: false

# Reward commands list (add keys: 0, 1, 2, etc.)
# %player% = Reporter's name
# console: true = Run as console, false = Run as player
reward-commands:
  0:
    command: "give %player% money 100"
    console: true
  1:
    command: "crate give %player% basic 1"
    console: true
```

---

## Discord Integration

### Setup

1. Open Discord server settings
2. Go to **Integrations** > **Webhooks** > **New Webhook**
3. Copy the webhook URL
4. Paste into config file:

```yaml
webhook-enabled: true
webhook-url: "https://discord.com/api/webhooks/xxxxx/xxxxx"
```

### Embed Appearance

```
:inbox_tray: New Report Notification
A player has created a new report. Check the admin panel for details.

:bust_in_silhouette: Reporter    | PlayerName
:skull: Reported           | TargetName
:clipboard: Reason        | :crossed_swords: Hack
:file_folder: Evidence    | evidence text
:shield: Handler         | AdminName

L-Report Plugin | 23/04/2026 23:16
```

### Embed Colors

| Event | Color | HEX |
|------|-------|-----|
| New Report | Orange | 15105570 |
| Accepted | Green | 5767199 |
| Rejected | Red | 15548984 |
| Deleted | Yellow | 16440904 |

---

## Database

### SQLite (Default)
Stored in auto-generated `lreport.db` file.

---

## FAQ

**Q: Plugin not working - What should I do?**
> A: First check the console for errors. Then try `/reload`.

**Q: GUI not opening?**
> A: Check if you have `inventory` permission.

**Q: Language file not working?**
> A: Delete the language files on server and restart.

**Q: Discord webhook not working?**
> A: Check if URL is correct. Make sure `webhook-enabled: true`.

---

## License

MIT License

---

Placeholders

###  General Report Statistics
* `%lreport_total_reports%`: Total number of reports.
* `%lreport_pending_reports%`: Number of reports with "PENDING" status.
* `%lreport_accepted_reports%`: Number of reports with "ACCEPTED" status.
* `%lreport_rejected_reports%`: Number of reports with "REJECTED" status.
* `%lreport_today_reports%`: Number of reports submitted today.
* `%lreport_week_reports%`: Number of reports submitted this week.
* `%lreport_month_reports%`: Number of reports submitted this month.
* `%lreport_acceptance_rate%`: The overall percentage of accepted reports.
* `%lreport_rejection_rate%`: The overall percentage of rejected reports.
* `%lreport_pending_rate%`: The overall percentage of pending reports.
* `%lreport_most_common_reason%`: The most frequently cited reason for reports.

---

###  Personal Player Statistics
* `%lreport_my_reports%`: Total reports submitted by the player.
* `%lreport_my_pending_reports%`: Number of pending reports submitted by the player.
* `%lreport_my_accepted_reports%`: Number of accepted reports submitted by the player.
* `%lreport_my_rejected_reports%`: Number of rejected reports submitted by the player.
* `%lreport_my_today_reports%`: Number of reports submitted by the player today.
* `%lreport_my_remaining_reports%`: Number of report attempts the player has left for today.
* `%lreport_my_status%`: Status of the player's most recent report.
* `%lreport_my_can_report%`: Whether the player is currently allowed to create a report (`true`/`false`).
* `%lreport_my_acceptance_rate%`: The acceptance rate percentage for the player's own reports.
* `%lreport_my_cooldown%`: The remaining cooldown time for the player (in seconds).

---

###  Leaderboards (Top Lists)
* **Reporters:**
    * `%lreport_top_reporter%`: Name of the player who has submitted the most reports.
    * `%lreport_top_reporter_count%`: The report count of the top reporter.
    * `%lreport_top_reporter_2%`: Name of the 2nd rank reporter.
    * `%lreport_top_reporter_3%`: Name of the 3rd rank reporter.
* **Reported Players:**
    * `%lreport_most_reported%`: Name of the player with the most reports against them.
    * `%lreport_most_reported_count%`: The number of reports the most reported player has received.
* **Staff/Handlers:**
    * `%lreport_top_handler%`: Name of the staff member who has handled the most reports.
    * `%lreport_top_handler_count%`: The number of reports handled by the top staff member.

---

###  Statistics by Reason
* **Hack:** `%lreport_hack_reports%`, `%lreport_hack_pending%`, `%lreport_hack_accepted%`, `%lreport_hack_rejected%`
* **Toxic:** `%lreport_toxic_reports%`, `%lreport_toxic_pending%`, `%lreport_toxic_accepted%`, `%lreport_toxic_rejected%`
* **Spam:** `%lreport_spam_reports%`, `%lreport_spam_pending%`, `%lreport_spam_accepted%`, `%lreport_spam_rejected%`
* **Ad:** `%lreport_ad_reports%`, `%lreport_ad_pending%`, `%lreport_ad_accepted%`, `%lreport_ad_rejected%`
* **Insult:** `%lreport_insult_reports%`, `%lreport_insult_pending%`, `%lreport_insult_accepted%`, `%lreport_insult_rejected%`

---

###  Date and Time Information
* `%lreport_first_report_date%`: Date of the very first report on the server.
* `%lreport_first_report_time%`: Time of the very first report on the server.
* `%lreport_last_report_date%`: Date of the most recent report on the server.
* `%lreport_last_report_time%`: Time of the most recent report on the server.
* `%lreport_last_report_datetime%`: Full date and time of the last report.
* `%lreport_my_first_report_date%`: The player's individual first report date.
* `%lreport_my_last_report_date%`: The player's individual last report date.

---

###  System and Database
* `%lreport_db_total%`: Total report count directly from the database.
* `%lreport_is_frozen%`: Whether the player is currently frozen (`true`/`false`).
* `%lreport_frozen_count%`: Total number of players currently frozen.
* `%lreport_plugin_version%`: The current version of the L-Report plugin.
* `%lreport_cooldown%`: The global cooldown setting from the config.
* `%lreport_online_players%`: Number of players currently online.




## Developer API Integration

Integrate L-report into your own plugin to interact with our reporting system. Follow the steps below to get started.

### 1. Add Repository
First, add the JitPack repository to the `<repositories>` section of your `pom.xml` file:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### 2. Add Dependency
Next, add the L-report API dependency to your `<dependencies>` section. Replace `TAG` with the version you wish to use (e.g., `v1.0.0`).

```xml
<dependencies>
    <dependency>
        <groupId>com.github.squezsaz</groupId>
        <artifactId>L-report</artifactId>
        <version>TAG</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

> **Note:** Since your plugin will require L-report at runtime, keep the scope as `provided` and remember to add `depend: [L-report]` to your `plugin.yml` file.

### 3. Usage Example
You can access the API by getting an instance through the main class:

```java
import com.lreport.api.LReportAPI;

public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Accessing the API
        LReportAPI api = LReport.getApi();
        
        if (api != null) {
            int pendingReports = api.getPendingReportCount();
            getLogger().info("There are currently " + pendingReports + " pending reports.");
        }
    }
}
```

---

###  API Documentation
For a detailed list of methods and descriptions, please visit our online JavaDoc page:
👉 [L-report JavaDocs](https://javadoc.jitpack.io/com/github/squezsaz/L-report/v1.1.5/javadoc/com/lreport/api/LReportAPI.html)

---

### Quick Tips:
*   **Version:** If you want to always pull the latest code, you can use `master-SNAPSHOT` as the `<version>`, though fixed versions like `v1.0.0` are recommended for stability.
*   **JitPack Logs:** If the dependency fails to download, check the build logs on JitPack to identify the error.


## Contact

For any questions or suggestions, discord: squezsaz

---

**Note:** This plugin works on Spigot, Paper and derivatives. (1.18 - 1.21+)
