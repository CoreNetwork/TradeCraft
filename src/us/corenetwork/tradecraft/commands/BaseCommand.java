package us.corenetwork.tradecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.tradecraft.Setting;
import us.corenetwork.tradecraft.Settings;
import us.corenetwork.tradecraft.Util;

public abstract class BaseCommand {
	public Boolean needPlayer;
	public String permission;
	public String desc;
	
	public abstract void run(CommandSender sender, String[] args);
	
	public boolean execute(CommandSender sender, String[] args, boolean stripArgs)
	{
		if (stripArgs && args.length > 0 && !Util.isInteger(args[0]))
		{
			String[] newargs = new String[args.length - 1];
			for (int i = 1; i < args.length; i++)
			{
				newargs[i - 1] = args[i];
			}
			args = newargs;			
		}

		if (!(sender instanceof Player) && needPlayer) 
		{
			Util.Message("Sorry, but you need to execute this command as player.", sender);
			return true;
		}
		if (sender instanceof Player && !Util.hasPermission(sender, "tradecraft.command." + permission))
		{
            Util.Message(Settings.getString(Setting.MESSAGE_NO_PERMISSION), sender);
			return true;
		}
		
		run(sender, args);
		return true;
	}

}