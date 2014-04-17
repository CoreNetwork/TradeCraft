package us.corenetwork.tradecraft.commands;

import org.bukkit.command.CommandSender;

import us.corenetwork.tradecraft.Util;
import us.corenetwork.tradecraft.Villagers;

public class SaveCommand  extends BaseCommand {
	public SaveCommand()
	{
		desc = "Dump villagers data from memory to db";
		needPlayer = false;
		permission = "save";
	}


	public void run(final CommandSender sender, String[] args) {
		Villagers.SaveVillagers();
		Util.Message("Tradecraft saved!", sender);
	}	
}
