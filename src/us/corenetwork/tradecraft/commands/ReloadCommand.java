package us.corenetwork.tradecraft.commands;

import org.bukkit.command.CommandSender;

import us.corenetwork.tradecraft.IO;
import us.corenetwork.tradecraft.Setting;
import us.corenetwork.tradecraft.Settings;
import us.corenetwork.tradecraft.Util;

public class ReloadCommand extends BaseCommand {
	public ReloadCommand()
	{
		desc = "Reload config";
		needPlayer = false;
		permission = "reload";
	}


	public void run(final CommandSender sender, String[] args) {
		IO.LoadSettings();
        Util.Message(Settings.getString(Setting.MESSAGE_CONFIGURATION_RELOADED), sender);
	}	
}
