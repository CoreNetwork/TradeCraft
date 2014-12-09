package us.corenetwork.tradecraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logs
{
	public static void debug(String text)
	{
		if (Settings.getBoolean(Setting.DEBUG))
			sendLog("&f[&3TradeCraft&f]&f " + text);
	}

	public static void debugIngame(String text)
	{
		if (Settings.getBoolean(Setting.DEBUG))
			Bukkit.broadcastMessage(text);
	}

	public static void info(String text)
	{
		sendLog("&f[&fTradeCraft&f]&f "+text);
	}

	public static void warning(String text)
	{
		sendLog("&f[&eTradeCraft&f]&f " + text);
	}

	public static void severe(String text)
	{
		sendLog("&f[&cTradeCraft&f]&f " + text);
	}

	public static void sendLog(String text)
	{
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', text));
	}

}
