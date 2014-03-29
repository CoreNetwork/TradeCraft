package us.corenetwork.tradecraft;

import org.bukkit.configuration.MemorySection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class NodeParser {
	private double chanceMultiplier;
	private double chanceAdder;

	private boolean anyChance = false;
	
	public NodeParser()
	{
		chanceMultiplier = 1;
		chanceAdder = 0;
	}

	public NodeParser(double chanceMultiplier, double chanceAdder)
	{
		this.chanceMultiplier = chanceMultiplier;
		this.chanceAdder = chanceAdder;
	}

	public boolean didAnyItemHadAnyChance()
	{
		return anyChance;
	}
	
	protected void parseNodeList(List<?> node)
	{
		for (Object setObject : node)
		{
			LinkedHashMap<?,?> hashMap = (LinkedHashMap<?,?>) setObject;
			Entry<?,?> firstEntry = hashMap.entrySet().toArray(new Entry<?,?>[0])[0];

			String type = (String) firstEntry.getKey();

			parseNodeObject(type, firstEntry.getValue());
		}
	}

	protected void parseNodeObject(String type, Object node)
	{
		if (node instanceof List)
		{
			if (type.toLowerCase().startsWith("pick"))
				parsePickList(type, (List<?>) node);
			else
				parseNodeList((List<?>) node);
		}
		else if (node instanceof LinkedHashMap)
		{
			int count = getNumberOfRolls(node, true);
			for (int i = 0; i < count; i++)
			{
				parseNode(type, (LinkedHashMap<?,?>) node);
			}
		}

	}

	protected void parsePickList(String params, List<?> node)
	{
		int count = getNumberOfRolls(node, false);

		int childCount = 0;
		for (Object o : node)
			if (o instanceof LinkedHashMap) childCount++;

		int[] weights = new int[childCount];
		for (int i = 0; i < childCount; i++)
			weights[i] = 1;

		int pickCount = 1;
		String paramSplit[] = params.split(" ");
		if (paramSplit.length > 1)
			pickCount = Integer.parseInt(paramSplit[1]);

		if (pickCount > childCount)
		{
			Logs.warning("Invalid config! Amount of items to pick must be smaller or equal to amount of items!");
			return;
		}

		for (Object o : node)
		{
			if (o instanceof String)
			{
				String text = (String) o;
				if (text.startsWith("weights "))
				{
					String[] textSplit = text.split(" ");
					for (int i = 0; i < childCount; i++)
					{
						try
						{
							weights[i] = Integer.parseInt(textSplit[i + 1]);
						}
						catch (ArrayIndexOutOfBoundsException e)
						{
                            Logs.warning("Invalid config! Amount of weights must be equal to amount of items!");
							return;
						}
					}
				}

			}
		}


		int weightsSum = 0;
		for (int i = 0; i < childCount; i++)
			weightsSum += weights[i];


		for (int a = 0; a < count; a++)
		{
			List<Integer> pickedItems = new ArrayList<Integer>();
			for (int b = 0; b < pickCount; b++)
			{
				int selection = 0;
				do
				{
					int pickedNumber = TradeCraftPlugin.random.nextInt(weightsSum);
					int sum = 0;
					for (int i = 0; i < childCount; i++)
					{
						sum += weights[i];
						if (pickedNumber < sum)
						{
							selection = i;
							break;
						}
					}
				}
				while (pickedItems.contains(selection));

				pickedItems.add(selection);

				int counter = -1;
				for (int i = 0; i < node.size(); i++)
				{
					Object o = node.get(i);
					if (o instanceof LinkedHashMap) 
						counter++;
					else
						continue;

					if (counter == selection)
					{
						LinkedHashMap<?,?> hashMap = (LinkedHashMap<?,?>) o;
						Entry<?,?> firstEntry = hashMap.entrySet().toArray(new Entry<?,?>[0])[0];
						parseNodeObject((String) firstEntry.getKey(), firstEntry.getValue());
						break;
					}

				}

			}
		}
	}

	protected abstract void parseNode(String type, LinkedHashMap<?,?> node);

	protected int getNumberOfRolls(Object node, boolean lowLevel)
	{
		int rolls = 1;
		double chance = 1;

		if (node instanceof List)
		{
			for (Object o : (List<?>) node)
			{
				if (o instanceof String)
				{
					String textSplit[] = ((String) o).split(" ");
					if (textSplit.length > 1 && textSplit[0].equalsIgnoreCase("rolls") && Util.isInteger(textSplit[1]))
					{
						rolls = Integer.parseInt(textSplit[1]);
					}
					else if (textSplit.length > 1 && textSplit[0].equalsIgnoreCase("chance") && Util.isDouble(textSplit[1]))
					{
						chance = Double.parseDouble(textSplit[1]);
					}
				}
			}
		}
		else if (node instanceof LinkedHashMap<?,?>)
		{
			LinkedHashMap<?,?> mapNode = (LinkedHashMap<?,?>) node;
			Integer rollsObject = (Integer) mapNode.get("rolls");
			Number chanceObject = (Number) mapNode.get("chance");

			if (rollsObject != null)
				rolls = rollsObject;
			if (chanceObject != null)
				chance = chanceObject.doubleValue();
		}

		if (lowLevel)
		{
			chance += chanceAdder;
			chance *= chanceMultiplier;			
		}

		if (chance < 0.01)
			return 0;

		anyChance = true;
		
		int num = 0;
		for (int i = 0; i < rolls; i++)
		{
			num += Math.floor(chance / 1.0);
			double newChance = chance % 1;

			double rand = TradeCraftPlugin.random.nextDouble();
			if (rand < newChance)
				num++;
		}

		return num;
	}	

	public static String pickNodeChance(MemorySection section)
	{
		Map<String, Object> nodes = section.getValues(false);

		int childCount = 0;
		for (Object o : nodes.values())
			if (o instanceof MemorySection) childCount++;

		int[] weights = new int[childCount];
		for (int i = 0; i < childCount; i++)
			weights[i] = 1;

		String[] keys = new String[childCount];
		
		int counter = 0;
		for (Entry<String, Object> e : nodes.entrySet())
		{
			if (e.getValue() instanceof MemorySection)
			{
				keys[counter] = e.getKey();
				weights[counter] = ((MemorySection) e.getValue()).getInt("weight", 1);
				counter++;
			}
		}

		int weightsSum = 0;
		for (int i = 0; i < childCount; i++)
			weightsSum += weights[i];


		int pickedNumber = TradeCraftPlugin.random.nextInt(weightsSum);
		int sum = 0;
		for (int i = 0; i < childCount; i++)
		{
			sum += weights[i];
			if (pickedNumber < sum)
			{
				return keys[i];
			}
		}

		return null;
	}
}
