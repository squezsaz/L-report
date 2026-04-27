package com.lreport;

import com.lreport.Report.ReportStatus;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUI implements Listener {
   private final Main plugin;
   private final LanguageManager lang;
   private final ReportManager reportManager;
   private ChatInputListener chatInputListener;
   private WebhookManager webhookManager;
   private RewardManager rewardManager;
   private static final Map<UUID, ReportData> playerData = new HashMap();
   private static final Map<UUID, Map<Integer, UUID>> playerReportSlots = new HashMap();

   public GUI(Main plugin, LanguageManager lang, ReportManager reportManager) {
      this.plugin = plugin;
      this.lang = lang;
      this.reportManager = reportManager;
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   public void setChatInputListener(ChatInputListener listener) {
      this.chatInputListener = listener;
   }

   public void setWebhookManager(WebhookManager webhookManager) {
      this.webhookManager = webhookManager;
   }

   public void setRewardManager(RewardManager rewardManager) {
      this.rewardManager = rewardManager;
   }

   public void openMainMenu(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("main"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.main_title")));
      ItemStack reportBtn = this.createItem(Material.PAPER, this.lang.get("gui.select_player"));
      inv.setItem(11, reportBtn);
      ItemStack myReportsBtn = this.createItem(Material.BOOK, this.lang.get("gui.my_reports"));
      inv.setItem(15, myReportsBtn);
      player.openInventory(inv);
   }

   public void openPlayerSelect(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("playerSelect"), 54, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.select_player")));
      ItemStack searchBtn = this.createItem(Material.NAME_TAG, this.lang.get("gui.search_player"));
      inv.setItem(49, searchBtn);
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(45, backBtn);
      ItemStack closeBtn = this.createItem(Material.BARRIER, this.lang.get("gui.close"));
      inv.setItem(53, closeBtn);
      List<Player> onlinePlayers = new ArrayList(Bukkit.getOnlinePlayers());
      onlinePlayers.remove(player);
      int slot = 0;

      for(Player target : onlinePlayers) {
         if (slot >= 45) {
            break;
         }

         ItemStack playerHead = this.createPlayerHead(target);
         inv.setItem(slot, playerHead);
         ++slot;
      }

      player.openInventory(inv);
   }

   public void openSearchMenu(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("searchPlayer"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.search_player")));
      ItemStack inputItem = this.createItem(Material.PAPER, this.lang.get("gui.write_playername"));
      inv.setItem(13, inputItem);
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(18, backBtn);
      if (this.chatInputListener != null) {
         this.chatInputListener.setAwaitingSearch(player);
      }

      player.openInventory(inv);
   }

   public void openReasonSelectWithPlayer(Player player, String targetName) {
      ReportData data = (ReportData)playerData.computeIfAbsent(player.getUniqueId(), (k) -> new ReportData());
      data.setTargetName(targetName);
      this.openReasonSelectGui(player);
   }

   private void openReasonSelectGui(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("reasonSelect"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.select_reason")));
      inv.setItem(10, this.createItem(Material.DIAMOND_SWORD, this.lang.get("gui.reason_hack")));
      inv.setItem(11, this.createItem(Material.REDSTONE, this.lang.get("gui.reason_toxic")));
      inv.setItem(12, this.createItem(Material.FEATHER, this.lang.get("gui.reason_spam")));
      inv.setItem(13, this.createItem(Material.OAK_SIGN, this.lang.get("gui.reason_ad")));
      inv.setItem(14, this.createItem(Material.FLINT_AND_STEEL, this.lang.get("gui.reason_insult")));
      inv.setItem(15, this.createItem(Material.COMPASS, this.lang.get("gui.reason_other")));
      inv.setItem(18, this.createItem(Material.ARROW, this.lang.get("gui.back")));
      inv.setItem(26, this.createItem(Material.BARRIER, this.lang.get("gui.close")));
      player.openInventory(inv);
   }

   public void openSettingsMenu(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("settings"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.settings")));
      inv.setItem(11, this.createItem(Material.PAPER, "&a&lTR &7- &eTürkçe"));
      inv.setItem(13, this.createItem(Material.PAPER, "&c&lEN &7- &eEnglish"));
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(18, backBtn);
      player.openInventory(inv);
   }

   public void openReasonSelect(Player player, String targetName) {
      ReportData data = (ReportData)playerData.computeIfAbsent(player.getUniqueId(), (k) -> new ReportData());
      data.setTargetName(targetName);
      Inventory inv = Bukkit.createInventory(new GUIHolder("reasonSelect"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.select_reason")));
      inv.setItem(10, this.createItem(Material.DIAMOND_SWORD, this.lang.get("gui.reason_hack")));
      inv.setItem(11, this.createItem(Material.REDSTONE, this.lang.get("gui.reason_toxic")));
      inv.setItem(12, this.createItem(Material.FEATHER, this.lang.get("gui.reason_spam")));
      inv.setItem(13, this.createItem(Material.OAK_SIGN, this.lang.get("gui.reason_ad")));
      inv.setItem(14, this.createItem(Material.FLINT_AND_STEEL, this.lang.get("gui.reason_insult")));
      inv.setItem(15, this.createItem(Material.COMPASS, this.lang.get("gui.reason_other")));
      inv.setItem(18, this.createItem(Material.ARROW, this.lang.get("gui.back")));
      inv.setItem(26, this.createItem(Material.BARRIER, this.lang.get("gui.close")));
      player.openInventory(inv);
   }

   public void openEvidenceInput(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("evidenceInput"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.add_evidence")));
      ItemStack inputItem = this.createItem(Material.PAPER, this.lang.get("gui.write_evidence"));
      inv.setItem(13, inputItem);
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(18, backBtn);
      ItemStack submitBtn = this.createItem(Material.EMERALD_BLOCK, this.lang.get("gui.submit"));
      inv.setItem(26, submitBtn);
      player.openInventory(inv);
   }

   public void openConfirmReport(Player player) {
      ReportData data = (ReportData)playerData.get(player.getUniqueId());
      if (data != null && data.getTargetName() != null) {
         Inventory inv = Bukkit.createInventory(new GUIHolder("confirmReport"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.report_sent")));
         String targetName = data.getTargetName();
         String reason = data.getReason();
         String evidence = data.getEvidence() != null ? data.getEvidence() : "-";
         Material var10001 = Material.PAPER;
         String var10002 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.confirm_title"));
         String[] var10003 = new String[3];
         String var10006 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.target_player"));
         var10003[0] = var10006 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + targetName;
         var10006 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reason"));
         var10003[1] = var10006 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + reason;
         var10006 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.your_evidence"));
         var10003[2] = var10006 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + evidence;
         ItemStack info = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
         inv.setItem(13, info);
         ItemStack cancelBtn = this.createItem(Material.REDSTONE_BLOCK, this.lang.get("gui.cancel"));
         inv.setItem(11, cancelBtn);
         ItemStack submitBtn = this.createItem(Material.EMERALD_BLOCK, this.lang.get("gui.submit"));
         inv.setItem(15, submitBtn);
         player.openInventory(inv);
      } else {
         player.closeInventory();
      }
   }

   public void openAdminMenu(Player player) {
      this.openAdminMenu(player, (String)null, (String)null, (String)null);
   }

   public void openAdminMenuWithFilter(Player player, String filter) {
      this.openAdminMenu(player, filter, (String)null, (String)null);
   }

   public void openAdminMenu(Player player, String filterName, String filterStatus, String sortOrder) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("admin"), 54, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.admin_title")));
      ItemStack searchBtn = this.createItem(Material.NAME_TAG, this.lang.get("gui.search_report"));
      inv.setItem(49, searchBtn);
      ItemStack statsBtn = this.createItem(Material.NETHER_STAR, this.lang.get("gui.stats"));
      inv.setItem(50, statsBtn);
      ItemStack pendingBtn = this.createItem(Material.YELLOW_STAINED_GLASS_PANE, this.lang.get("gui.filter_pending"));
      ItemStack acceptedBtn = this.createItem(Material.GREEN_STAINED_GLASS_PANE, this.lang.get("gui.filter_accepted"));
      ItemStack rejectedBtn = this.createItem(Material.RED_STAINED_GLASS_PANE, this.lang.get("gui.filter_rejected"));
      inv.setItem(19, pendingBtn);
      inv.setItem(22, acceptedBtn);
      inv.setItem(25, rejectedBtn);
      ItemStack newestBtn = this.createItem(Material.ARROW, this.lang.get("gui.sort_newest"));
      ItemStack oldestBtn = this.createItem(Material.ARROW, this.lang.get("gui.sort_oldest"));
      inv.setItem(28, newestBtn);
      inv.setItem(34, oldestBtn);
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(45, backBtn);
      ItemStack closeBtn = this.createItem(Material.BARRIER, this.lang.get("gui.close"));
      inv.setItem(53, closeBtn);
      Map<Integer, UUID> slotMap = new HashMap();
      List<Report> allReports = this.reportManager.getAllReports();
      List<Report> filteredReports = new ArrayList();

      for(Report report : allReports) {
         boolean nameMatch = true;
         boolean statusMatch = true;
         if (filterName != null && !filterName.isEmpty()) {
            nameMatch = report.getReportedName().toLowerCase().contains(filterName.toLowerCase()) || report.getReporterName().toLowerCase().contains(filterName.toLowerCase());
         }

         if (filterStatus != null && !filterStatus.isEmpty()) {
            statusMatch = report.getStatus().name().equalsIgnoreCase(filterStatus);
         }

         if (nameMatch && statusMatch) {
            filteredReports.add(report);
         }
      }

      if (sortOrder != null && sortOrder.equals("oldest")) {
         filteredReports.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
      } else {
         filteredReports.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
      }

      if (filteredReports.isEmpty()) {
         inv.setItem(22, this.createItem(Material.BARRIER, this.lang.get("gui.no_reports")));
      } else {
         int slot = 0;

         for(Report report : filteredReports) {
            if (slot >= 45) {
               break;
            }

            ItemStack reportItem = this.createReportItem(report, slot + 1);
            inv.setItem(slot, reportItem);
            slotMap.put(slot, report.getId());
            ++slot;
         }
      }

      playerReportSlots.put(player.getUniqueId(), slotMap);
      player.openInventory(inv);
   }

   public void openAdminSearchMenu(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("adminSearch"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.search_report")));
      ItemStack inputItem = this.createItem(Material.PAPER, this.lang.get("gui.write_search"));
      inv.setItem(13, inputItem);
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(18, backBtn);
      if (this.chatInputListener != null) {
         this.chatInputListener.setAwaitingAdminSearch(player);
      }

      player.openInventory(inv);
   }

   public void openStatsMenu(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("stats"), 36, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.stats_title")));
      DatabaseManager db = this.plugin.getDatabaseManager();
      int total = db.getTotalReportCount();
      int pending = db.getPendingReportCount();
      int accepted = db.getAcceptedReportCount();
      int rejected = db.getRejectedReportCount();
      int today = db.getTodayReportCount();
      Material var10003 = Material.PAPER;
      String var10004 = this.lang.get("gui.stats_total");
      String[] var10005 = new String[1];
      String var10008 = String.valueOf(ChatColor.GRAY);
      var10005[0] = var10008 + String.valueOf(total);
      inv.setItem(11, this.createItemWithLore(var10003, var10004, Arrays.asList(var10005)));
      var10003 = Material.YELLOW_STAINED_GLASS_PANE;
      var10004 = this.lang.get("gui.stats_pending");
      var10005 = new String[1];
      var10008 = String.valueOf(ChatColor.GRAY);
      var10005[0] = var10008 + String.valueOf(pending);
      inv.setItem(12, this.createItemWithLore(var10003, var10004, Arrays.asList(var10005)));
      var10003 = Material.GREEN_STAINED_GLASS_PANE;
      var10004 = this.lang.get("gui.stats_accepted");
      var10005 = new String[1];
      var10008 = String.valueOf(ChatColor.GRAY);
      var10005[0] = var10008 + String.valueOf(accepted);
      inv.setItem(13, this.createItemWithLore(var10003, var10004, Arrays.asList(var10005)));
      var10003 = Material.RED_STAINED_GLASS_PANE;
      var10004 = this.lang.get("gui.stats_rejected");
      var10005 = new String[1];
      var10008 = String.valueOf(ChatColor.GRAY);
      var10005[0] = var10008 + String.valueOf(rejected);
      inv.setItem(14, this.createItemWithLore(var10003, var10004, Arrays.asList(var10005)));
      var10003 = Material.BOOKSHELF;
      var10004 = this.lang.get("gui.stats_today");
      var10005 = new String[1];
      var10008 = String.valueOf(ChatColor.GRAY);
      var10005[0] = var10008 + String.valueOf(today);
      inv.setItem(15, this.createItemWithLore(var10003, var10004, Arrays.asList(var10005)));
      Material var10001 = Material.PLAYER_HEAD;
      String var10002 = this.lang.get("gui.top_handlers");
      String[] var16 = new String[1];
      String var10006 = String.valueOf(ChatColor.GRAY);
      var16[0] = var10006 + this.lang.get("gui.top_handlers_desc");
      ItemStack topHandlersBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var16));
      inv.setItem(22, topHandlersBtn);
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(27, backBtn);
      ItemStack closeBtn = this.createItem(Material.BARRIER, this.lang.get("gui.close"));
      inv.setItem(35, closeBtn);
      player.openInventory(inv);
   }

   public void openTopHandlersMenu(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("topHandlers"), 54, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.top_handlers_title")));
      DatabaseManager db = this.plugin.getDatabaseManager();
      List<Map.Entry<String, Integer>> topHandlers = db.getTopHandlers(10);
      Material var10001 = Material.NETHER_STAR;
      String var10002 = this.lang.get("gui.top_handlers_all_time");
      String[] var10003 = new String[1];
      String var10006 = String.valueOf(ChatColor.GRAY);
      var10003[0] = var10006 + this.lang.get("gui.top_handlers_all_time_desc");
      ItemStack allTimeBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
      inv.setItem(19, allTimeBtn);
      var10001 = Material.CLOCK;
      var10002 = this.lang.get("gui.top_handlers_daily");
      var10003 = new String[1];
      var10006 = String.valueOf(ChatColor.GRAY);
      var10003[0] = var10006 + this.lang.get("gui.top_handlers_daily_desc");
      ItemStack dailyBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
      inv.setItem(21, dailyBtn);
      var10001 = Material.DAYLIGHT_DETECTOR;
      var10002 = this.lang.get("gui.top_handlers_weekly");
      var10003 = new String[1];
      var10006 = String.valueOf(ChatColor.GRAY);
      var10003[0] = var10006 + this.lang.get("gui.top_handlers_weekly_desc");
      ItemStack weeklyBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
      inv.setItem(23, weeklyBtn);
      var10001 = Material.BOOKSHELF;
      var10002 = this.lang.get("gui.top_handlers_monthly");
      var10003 = new String[1];
      var10006 = String.valueOf(ChatColor.GRAY);
      var10003[0] = var10006 + this.lang.get("gui.top_handlers_monthly_desc");
      ItemStack monthlyBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
      inv.setItem(25, monthlyBtn);
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(45, backBtn);
      ItemStack closeBtn = this.createItem(Material.BARRIER, this.lang.get("gui.close"));
      inv.setItem(53, closeBtn);
      player.openInventory(inv);
   }

   public void openHandlersListMenu(Player player, String period) {
      String title;
      List<Map.Entry<String, Integer>> handlers;
      switch (period) {
         case "daily":
            title = this.lang.get("gui.handlers_daily_title");
            handlers = this.plugin.getDatabaseManager().getAllHandlersDaily(1);
            break;
         case "weekly":
            title = this.lang.get("gui.handlers_weekly_title");
            handlers = this.plugin.getDatabaseManager().getAllHandlersWeekly(7);
            break;
         case "monthly":
            title = this.lang.get("gui.handlers_monthly_title");
            handlers = this.plugin.getDatabaseManager().getAllHandlersMonthly(30);
            break;
         default:
            title = this.lang.get("gui.handlers_all_time_title");
            handlers = this.plugin.getDatabaseManager().getAllHandlers();
      }

      int size = 54;
      if (handlers.isEmpty()) {
         size = 27;
      } else if (handlers.size() <= 9) {
         size = 27;
      } else if (handlers.size() <= 18) {
         size = 36;
      } else if (handlers.size() <= 27) {
         size = 45;
      } else if (handlers.size() <= 36) {
         size = 54;
      }

      Inventory inv = Bukkit.createInventory(new GUIHolder("handlersList"), size, ChatColor.translateAlternateColorCodes('&', title));
      int slot = 0;

      for(Map.Entry<String, Integer> entry : handlers) {
         if (slot >= 54) {
            break;
         }

         ChatColor rankColor = slot == 0 ? ChatColor.GOLD : (slot == 1 ? ChatColor.GRAY : (slot == 2 ? ChatColor.RED : ChatColor.WHITE));
         Material var10003 = Material.PLAYER_HEAD;
         String var10004 = String.valueOf(rankColor) + "#" + (slot + 1) + " " + (String)entry.getKey();
         String[] var10005 = new String[1];
         String var10008 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.handlers_processed"));
         var10005[0] = var10008 + " " + String.valueOf(ChatColor.WHITE) + String.valueOf(entry.getValue());
         inv.setItem(slot, this.createItemWithLore(var10003, var10004, Arrays.asList(var10005)));
         ++slot;
      }

      if (handlers.isEmpty()) {
         inv.setItem(13, this.createItem(Material.BARRIER, this.lang.get("gui.no_handlers")));
      }

      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(size - 9, backBtn);
      ItemStack closeBtn = this.createItem(Material.BARRIER, this.lang.get("gui.close"));
      inv.setItem(size - 1, closeBtn);
      player.openInventory(inv);
   }

   public void openTopHandlersMenu(Player player, String period) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("topHandlers"), 54, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.top_handlers_title")));
      DatabaseManager db = this.plugin.getDatabaseManager();
      List<Map.Entry<String, Integer>> topHandlers = db.getTopHandlers(10);
      List<Map.Entry<String, Integer>> topHandlersDaily = db.getTopHandlersDaily(1, 10);
      List<Map.Entry<String, Integer>> topHandlersWeekly = db.getTopHandlersWeekly(7, 10);
      List<Map.Entry<String, Integer>> topHandlersMonthly = db.getTopHandlersMonthly(30, 10);
      Material var10001 = Material.NETHER_STAR;
      String var10002 = this.lang.get("gui.top_handlers_all_time");
      String[] var10003 = new String[1];
      String var10006 = String.valueOf(ChatColor.GRAY);
      var10003[0] = var10006 + this.lang.get("gui.top_handlers_all_time_desc");
      ItemStack allTimeBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
      inv.setItem(19, allTimeBtn);
      var10001 = Material.CLOCK;
      var10002 = this.lang.get("gui.top_handlers_daily");
      var10003 = new String[1];
      var10006 = String.valueOf(ChatColor.GRAY);
      var10003[0] = var10006 + this.lang.get("gui.top_handlers_daily_desc");
      ItemStack dailyBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
      inv.setItem(21, dailyBtn);
      var10001 = Material.DAYLIGHT_DETECTOR;
      var10002 = this.lang.get("gui.top_handlers_weekly");
      var10003 = new String[1];
      var10006 = String.valueOf(ChatColor.GRAY);
      var10003[0] = var10006 + this.lang.get("gui.top_handlers_weekly_desc");
      ItemStack weeklyBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
      inv.setItem(23, weeklyBtn);
      var10001 = Material.BOOKSHELF;
      var10002 = this.lang.get("gui.top_handlers_monthly");
      var10003 = new String[1];
      var10006 = String.valueOf(ChatColor.GRAY);
      var10003[0] = var10006 + this.lang.get("gui.top_handlers_monthly_desc");
      ItemStack monthlyBtn = this.createItemWithLore(var10001, var10002, Arrays.asList(var10003));
      inv.setItem(25, monthlyBtn);
      List<Map.Entry<String, Integer>> currentList;
      String periodName;
      switch (period) {
         case "daily":
            currentList = topHandlersDaily;
            periodName = this.lang.get("gui.top_handlers_daily");
            break;
         case "weekly":
            currentList = topHandlersWeekly;
            periodName = this.lang.get("gui.top_handlers_weekly");
            break;
         case "monthly":
            currentList = topHandlersMonthly;
            periodName = this.lang.get("gui.top_handlers_monthly");
            break;
         default:
            currentList = topHandlers;
            periodName = this.lang.get("gui.top_handlers_all_time");
      }

      Material var31 = Material.PAPER;
      String[] var10005 = new String[1];
      String var10008 = String.valueOf(ChatColor.GRAY);
      var10005[0] = var10008 + this.lang.get("gui.top_handlers_ranking");
      inv.setItem(4, this.createItemWithLore(var31, periodName, Arrays.asList(var10005)));
      int slot = 0;

      for(int i = 0; i < 45 && i < currentList.size(); ++i) {
         Map.Entry<String, Integer> entry = (Map.Entry)currentList.get(i);
         ChatColor rankColor = i == 0 ? ChatColor.GOLD : (i == 1 ? ChatColor.GRAY : (i == 2 ? ChatColor.RED : ChatColor.WHITE));
         var31 = Material.PLAYER_HEAD;
         String var10004 = String.valueOf(rankColor) + "#" + (i + 1) + " " + (String)entry.getKey();
         var10005 = new String[1];
         var10008 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.top_handlers_processed"));
         var10005[0] = var10008 + " " + String.valueOf(ChatColor.WHITE) + String.valueOf(entry.getValue());
         inv.setItem(slot, this.createItemWithLore(var31, var10004, Arrays.asList(var10005)));
         ++slot;
      }

      if (currentList.isEmpty()) {
         var31 = Material.BARRIER;
         String var34 = this.lang.get("gui.no_handlers");
         var10005 = new String[1];
         var10008 = String.valueOf(ChatColor.GRAY);
         var10005[0] = var10008 + periodName;
         inv.setItem(22, this.createItemWithLore(var31, var34, Arrays.asList(var10005)));
      }

      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(45, backBtn);
      ItemStack closeBtn = this.createItem(Material.BARRIER, this.lang.get("gui.close"));
      inv.setItem(53, closeBtn);
      player.openInventory(inv);
   }

   public void openMyReportsMenu(Player player) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("myReports"), 54, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.my_reports_title")));
      Map<Integer, UUID> slotMap = new HashMap();
      List<Report> playerReports = new ArrayList();

      for(Report report : this.reportManager.getAllReports()) {
         if (report.getReporterName().equalsIgnoreCase(player.getName())) {
            playerReports.add(report);
         }
      }

      if (playerReports.isEmpty()) {
         inv.setItem(22, this.createItemWithLore(Material.BARRIER, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.no_my_reports")), Collections.singletonList(ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.no_my_reports_desc")))));
      } else {
         int slot = 0;

         for(Report report : playerReports) {
            if (slot >= 45) {
               break;
            }

            ItemStack reportItem = this.createMyReportItem(report, slot + 1);
            inv.setItem(slot, reportItem);
            slotMap.put(slot, report.getId());
            ++slot;
         }
      }

      playerReportSlots.put(player.getUniqueId(), slotMap);
      ItemStack backBtn = this.createItem(Material.ARROW, this.lang.get("gui.back"));
      inv.setItem(45, backBtn);
      ItemStack closeBtn = this.createItem(Material.BARRIER, this.lang.get("gui.close"));
      inv.setItem(53, closeBtn);
      player.openInventory(inv);
   }

private ItemStack createMyReportItem(Report report, int index) {
       String statusKey = "gui.status_" + report.getStatus().name().toLowerCase();
       String status = ChatColor.translateAlternateColorCodes('&', this.lang.get(statusKey));
       String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(new Date(report.getTimestamp()));
       ChatColor statusColor = ChatColor.YELLOW;
       switch (report.getStatus()) {
            case ACCEPTED -> statusColor = ChatColor.GREEN;
            case REJECTED -> statusColor = ChatColor.RED;
            default -> statusColor = ChatColor.YELLOW;
       }

       ItemStack item = new ItemStack(Material.PAPER);
       ItemMeta meta = item.getItemMeta();
       meta.setDisplayName(String.valueOf(statusColor) + "(" + index + ") " + report.getReportedName());
       List<String> lore = new ArrayList();
       String var10001 = String.valueOf(statusColor);
       lore.add(var10001 + "■ " + ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.status")) + " " + status);
       lore.add("");
       var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reason"));
       lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getReason());
       var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.date"));
       lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + date);
       if (report.getHandledBy() != null && !report.getHandledBy().isEmpty()) {
          var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.handled_by"));
          lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getHandledBy());
       }

       if (report.getStatus() == Report.ReportStatus.ACCEPTED && this.rewardManager != null && this.rewardManager.isEnabled()) {
          lore.add("");
          if (report.isRewardClaimed()) {
             lore.add(ChatColor.GREEN + "✓ " + ChatColor.translateAlternateColorCodes('&', this.lang.get("reward.claimed")));
          } else {
             lore.add(ChatColor.GOLD + "★ " + ChatColor.translateAlternateColorCodes('&', this.lang.get("reward.claim_click")));
          }
       }

       lore.add("");
       lore.add(ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.click_view")));
meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

public void openMyReportDetail(Player player, Report report) {
       Inventory inv = Bukkit.createInventory(new GUIHolder("myReportDetail"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.my_report_detail_title")));
       String statusKey = "gui.status_" + report.getStatus().name().toLowerCase();
       String status = ChatColor.translateAlternateColorCodes('&', this.lang.get(statusKey));
       String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(new Date(report.getTimestamp()));
       Material var10001 = Material.PAPER;
       String var10002 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.my_report_detail_title"));
       String[] var10005 = new String[]{"", null, null, null, null, null, null};
       String var10008 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reported"));
       var10005[1] = var10008 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getReportedName();
       var10008 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reason"));
       var10005[2] = var10008 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getReason();
       var10008 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.evidence"));
       var10005[3] = var10008 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + (report.getEvidence() != null ? report.getEvidence() : "-");
       var10008 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.date"));
       var10005[4] = var10008 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + date;
       var10008 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.status"));
       var10005[5] = var10008 + String.valueOf(ChatColor.DARK_GRAY) + " » " + status;
       var10005[6] = report.getHandledBy() != null && !report.getHandledBy().isEmpty() ? ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.handled_by")) + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getHandledBy() : "";
       ItemStack info = this.createItemWithLore(var10001, var10002, new ArrayList(Arrays.asList(var10005)));
       inv.setItem(13, info);
       
       Map<Integer, UUID> slotMap = new HashMap();
       slotMap.put(13, report.getId());
       playerReportSlots.put(player.getUniqueId(), slotMap);
       
       if (report.getStatus() == Report.ReportStatus.ACCEPTED && this.rewardManager != null && this.rewardManager.isEnabled()) {
          if (!report.isRewardClaimed()) {
             ItemStack claimBtn = this.createItem(Material.DIAMOND, ChatColor.translateAlternateColorCodes('&', this.lang.get("reward.claim_button")));
             inv.setItem(11, claimBtn);
          } else {
             ItemStack claimedBtn = this.createItem(Material.EMERALD, ChatColor.translateAlternateColorCodes('&', this.lang.get("reward.claimed_button")));
             inv.setItem(11, claimedBtn);
          }
       }
       
       inv.setItem(18, this.createItem(Material.ARROW, this.lang.get("gui.back")));
       inv.setItem(26, this.createItem(Material.BARRIER, this.lang.get("gui.close")));
       player.openInventory(inv);
    }

   public void openReportInfo(Player player, Report report) {
      Inventory inv = Bukkit.createInventory(new GUIHolder("reportInfo"), 27, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.report_info")));
      String statusKey = "gui.status_" + report.getStatus().name().toLowerCase();
      String status = ChatColor.translateAlternateColorCodes('&', this.lang.get(statusKey));
      String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(new Date(report.getTimestamp()));
      List<String> lore = new ArrayList();
      lore.add("");
      String var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reporter"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getReporterName());
      var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reported"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getReportedName());
      var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reason"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getReason());
      var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.evidence"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + (report.getEvidence() != null ? report.getEvidence() : "-"));
      var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.date"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + date);
      var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.status"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + status);
      if (report.getHandledBy() != null && !report.getHandledBy().isEmpty()) {
         var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.handled_by"));
         lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getHandledBy());
      }

      ItemStack info = this.createItemWithLore(Material.PAPER, ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.report_info_title")), lore);
      inv.setItem(13, info);
      if (report.getStatus() == ReportStatus.PENDING) {
         inv.setItem(10, this.createItem(Material.EMERALD_BLOCK, this.lang.get("gui.accept")));
         inv.setItem(11, this.createItem(Material.REDSTONE_BLOCK, this.lang.get("gui.reject")));
      }

      inv.setItem(14, this.createItem(Material.ENDER_PEARL, this.lang.get("gui.teleport")));
      Player reportedPlayer = Bukkit.getPlayer(report.getReportedName());
      boolean isFrozen = reportedPlayer != null && this.plugin.isFrozen(reportedPlayer);
      if (isFrozen) {
         Material var10003 = Material.PACKED_ICE;
         String var10004 = this.lang.get("gui.unfreeze");
         String[] var10005 = new String[1];
         String var10008 = String.valueOf(ChatColor.GRAY);
         var10005[0] = var10008 + this.lang.get("gui.freeze_status_active");
         inv.setItem(15, this.createItemWithLore(var10003, var10004, Arrays.asList(var10005)));
      } else {
         Material var17 = Material.ICE;
         String var18 = this.lang.get("gui.freeze");
         String[] var19 = new String[1];
         String var20 = String.valueOf(ChatColor.GRAY);
         var19[0] = var20 + this.lang.get("gui.freeze_status_inactive");
         inv.setItem(15, this.createItemWithLore(var17, var18, Arrays.asList(var19)));
      }

      inv.setItem(18, this.createItem(Material.ARROW, this.lang.get("gui.back")));
      inv.setItem(26, this.createItem(Material.BARRIER, this.lang.get("gui.close")));
      Map<Integer, UUID> slotMap = new HashMap();
      slotMap.put(0, report.getId());
      playerReportSlots.put(player.getUniqueId(), slotMap);
      player.openInventory(inv);
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         Player player = (Player)e.getWhoClicked();
         InventoryHolder holder = e.getInventory().getHolder();
         if (holder instanceof GUIHolder) {
            GUIHolder guiHolder = (GUIHolder)holder;
            e.setCancelled(true);
            switch (guiHolder.getType()) {
               case "main":
                  if (e.getSlot() == 11) {
                     this.openPlayerSelect(player);
                  } else if (e.getSlot() == 15) {
                     this.openMyReportsMenu(player);
                  }
                  break;
               case "playerSelect":
                  if (e.getCurrentItem() != null) {
                     if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                        String targetName = e.getCurrentItem().getItemMeta().getDisplayName();
                        targetName = ChatColor.stripColor(targetName);
                        Player target = Bukkit.getPlayer(targetName);
                        if (target != null && !target.getName().equals(player.getName())) {
                           if (this.reportManager.hasPendingReport(player.getName(), target.getName())) {
                              String var53 = this.lang.getPrefix();
                              player.sendMessage(ChatColor.translateAlternateColorCodes('&', var53 + this.lang.get("gui.already_pending")));
                              return;
                           }

                           this.openReasonSelect(player, target.getName());
                        } else {
                           String var52 = this.lang.getPrefix();
                           player.sendMessage(ChatColor.translateAlternateColorCodes('&', var52 + this.lang.get("gui.player_not_found")));
                        }
                     } else if (e.getCurrentItem().getType() == Material.NAME_TAG) {
                        this.openSearchMenu(player);
                     } else if (e.getCurrentItem().getType() == Material.ARROW) {
                        this.openMainMenu(player);
                     } else if (e.getCurrentItem().getType() == Material.BARRIER) {
                        player.closeInventory();
                     }
                  }
                  break;
               case "searchPlayer":
                  if (e.getCurrentItem() != null) {
                     if (e.getCurrentItem().getType() == Material.PAPER) {
                        player.closeInventory();
                        if (this.chatInputListener != null) {
                           this.chatInputListener.setAwaitingSearch(player);
                        }

                        String var51 = this.lang.getPrefix();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', var51 + this.lang.get("gui.write_playername")));
                     } else if (e.getCurrentItem().getType() == Material.ARROW) {
                        if (this.chatInputListener != null) {
                           this.chatInputListener.cancelAwaitingSearch(player);
                        }

                        this.openPlayerSelect(player);
                     }
                  }
                  break;
               case "settings":
                  if (e.getCurrentItem() != null) {
                     if (e.getSlot() == 11) {
                        this.plugin.getConfig().set("language", "tr");
                        this.plugin.saveConfig();
                        this.lang.setLanguage("tr");
                        String var49 = this.lang.getPrefix();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', var49 + this.lang.get("gui.tr_selected")));
                        this.openSettingsMenu(player);
                     } else if (e.getSlot() == 13) {
                        this.plugin.getConfig().set("language", "en");
                        this.plugin.saveConfig();
                        this.lang.setLanguage("en");
                        String var50 = this.lang.getPrefix();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', var50 + this.lang.get("gui.en_selected")));
                        this.openSettingsMenu(player);
                     } else if (e.getCurrentItem().getType() == Material.ARROW) {
                        this.openMainMenu(player);
                     }
                  }
                  break;
               case "reasonSelect":
                  if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.ARROW && e.getCurrentItem().getType() != Material.BARRIER) {
                     ReportData data = (ReportData)playerData.get(player.getUniqueId());
                     if (data != null) {
                        String reason = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                        data.setReason(reason);
                        this.openEvidenceInput(player);
                     }
                  } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {
                     this.openPlayerSelect(player);
                  } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BARRIER) {
                     player.closeInventory();
                     playerData.remove(player.getUniqueId());
                  }
                  break;
               case "evidenceInput":
                  if (e.getCurrentItem() != null) {
                     if (e.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                        ReportData data = (ReportData)playerData.get(player.getUniqueId());
                        if (data != null) {
                           if (!this.reportManager.canCreateReport(player.getName())) {
                              int maxReports = this.plugin.getConfig().getInt("max-reports-per-day", 5);
                              String var47 = this.lang.getPrefix();
                              player.sendMessage(ChatColor.translateAlternateColorCodes('&', var47 + this.lang.get("gui.max_reports_reached", Collections.singletonMap("%max%", String.valueOf(maxReports)))));
                              player.closeInventory();
                              return;
                           }

this.reportManager.createReport(player.getName(), data.getTargetName(), data.getReason(), data.getEvidence());
                            if (this.webhookManager != null) {
                               this.webhookManager.sendNewReportMessage(player.getName(), data.getTargetName(), data.getReason(), data.getEvidence());
                            }
                            String var46 = this.lang.getPrefix();
                           player.sendMessage(ChatColor.translateAlternateColorCodes('&', var46 + this.lang.get("gui.report_sent")));
                           playerData.remove(player.getUniqueId());
                           player.closeInventory();
                        }
                     } else if (e.getCurrentItem().getType() == Material.ARROW) {
                        if (this.chatInputListener != null) {
                           this.chatInputListener.cancelAwaitingEvidence(player);
                        }

                        this.openReasonSelect(player, playerData.get(player.getUniqueId()) != null ? ((ReportData)playerData.get(player.getUniqueId())).getTargetName() : null);
                     } else if (e.getCurrentItem().getType() == Material.PAPER) {
                        player.closeInventory();
                        if (this.chatInputListener != null) {
                           this.chatInputListener.setAwaitingEvidence(player);
                        }

                        String var48 = this.lang.getPrefix();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', var48 + this.lang.get("gui.write_evidence")));
                     }
                  }
                  break;
               case "confirmReport":
                  if (e.getCurrentItem() != null) {
                     if (e.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                        ReportData data = (ReportData)playerData.get(player.getUniqueId());
                        if (data != null) {
                           if (!this.reportManager.canCreateReport(player.getName())) {
                              int maxReports = this.plugin.getConfig().getInt("max-reports-per-day", 5);
                              String var45 = this.lang.getPrefix();
                              player.sendMessage(ChatColor.translateAlternateColorCodes('&', var45 + this.lang.get("gui.max_reports_reached", Collections.singletonMap("%max%", String.valueOf(maxReports)))));
                              player.closeInventory();
                              return;
                           }

this.reportManager.createReport(player.getName(), data.getTargetName(), data.getReason(), data.getEvidence());
                            if (this.webhookManager != null) {
                               this.webhookManager.sendNewReportMessage(player.getName(), data.getTargetName(), data.getReason(), data.getEvidence());
                            }
                            String var44 = this.lang.getPrefix();
                           player.sendMessage(ChatColor.translateAlternateColorCodes('&', var44 + this.lang.get("gui.report_sent")));
                           playerData.remove(player.getUniqueId());
                        }
                     } else if (e.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
                        playerData.remove(player.getUniqueId());
                     }

                     player.closeInventory();
                  }
                  break;
               case "admin":
                  if (e.getCurrentItem() != null) {
                     int clickedSlot = e.getSlot();
                     Map<Integer, UUID> slotMap = (Map)playerReportSlots.getOrDefault(player.getUniqueId(), new HashMap());
                     if (slotMap.containsKey(clickedSlot)) {
                        UUID reportId = (UUID)slotMap.get(clickedSlot);
                        Report report = this.reportManager.getReport(reportId);
                        if (report != null) {
                           this.openReportInfo(player, report);
                        }
                     } else if (e.getCurrentItem().getType() == Material.NAME_TAG) {
                        this.openAdminSearchMenu(player);
                     } else if (e.getCurrentItem().getType() == Material.YELLOW_STAINED_GLASS_PANE) {
                        this.openAdminMenu(player, (String)null, "PENDING", (String)null);
                     } else if (e.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
                        this.openAdminMenu(player, (String)null, "ACCEPTED", (String)null);
                     } else if (e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
                        this.openAdminMenu(player, (String)null, "REJECTED", (String)null);
                     } else if (e.getSlot() == 28) {
                        this.openAdminMenu(player, (String)null, (String)null, "newest");
                     } else if (e.getSlot() == 34) {
                        this.openAdminMenu(player, (String)null, (String)null, "oldest");
                     } else if (e.getSlot() != 45 && e.getCurrentItem().getType() != Material.ARROW) {
                        if (e.getCurrentItem().getType() == Material.BARRIER) {
                           player.closeInventory();
                        } else if (e.getCurrentItem().getType() == Material.NETHER_STAR) {
                           this.openStatsMenu(player);
                        }
                     } else {
                        player.closeInventory();
                     }
                  }
                  break;
               case "adminSearch":
                  if (e.getCurrentItem() != null) {
                     if (e.getCurrentItem().getType() == Material.PAPER) {
                        player.closeInventory();
                        if (this.chatInputListener != null) {
                           this.chatInputListener.setAwaitingAdminSearch(player);
                        }

                        String var43 = this.lang.getPrefix();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', var43 + this.lang.get("gui.write_search")));
                     } else if (e.getCurrentItem().getType() == Material.ARROW) {
                        if (this.chatInputListener != null) {
                           this.chatInputListener.cancelAwaitingAdminSearch(player);
                        }

                        this.openAdminMenu(player);
                     }
                  }
                  break;
case "myReports":
                   if (e.getCurrentItem() != null) {
                      int clickedSlot = e.getSlot();
                      Map<Integer, UUID> slotMap = (Map)playerReportSlots.getOrDefault(player.getUniqueId(), new HashMap());
                      if (slotMap.containsKey(clickedSlot)) {
                         UUID reportId = (UUID)slotMap.get(clickedSlot);
                         Report report = this.reportManager.getReport(reportId);
                         if (report != null) {
                            if (report.getStatus() == Report.ReportStatus.ACCEPTED && 
                                this.rewardManager != null && 
                                this.rewardManager.isEnabled() &&
                                !report.isRewardClaimed()) {
                               this.reportManager.setRewardClaimed(reportId, true);
                               this.rewardManager.giveReward(player);
                               this.openMyReportsMenu(player);
                            } else if (report.getStatus() == Report.ReportStatus.ACCEPTED && 
                                     this.rewardManager != null && 
                                     this.rewardManager.isEnabled() &&
                                     report.isRewardClaimed()) {
                               player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.lang.getPrefix() + this.lang.get("reward.already_claimed")));
                               this.openMyReportDetail(player, report);
                            } else {
                               this.openMyReportDetail(player, report);
                            }
                         }
                      } else if (e.getCurrentItem().getType() == Material.ARROW) {
                         this.openMainMenu(player);
                      } else if (e.getCurrentItem().getType() == Material.BARRIER) {
                         player.closeInventory();
                      }
                   }
                   break;
case "myReportDetail":
                   if (e.getCurrentItem() != null) {
                      if (e.getCurrentItem().getType() == Material.DIAMOND) {
                         Map<Integer, UUID> slotMap = (Map)playerReportSlots.getOrDefault(player.getUniqueId(), new HashMap());
                         UUID reportId = (UUID)slotMap.get(13);
                         if (reportId != null) {
                            Report report = this.reportManager.getReport(reportId);
                            if (report != null && !report.isRewardClaimed()) {
                               this.reportManager.setRewardClaimed(reportId, true);
                               this.rewardManager.giveReward(player);
                               this.openMyReportDetail(player, report);
                            } else {
                               player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.lang.getPrefix() + this.lang.get("reward.already_claimed")));
                            }
                         }
                      } else if (e.getCurrentItem().getType() == Material.ARROW) {
                         this.openMyReportsMenu(player);
                      } else if (e.getCurrentItem().getType() == Material.BARRIER) {
                         player.closeInventory();
                      }
                   }
                   break;
               case "stats":
                  if (e.getCurrentItem() != null) {
                     if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                        this.openTopHandlersMenu(player);
                     } else if (e.getCurrentItem().getType() == Material.ARROW) {
                        this.openAdminMenu(player);
                     } else if (e.getCurrentItem().getType() == Material.BARRIER) {
                        player.closeInventory();
                     }
                  }
                  break;
               case "topHandlers":
                  if (e.getCurrentItem() != null) {
                     Material mat = e.getCurrentItem().getType();
                     if (mat == Material.NETHER_STAR) {
                        this.openHandlersListMenu(player, "alltime");
                     } else if (mat == Material.CLOCK) {
                        this.openHandlersListMenu(player, "daily");
                     } else if (mat == Material.DAYLIGHT_DETECTOR) {
                        this.openHandlersListMenu(player, "weekly");
                     } else if (mat == Material.BOOKSHELF) {
                        this.openHandlersListMenu(player, "monthly");
                     } else if (mat == Material.ARROW) {
                        this.openStatsMenu(player);
                     } else if (mat == Material.BARRIER) {
                        player.closeInventory();
                     }
                  }
                  break;
               case "reportInfo":
                  if (e.getCurrentItem() != null) {
                     Material mat = e.getCurrentItem().getType();
                     int slot = e.getSlot();
                     Map<Integer, UUID> slotMap = (Map)playerReportSlots.getOrDefault(player.getUniqueId(), new HashMap());
                     UUID reportIdUUID = (UUID)slotMap.get(0);
                     if (mat != Material.ARROW && slot != 18) {
                        if (mat != Material.BARRIER && slot != 26) {
                           if (mat != Material.ICE && mat != Material.PACKED_ICE && slot != 15) {
                              if (mat != Material.EMERALD_BLOCK && slot != 10) {
                                 if (mat != Material.REDSTONE_BLOCK && slot != 11) {
                                    if (mat == Material.ENDER_PEARL || slot == 14) {
                                       if (reportIdUUID != null) {
                                          Report report = this.reportManager.getReport(reportIdUUID);
                                          if (report != null) {
                                             String reportedPlayerName = report.getReportedName();
                                             Player target = Bukkit.getPlayer(reportedPlayerName);
                                             if (target != null) {
                                                player.teleport(target.getLocation());
                                                player.setGameMode(GameMode.SPECTATOR);
                                                String var41 = this.lang.getPrefix();
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', var41 + this.lang.get("gui.teleported")));
                                             } else {
                                                String var42 = this.lang.getPrefix();
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', var42 + this.lang.get("gui.player_offline", Collections.singletonMap("%player%", reportedPlayerName))));
                                             }
                                          }
                                       }

                                       player.closeInventory();
                                    }
} else if (reportIdUUID != null) {
                                     Report report = this.reportManager.getReport(reportIdUUID);
                                     if (report != null) {
                                        this.reportManager.rejectReport(reportIdUUID, player.getName());
                                        if (this.webhookManager != null) {
                                           this.webhookManager.sendReportRejectedMessage(report.getReporterName(), report.getReportedName(), report.getReason(), player.getName());
                                        }
                                        String var40 = this.lang.getPrefix();
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', var40 + this.lang.get("gui.report_rejected")));
                                        this.openReportInfo(player, this.reportManager.getReport(reportIdUUID));
                                     }
                                  }
} else if (reportIdUUID != null) {
Report report = this.reportManager.getReport(reportIdUUID);
                                   if (report != null) {
                                      this.reportManager.acceptReport(reportIdUUID, player.getName());
                                      if (this.webhookManager != null) {
                                         this.webhookManager.sendReportAcceptedMessage(report.getReporterName(), report.getReportedName(), report.getReason(), player.getName());
                                      }
                                      String var39 = this.lang.getPrefix();
                                      player.sendMessage(ChatColor.translateAlternateColorCodes('&', var39 + this.lang.get("gui.report_accepted")));
                                      this.openReportInfo(player, this.reportManager.getReport(reportIdUUID));
                                   }
                               }
                           } else if (reportIdUUID != null) {
                              Report report = this.reportManager.getReport(reportIdUUID);
                              if (report != null) {
                                 String reportedPlayerName = report.getReportedName();
                                 Player target = Bukkit.getPlayer(reportedPlayerName);
                                 if (target != null) {
                                    if (this.plugin.isFrozen(target)) {
                                       this.plugin.unfreezePlayer(target);
                                       String var10002 = this.lang.getPrefix();
                                       player.sendMessage(ChatColor.translateAlternateColorCodes('&', var10002 + this.lang.get("gui.unfreeze_success", Collections.singletonMap("%player%", reportedPlayerName))));
                                    } else {
                                       this.plugin.freezePlayer(target);
                                       String var37 = this.lang.getPrefix();
                                       player.sendMessage(ChatColor.translateAlternateColorCodes('&', var37 + this.lang.get("gui.freeze_success", Collections.singletonMap("%player%", reportedPlayerName))));
                                    }

                                    this.openReportInfo(player, report);
                                 } else {
                                    String var38 = this.lang.getPrefix();
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', var38 + this.lang.get("gui.player_offline")));
                                 }
                              }
                           }
                        } else {
                           player.closeInventory();
                        }
                     } else {
                        this.openAdminMenu(player);
                     }
                  }
                  break;
               case "handlersList":
                  if (e.getCurrentItem() != null) {
                     if (e.getCurrentItem().getType() == Material.ARROW) {
                        this.openTopHandlersMenu(player);
                     } else if (e.getCurrentItem().getType() == Material.BARRIER) {
                        player.closeInventory();
                     }
                  }
            }

         }
      }
   }

   private ItemStack createItem(Material material, String name) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createItemWithLore(Material material, String name, List<String> lore) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
      List<String> coloredLore = new ArrayList();

      for(String line : lore) {
         coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
      }

      meta.setLore(coloredLore);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createPlayerHead(Player player) {
      ItemStack head = new ItemStack(Material.PLAYER_HEAD);
      ItemMeta meta = head.getItemMeta();
      meta.setDisplayName(player.getName());
      head.setItemMeta(meta);
      return head;
   }

   private ItemStack createReportItem(Report report, int index) {
      String statusKey = "gui.status_" + report.getStatus().name().toLowerCase();
String status = ChatColor.translateAlternateColorCodes('&', this.lang.get(statusKey));
      ChatColor statusColor = ChatColor.YELLOW;
      switch (report.getStatus()) {
           case ACCEPTED -> statusColor = ChatColor.GREEN;
           case REJECTED -> statusColor = ChatColor.RED;
           default -> statusColor = ChatColor.YELLOW;
      }

      ItemStack item = new ItemStack(Material.PAPER);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(String.valueOf(statusColor) + "(" + index + ") " + report.getReportedName());
      String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(new Date(report.getTimestamp()));
      List<String> lore = new ArrayList();
      String var10001 = String.valueOf(statusColor);
      lore.add(var10001 + "■ " + ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.status")) + " " + status);
      lore.add("");
      var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reporter"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getReporterName());
      var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.reason"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getReason());
      var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.date"));
      lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + date);
      if (report.getHandledBy() != null && !report.getHandledBy().isEmpty()) {
         var10001 = ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.handled_by"));
         lore.add(var10001 + String.valueOf(ChatColor.DARK_GRAY) + " » " + String.valueOf(ChatColor.WHITE) + report.getHandledBy());
      }

      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', this.lang.get("gui.click_view")));
      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   public void setEvidence(Player player, String evidence) {
      ReportData data = (ReportData)playerData.get(player.getUniqueId());
      if (data != null) {
         data.setEvidence(evidence);
         this.openConfirmReport(player);
      }

   }

   public void setEvidenceFromChat(Player player, String evidence) {
      ReportData data = (ReportData)playerData.get(player.getUniqueId());
      if (data != null) {
         data.setEvidence(evidence);
         this.openConfirmReport(player);
      }

   }

public boolean hasPendingReport(Player player, String targetName) {
       return this.reportManager.hasPendingReport(player.getName(), targetName);
    }

    public static class GUIHolder implements InventoryHolder {
        private final String type;
        
        public GUIHolder(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
        
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static class ReportData {
        private String targetName;
        private String reason;
        private String evidence;
        
        public String getTargetName() {
            return targetName;
        }
        
        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        public String getEvidence() {
            return evidence;
        }
        
        public void setEvidence(String evidence) {
            this.evidence = evidence;
        }
    }
}
