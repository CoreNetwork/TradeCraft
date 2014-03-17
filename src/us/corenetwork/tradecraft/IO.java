package us.corenetwork.tradecraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Villager;

public class IO {
	public static YamlConfiguration config;
    private static Connection connection;

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

    public static synchronized Connection getConnection() {
        if (connection == null) connection = createConnection();
        return connection;
    }
    private static Connection createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection ret = DriverManager.getConnection("jdbc:sqlite:" + new File(TradeCraftPlugin.instance.getDataFolder().getPath(), "data.sqlite").getPath());
            ret.setAutoCommit(false);
            return ret;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized void freeConnection() {
        Connection conn = getConnection();
        if(conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void PrepareDB()
    {
        Connection conn;
        Statement st = null;
        try {
            conn = IO.getConnection();//            {
            st = conn.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS villagers (ID STRING NOT NULL, Career STRING NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS offers (Villager STRING, ID INTEGER, FirstItemID INTEGER, FirstItemDamage INTEGER, FirstItemNBT BLOB, FirstItemAmount INTEGER, SecondItemID INTEGER, SecondItemDamage INTEGER, SecondItemNBT BLOB, SecondItemAmount INTEGER, ThirdItemID INTEGER, ThirdItemDamage INTEGER, ThirdItemNBT BLOB, ThirdItemAmount INTEGER, Tier INTEGER, TradesLeft INTEGER)");
            conn.commit();
            st.close();
        } catch (SQLException e) {
            TradeCraftPlugin.instance.getLogger().log(Level.SEVERE, "[Mantle]: Error while creating tables! - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
