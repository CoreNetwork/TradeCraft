package us.corenetwork.tradecraft;

import org.bukkit.Bukkit;

public class Logs
{
	public static void debug(String text)
	{
		if (Settings.getBoolean(Setting.DEBUG))
			info(text);
	}

    public static void debugIngame(String text)
    {
        if (Settings.getBoolean(Setting.DEBUG))
            Bukkit.broadcastMessage(text);
    }
	
	public static void info(String text)
	{
		Bukkit.getLogger().info("[TradeCraft] " + text);
	}
	
	public static void warning(String text)
	{
		Bukkit.getLogger().warning("[TradeCraft] " + text);
	}
	
	public static void severe(String text)
	{
		Bukkit.getLogger().severe("[TradeCraft] " + text);
	}
}
