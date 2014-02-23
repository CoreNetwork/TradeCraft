package us.corenetwork.tradecraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
public class IO {
	public static YamlConfiguration config;

	public static void LoadSettings()
	{
		try {
			config = new YamlConfiguration();

			if (!new File(TradeCraftPlugin.instance.getDataFolder(),"config.yml").exists()) config.save(new File(TradeCraftPlugin.instance.getDataFolder(),"config.yml"));

			config.load(new File(TradeCraftPlugin.instance.getDataFolder(),"config.yml"));
			for (Setting s : Setting.values())
			{
				if (config.get(s.getString()) == null && s.getDefault() != null) config.set(s.getString(), s.getDefault());
			}

			saveConfig();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static void saveConfig()
	{
		try {
			config.save(new File(TradeCraftPlugin.instance.getDataFolder(),"config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
