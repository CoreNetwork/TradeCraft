package us.corenetwork.tradecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import us.corenetwork.tradecraft.commands.BaseCommand;
import us.corenetwork.tradecraft.commands.ReloadCommand;
import us.corenetwork.tradecraft.commands.SaveCommand;
import us.corenetwork.tradecraft.db.DbWorker;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

public class TradeCraftPlugin extends JavaPlugin {
	public static TradeCraftPlugin instance;
	
	public static Random random;
	
	public static HashMap<String, BaseCommand> commands = new HashMap<String, BaseCommand>();

    private Thread dbWorkerThread;

	@Override
	public void onEnable() {
		instance = this;
		random = new Random();
		
		commands.put("reload", new ReloadCommand());
		commands.put("save", new SaveCommand());
        getServer().getPluginManager().registerEvents(new TradeCraftListener(), this);

		IO.LoadSettings();
        IO.PrepareDB();
        NMSVillagerManager.register();
        
        try {
			IO.getConnection().commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        Villagers.LoadVillagers();

        dbWorkerThread = new Thread(new DbWorker());
        dbWorkerThread.start();
	}

	@Override
	public void onDisable() {
        DbWorker.stopFurtherRequests();
        
        dbWorkerThread.interrupt();
        
        IO.freeConnection();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		BaseCommand cmd = commands.get(args[0]);
		if (cmd != null)
			return cmd.execute(sender, args, true);
		else
			return false;
	}
}
