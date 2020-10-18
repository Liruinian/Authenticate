package com.timlrn2016.authenticate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.timlrn2016.authenticate.Filter.ConsoleFilter;


public class Authenticate extends JavaPlugin implements Listener {
	
	public static String Prefix;
	public static Integer MaxAmount;
	public static String Address;
	public static String Success_message;
	public static List<String> Success_Commands;
	public static String Error_notaplayer;
	public static String Error_malformedURL;
	public static String Error_errorpassword;
	public static String Error_errorencoding;
	public static String Error_errorname;
	public static String Error_maxamount;
	public static List<String> Error_command;
	public static String Error_successed;
	
	ArrayList<String> name = new ArrayList<String>();

	public void LoadConfig() {
		Bukkit.getPluginManager().registerEvents(this, this);
		File file = new File(getDataFolder(), "config.yml");
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		if (!file.exists()) {
			getServer().getConsoleSender().sendMessage("§6[正版认证]未发现配置文件..正在新建一个配置文件");
			getServer().getConsoleSender().sendMessage("§6提示：如果您的提示为乱码 请将文件夹中的config.yml转为ansi编码");
			this.saveDefaultConfig();
		}
		this.reloadConfig();
		ConsoleFilter.setupConsoleFilter();
		Bukkit.getPluginManager().registerEvents(this, this);
		
		FileConfiguration config = getConfig();
		Prefix = config.getString("Prefix").replace("&", "§");
		MaxAmount = config.getInt("MaxAmount");
		Address = config.getString("Address").replace("&", "§") ;
		Success_message = config.getString("Success.message").replace("&", "§") ;
		Success_Commands = config.getStringList("Success.commands");
		Error_notaplayer = config.getString("Error.notaplayer").replace("&", "§") ;
		Error_malformedURL = config.getString("Error.malformedURL").replace("&", "§") ;
		Error_errorpassword = config.getString("Error.errorpassword").replace("&", "§");
		Error_errorencoding = config.getString("Error.errorencoding").replace("&", "§");
		Error_errorname = config.getString("Error.errorname").replace("&", "§");
		Error_maxamount = config.getString("Error.maxamount").replace("&", "§");
		Error_command = config.getStringList("Error.command");
		Error_successed = config.getString("Error.successed").replace("&", "§");
		
		getServer().getConsoleSender().sendMessage("§2[正版认证]成功加载正版认证插件 Made By Tim_LRN2016");

	}
	public void onEnable() {
		LoadConfig();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		Player sender = e.getPlayer();
		if (e.getMessage().toLowerCase().startsWith("/zb ")) {
			String[] fg = e.getMessage().split(" ");
			File f = new File(getDataFolder() + "\\Players\\", e.getPlayer().getName() + ".yml");
			FileConfiguration data = YamlConfiguration.loadConfiguration(f);
			if (fg.length == 0 || fg.length <= 2) {
				return;
			}
			if (data.getBoolean("Success")) {
				e.setCancelled(true);
				sender.sendMessage(Prefix+ Error_successed);
				return;
			}
			if (getAmount(sender.getName()) >= MaxAmount) {
				return;
			}
			sender.sendMessage(Prefix + "§a正在连接认证服务器中...");
			try {
				postMojang(e.getPlayer(), fg[1], fg[2]);
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String lable, String args[]) {
		if (lable.equalsIgnoreCase("zb")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Prefix + Error_notaplayer);
					return true;
				}
				if (args.length == 0) {
					for (String i : Error_command) {
						sender.sendMessage(i.replace("%amount%", "" + MaxAmount));
					}
					return true;
				}
				if (args[0].equalsIgnoreCase("reload")) {
					LoadConfig();
					sender.sendMessage("§2[正版认证]成功重载正版认证插件 Made By Tim_LRN2016");
					getServer().getConsoleSender().sendMessage("§2[正版认证]成功重载正版认证插件 Made By Tim_LRN2016");
			}
				if (getAmount(sender.getName()) >= MaxAmount) {
					sender.sendMessage(Prefix + Error_maxamount);
					return true;
				}
		}
		return false;
	}

	@SuppressWarnings({ "unchecked" })
	public void postMojang(Player p, String email, String pass) throws ParseException {
		new BukkitRunnable() {
			@Override
			public void run() {
				String url_post = Address;
				try {
					URL url = new URL(url_post);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setUseCaches(false);
					connection.setInstanceFollowRedirects(true);
					connection.setRequestProperty("Content-Type", "application/json");
					connection.setDoOutput(true);
					connection.setDoInput(true);
					connection.setRequestMethod("POST");
					connection.connect();

					DataOutputStream out = new DataOutputStream(connection.getOutputStream());
					JSONObject obj = new JSONObject();
					obj.put("username", email);
					obj.put("password", pass);
					JSONObject obj2 = new JSONObject();
					obj2.put("name", "Minecraft");
					obj2.put("version", 1);
					obj.put("agent", obj2);

					out.writeBytes(obj.toString());
					out.flush();
					out.close();

					BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String lines;
					StringBuffer stbu = new StringBuffer("");
					while ((lines = reader.readLine()) != null) {
						lines = new String(lines.getBytes(), "UTF-8");
						stbu.append(lines);
					}
					String i = stbu.toString();
					JSONObject x = (JSONObject) (new JSONParser().parse(i));
					String m = x.get("selectedProfile").toString();
					JSONObject y = (JSONObject) (new JSONParser().parse(m));
					if (p.getName().equals(y.get("name"))) {
						addAmount(p.getName());
						p.sendMessage(Prefix+ Success_message);
						File f = new File(getDataFolder() + "\\Players\\", p.getName() + ".yml");
						FileConfiguration data = YamlConfiguration.loadConfiguration(f);
						data.set("Success", true);
						data.save(f);
						for (String c : Success_Commands) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
									c.replace("&", "§").replace("%player%", p.getName()));
						}
					} else {
						addAmount(p.getName());
						p.sendMessage(Prefix + Error_errorname);
					}
					reader.close();
					connection.disconnect();
				} catch (MalformedURLException e) {
					p.sendMessage(Prefix +Error_malformedURL.replace("%amount%","" + (MaxAmount - getAmount(p.getName()))));
				} catch (UnsupportedEncodingException e) {
					p.sendMessage(Prefix + Error_errorencoding.replace("%amount%","" + (MaxAmount - getAmount(p.getName()))));
				} catch (IOException e) {
					addAmount(p.getName());
					p.sendMessage(Prefix + Error_errorpassword.replace("%amount%","" + (MaxAmount - getAmount(p.getName()))));
				} catch (ParseException e) {
					p.sendMessage(Prefix + "§c核实过程中出现未知错误!");
				}
			}
		}.runTaskLaterAsynchronously(this, 0L);
	}

	public int getAmount(String name) {
		int amount = 0;
		File f = new File(getDataFolder() + "\\Players\\", name + ".yml");
		FileConfiguration data = YamlConfiguration.loadConfiguration(f);
		if (!f.exists()) {
			try {
				f.createNewFile();
				data.set("Amount", 0);
				data.set("Success", false);
				data.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		amount = data.getInt("Amount");
		return amount;
	}

	public void addAmount(String name) {
		int amount = 0;
		File f = new File(getDataFolder() + "\\Players\\", name + ".yml");
		FileConfiguration data = YamlConfiguration.loadConfiguration(f);
		if (!f.exists()) {
			try {
				f.createNewFile();
				data.set("Amount", 0);
				data.set("Success", false);
				data.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		amount = data.getInt("Amount");
		amount = amount + 1;
		data.set("Amount", amount);
		try {
			data.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
}
