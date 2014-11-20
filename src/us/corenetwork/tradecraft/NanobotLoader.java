package us.corenetwork.tradecraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.NBTBase;
import net.minecraft.server.v1_7_R4.NBTTagByte;
import net.minecraft.server.v1_7_R4.NBTTagByteArray;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagDouble;
import net.minecraft.server.v1_7_R4.NBTTagFloat;
import net.minecraft.server.v1_7_R4.NBTTagInt;
import net.minecraft.server.v1_7_R4.NBTTagIntArray;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTTagLong;
import net.minecraft.server.v1_7_R4.NBTTagShort;
import net.minecraft.server.v1_7_R4.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class NanobotLoader {
	public static NBTTagCompound load(String name)
	{
		YamlConfiguration yaml = new YamlConfiguration();

		File folder = new File(TradeCraftPlugin.instance.getDataFolder(), "nanobot");
		if (!folder.exists())
			folder.mkdir();


		try {
			yaml.load(new File(folder, name + ".yml"));
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			Logs.severe("Error while loading tag yml file - " + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (InvalidConfigurationException e) {
			Logs.severe("Error while loading tag yml file - " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		return load(yaml.getValues(false));
	}
	
	public static NBTTagCompound load(Map<?,?> section)
	{
		NBTTagCompound newTag = new NBTTagCompound();
		
		for (Entry<?, ?> e : section.entrySet())
		{
			NBTBase tag =  loadTag(e.getValue(), e.getKey().equals("compound"));
			newTag.set((String) e.getKey(), tag);
		}
			
		return newTag;
	}
	
	public static NBTBase loadTag(Object tag)
	{
		return loadTag(tag, false);
	}
	
	public static NBTBase loadTag(Object tag, boolean isCompound)
	{
		if (tag instanceof String)
		{
			return new NBTTagString(fixFormatting((String) tag));
		}
		else if (tag instanceof ArrayList)
		{
			NBTTagList list = new NBTTagList();
			for (Object o : (ArrayList) tag)
				list.add(loadTag(o));
			
			return list;
		}
		else if (tag instanceof MemorySection || tag instanceof LinkedHashMap)
		{
			Map<String, Object> map;
			
			if (tag instanceof MemorySection)
			{
				MemorySection section = (MemorySection) tag;
				map = section.getValues(false);
			}
			else
				map = (Map) tag;
			
			if (isCompound)
			{
				NBTTagCompound compound = new NBTTagCompound();
								
				for (Entry<String, Object> ee : map.entrySet())
				{
					Bukkit.getServer().broadcastMessage("ee - " + ee.getKey());

					NBTBase eTag = loadTag(ee.getValue(), ee.getKey().equals("compound"));
					compound.set(ee.getKey(), eTag);
				}
				
				return compound;
			}
			
			for (Entry<String, Object> e : map.entrySet())
			{
				if (e.getKey().equals("byte"))
				{
					return new NBTTagByte((byte) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("short"))
				{
					return new NBTTagShort((short) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("int"))
				{
					return new NBTTagInt((Integer) e.getValue());
				}
				else if (e.getKey().equals("long"))
				{
					return new NBTTagLong((long) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("float"))
				{
					return new NBTTagFloat((float) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("double"))
				{
					return new NBTTagDouble((double) (int) (Integer) e.getValue());
				}
				else if (e.getKey().equals("byteArray"))
				{
					return new NBTTagByteArray(convert(((ArrayList<Integer>) e.getValue()).toArray(new Byte[0])));
				}
				else if (e.getKey().equals("intArray"))
				{
					return new NBTTagIntArray(convert(((ArrayList<Integer>) e.getValue()).toArray(new Integer[0])));
				}
				else if (e.getKey().equals("compound"))
				{
					NBTTagCompound compound = new NBTTagCompound();
					
					Map<String, Object> inMap = null;
					
					if (e.getValue() instanceof MemorySection)
					{
						MemorySection section = (MemorySection) e.getValue();
						inMap = section.getValues(false);
					}
					else
						inMap = (Map) e.getValue();
					
					for (Entry<String, Object> ee : inMap.entrySet())
					{
						NBTBase eTag = loadTag(ee.getValue(), ee.getKey().equals("compound"));
						compound.set(ee.getKey(), eTag);
					}
					
					return compound;
				}
			}
		}
				
		return null;
	}

	public static String fixFormatting(String source)
	{
		source = source.replaceAll("(?<!&)&([klmnor0-9abcdef])", ChatColor.COLOR_CHAR + "$1");
		source = source.replaceAll("&&([klmnor0-9abcdef])", ChatColor.COLOR_CHAR + "$1");

		return source;
	}

	public static int[] convert(Integer[] input)
	{
		int[] array = new int[input.length];
		for (int i = 0; i < array.length; i++)
			array[i] = input[i].intValue();

		return array;
	}

	public static byte[] convert(Byte[] input)
	{
		byte[] array = new byte[input.length];
		for (int i = 0; i < array.length; i++)
			array[i] = input[i].byteValue();

		return array;
	}

	public static Integer[] convert(int[] input)
	{
		Integer[] array = new Integer[input.length];
		for (int i = 0; i < array.length; i++)
			array[i] = Integer.valueOf(input[i]);

		return array;
	}

	public static Byte[] convert(byte[] input)
	{
		Byte[] array = new Byte[input.length];
		for (int i = 0; i < array.length; i++)
			array[i] = Byte.valueOf(input[i]);

		return array;
	}

}
