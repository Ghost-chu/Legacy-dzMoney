package com.mcsunnyside.dzmoney;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import lib.PatPeter.SQLibrary.Database;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Main extends org.bukkit.plugin.java.JavaPlugin
{
	private Database sql;
	private Economy economy;
	private int money;
	private ResultSet result;

	String Prefix = ChatColor.GOLD+"["+ChatColor.AQUA+"dzMoney"+ChatColor.GOLD+"] ";
	String UID_Table = getConfig().getString("mysql.uid_table");
	String Members_Table = getConfig().getString("mysql.members_table");
	String Members_Count_Table = getConfig().getString("mysql.members_count_table");
	String Credit_Table = getConfig().getString("mysql.credit");
	String Username_Table = getConfig().getString("mysql.username_table");
	String CoinName = getConfig().getString("msg.coinName");
	String ForumName = getConfig().getString("msg.forumName");
	public void onEnable()
	{
		getLogger().info(">>dzMoney 重制版 by Ghost_chu&阳光之城社区 插件原作者:john180");
		getLogger().info(">>阳光之城社区汉化 http://www.mcsunnyside.com");
		getLogger().info(">>向原作者致敬! 原帖地址:http://www.mcbbs.net/thread-385078-1-1.html");
		setupEconomy();
		saveDefaultConfig();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String pname;
		if (cmd.getName().equalsIgnoreCase("dzm")) {
			if (args.length == 0) {
				if (!(sender instanceof org.bukkit.entity.Player)) {
					pname = "Ghost_chu";
				} else {
					pname = sender.getName();
				}
				if (sender.hasPermission("dzMoney.use")) {
					mysqlConnection();
					if (!this.sql.isOpen()) {
						sender.sendMessage(Prefix + ChatColor.RED + "无法连接至数据库");
						return true;
					}
					try {
						Bukkit.getConsoleSender().sendMessage("select " + UID_Table + " from " + Members_Table
								+ " where " + Username_Table + "='" + pname + "'");
						this.result = this.sql.query("select " + UID_Table + " from " + Members_Table + " where "
								+ Username_Table + "='" + pname + "'");
						if (!this.result.next()) {
							sender.sendMessage(Prefix + ChatColor.RED + "无法获取到玩家信息");
							return true;
						}
						int uid = this.result.getInt(UID_Table);
						Bukkit.getConsoleSender().sendMessage("select " + Credit_Table + " from " + Members_Count_Table
								+ " where " + UID_Table + "='" + uid + "'");
						this.result = this.sql.query("select " + Credit_Table + " from " + Members_Count_Table
								+ " where " + UID_Table + "='" + uid + "'");
						this.result.next();
						this.money = this.result.getInt(Credit_Table);
						int finalMoney = money * getConfig().getInt("control.boost");
						sender.sendMessage(Prefix + ChatColor.DARK_GREEN + ForumName + CoinName + "余额为:"
								+ ChatColor.YELLOW + String.valueOf(this.money));
						if(getConfig().getInt("control.boost")!=1) {
							sender.sendMessage("此服务器已启动导入翻倍功能，翻倍 x"+getConfig().getInt("control.boost"));
						}
						sender.sendMessage(Prefix + ChatColor.DARK_GREEN + "正在处理" + ForumName + CoinName + "数据…");
						this.sql.query("UPDATE " + Members_Count_Table + " SET " + Credit_Table + "=0 where "
								+ UID_Table + "='" + uid + "'");
						sender.sendMessage(Prefix + ChatColor.DARK_GREEN + "正在处理游戏" + CoinName + "数据…");
						this.economy.depositPlayer(sender.getName(), finalMoney);
						sender.sendMessage(Prefix + ChatColor.YELLOW + String.valueOf(finalMoney) + ChatColor.DARK_GREEN
								+ CoinName + "已导入账户,当前账户余额为:" + ChatColor.YELLOW + this.economy.getBalance(pname));
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return true;
				}
				sender.sendMessage(Prefix + ChatColor.RED + "你没有权限执行此指令");
				return true;
			}

			if ((args.length == 1) && (args[0].equalsIgnoreCase("reload"))) {
				if (sender.hasPermission("dzmoney.reload")) {
					reloadConfig();
					getServer().getPluginManager().getPlugin("dzMoney").onEnable();
					Prefix = ChatColor.GOLD + "[" + ChatColor.AQUA + "dzMoney" + "]";
					UID_Table = getConfig().getString("mysql.uid_table");
					Members_Table = getConfig().getString("mysql.members_table");
					Members_Count_Table = getConfig().getString("mysql.members_count_table");
					Credit_Table = getConfig().getString("mysql.credit");
					Username_Table = getConfig().getString("mysql.username_table");
					CoinName = getConfig().getString("msg.coinName");
					ForumName = getConfig().getString("msg.forumName");
					sender.sendMessage(Prefix + ChatColor.DARK_GREEN + "插件已重载");
					return true;
				}
				sender.sendMessage(Prefix + ChatColor.RED + "你没有权限执行此指令");
				return true;
			}
		}

		return false;
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			this.economy = ((Economy)economyProvider.getProvider());
		}
		return this.economy != null;
	}

	public void mysqlConnection() { this.sql = new lib.PatPeter.SQLibrary.MySQL(getLogger(), "[dzMoney] ", getConfig().getString("mysql.host"), getConfig().getInt("mysql.port"), getConfig().getString("mysql.database"), getConfig().getString("mysql.user"), getConfig().getString("mysql.password"));
	if (this.sql.open())
		try {
			this.result = this.sql.query("SHOW TABLES LIKE '"+Members_Table+"'");
			if (!this.result.next()) {
				getLogger().info("指定数据表不存在请检查数据库是否正确");
				this.sql.close();
				return;
			}
		} catch (SQLException e) {
			getLogger().info(e.getMessage());

			getLogger().info("插件启动成功");
		}
	getLogger().info("无法连接到数据库");
	}
}