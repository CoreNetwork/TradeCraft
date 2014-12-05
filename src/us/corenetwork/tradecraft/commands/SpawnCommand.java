package us.corenetwork.tradecraft.commands;

import java.util.List;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.entity.CreatureSpawnEvent;
import us.corenetwork.tradecraft.CustomVillager;
import us.corenetwork.tradecraft.IO;
import us.corenetwork.tradecraft.Util;
import us.corenetwork.tradecraft.Villagers;

public class SpawnCommand  extends BaseCommand {
	public SpawnCommand()
	{
		desc = "Spawn a villager with unique proffesion";
		needPlayer = true;
		permission = "spawn";
	}


	public void run(final CommandSender sender, String[] args) 
	{
		if(args.length != 2)
		{
			Util.Message("Usage : /tradecraft spawn <villagerType> <proffesionName>", sender);
			return;
		}
		
		if(Util.isInteger(args[0]) == false)
		{
			Util.Message("Usage : <villagerType> must be an integer", sender);
			return;
		}
		
		List<String> list = IO.config.getStringList("CustomProfessions");
		if(list.contains(args[1]) == false)
		{
			Util.Message("Proffesion name needs to be on a CustomProfessions list in config", sender);
			return;
		}
		
		Player player = (Player)sender;
		Location location = player.getLocation();
		
		CraftPlayer craftPlayer = (CraftPlayer)player;
		World world = craftPlayer.getHandle().world;
		
		CustomVillager newVillager = new CustomVillager(world, Integer.parseInt(args[0]));
        newVillager.setPosition(location.getX(), location.getY(), location.getZ());
        world.addEntity(newVillager, CreatureSpawnEvent.SpawnReason.CUSTOM);
		Villagers.create(newVillager.getUniqueID().toString(), args[1]);
	}	
}
