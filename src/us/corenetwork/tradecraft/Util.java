package us.corenetwork.tradecraft;
import net.minecraft.server.v1_7_R1.ItemStack;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Util {
	public static Boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
    }

	public static Boolean isDouble(String text) {
		try {
			Double.parseDouble(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}


	public static int flatDistance(Location a, Location b)
	{
		return ((a.getBlockX() - b.getBlockX()) * (a.getBlockX() - b.getBlockX())) + ((a.getBlockZ() - b.getBlockZ()) * (a.getBlockZ() - b.getBlockZ()));
	}

	public static Location unserializeLocation(String text)
	{
		String[] split = text.split(";");

		World world = Bukkit.getWorld(split[0]);
		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		double z = Double.parseDouble(split[3]);
		float pitch = Float.parseFloat(split[4]);
		float yaw = Float.parseFloat(split[5]);

		return new Location(world, x, y, z, yaw, pitch);
	}

	public static String serializeLocation(Location location)
	{
		String locString = location.getWorld().getName().concat(";");
		locString = locString.concat(Double.toString(location.getX())).concat(";").concat(Double.toString(location.getY())).concat(";").concat(Double.toString(location.getZ())).concat(";");
		locString = locString.concat(Float.toString(location.getPitch())).concat(";").concat(Float.toString(location.getYaw()));

		return locString;
	}

	public static boolean hasPermission(CommandSender player, String permission)
	{
		while (true)
		{
			if (player.hasPermission(permission))
				return true;

			if (permission.length() < 2)
				return false;

			if (permission.endsWith("*"))
				permission = permission.substring(0, permission.length() - 2);

			int lastIndex = permission.lastIndexOf(".");
			if (lastIndex < 0)
				return false;

			permission = permission.substring(0, lastIndex).concat(".*");  
		}
	}

    public static void safeTeleport(final Player player, final Location location)
    {
        Chunk c = location.getChunk();
        if (!c.isLoaded())
            location.getChunk().load();
        player.teleport(location);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(TradeCraftPlugin.instance, new Runnable() {
            @Override
            public void run() {
                player.teleport(location);

            }
        }, 10);
    }

    public static void Message(String message, CommandSender sender)
    {
        message = message.replaceAll("\\&([0-9abcdefklmnor])", ChatColor.COLOR_CHAR + "$1");

        final String newLine = "\\[NEWLINE\\]";
        String[] lines = message.split(newLine);

        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim();

            if (i == 0)
                continue;

            int lastColorChar = lines[i - 1].lastIndexOf(ChatColor.COLOR_CHAR);
            if (lastColorChar == -1 || lastColorChar >= lines[i - 1].length() - 1)
                continue;

            char lastColor = lines[i - 1].charAt(lastColorChar + 1);
            lines[i] = Character.toString(ChatColor.COLOR_CHAR).concat(Character.toString(lastColor)).concat(lines[i]);
        }

        for (int i = 0; i < lines.length; i++)
            sender.sendMessage(lines[i]);
    }

    public static void MessagePermissions(String message, String permission)
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (Util.hasPermission(p,permission))
                Message(message, p);
        }
    }

    public static byte[] getNBT(ItemStack stack)
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(byteStream);
        NBTTagCompound tag = stack.getTag();
        if (tag == null)
            return new byte[0];

        try {
            Method method = NBTTagCompound.class.getDeclaredMethod("write", DataOutput.class);
            method.setAccessible(true);

            method.invoke(tag, dataOutput);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return byteStream.toByteArray();
    }

    public static void loadNBT(byte[] nbt, ItemStack stack)
    {
        if (nbt == null || nbt.length == 0)
            return;

        NBTTagCompound tag = new NBTTagCompound();

        ByteArrayInputStream stream = new ByteArrayInputStream(nbt);
        DataInputStream dataInput = new DataInputStream(stream);

        try {
            Method method = NBTTagCompound.class.getDeclaredMethod("read", DataInput.class, Integer.class);
            method.setAccessible(true);

            method.invoke(tag, dataInput, 0);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        stack.setTag(tag);
    }

}
