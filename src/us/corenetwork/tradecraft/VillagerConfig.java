package us.corenetwork.tradecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;


/**
 * Created by Matej on 3.3.2014.
 */
public class VillagerConfig {

    public static boolean hasTrades(String career, int tier)
    {
        return IO.config.get("Tiers." + career + "." + tier) != null;
    }

    public static List<CustomRecipe> getTrades(String career, int tier)
    {
        List<CustomRecipe> trades = new ArrayList<CustomRecipe>();

        List<?> tradesConfig = (List<?>) IO.config.get("Tiers." + career + "." + tier);

        if (tradesConfig == null)
        {
            Logs.warning("No trades found for career " + career + "!");
            return trades;
        }

        for (Object mapO : tradesConfig)
        {
            Map<String, ?> outerMap = (Map<String, ?>) mapO;
            if (outerMap.size() != 1)
            {
                Logs.warning("Invalid trades config for career " + career + "!");
                return trades;
            }

            Map<String, ?> map = (Map<String, ?>) outerMap.get("trade");
            if (map == null)
            {
                Logs.warning("Invalid trades config for career " + career + "!");
                return trades;
            }

            ItemStack itemA = getItemStack((Map<?, ?>) map.get("itemA"), null, null);

            ItemStack itemB = null;
            if (map.containsKey("itemB"))
                itemB = getItemStack((Map<?, ?>) map.get("itemB"), itemA, null);

            ItemStack itemC = getItemStack((Map<?, ?>) map.get("result"), itemA, itemB);

            if (itemA == null || itemC == null)
                continue;

            itemA.setAmount(Math.min(itemA.getAmount(), 64));

            CustomRecipe recipe;
            if (itemB == null)
                recipe = new CustomRecipe(CraftItemStack.asNMSCopy(itemA), CraftItemStack.asNMSCopy(itemC));
            else
            {
                itemB.setAmount(Math.min(itemB.getAmount(), 64));
                recipe = new CustomRecipe(CraftItemStack.asNMSCopy(itemA), CraftItemStack.asNMSCopy(itemB), CraftItemStack.asNMSCopy(itemC));

            }

            trades.add(recipe);
        }

        return trades;
    }

    private static ItemStack getItemStack(Map<?,?> map, ItemStack itemA, ItemStack itemB)
    {
        int id;
        int amount = 0;
        Integer data = 0;

        if (map.containsKey("currency"))
        {
        	String currencyName = Settings.getString(Setting.CURRENCY);
        	Integer idFromName = MinecraftNames.getMaterialId(currencyName);
            if (idFromName == null)
            {
                Logs.warning("Invalid trades config: Unknown currency item name:" + currencyName + "!");
                return null;
            }
            id = idFromName;
            amount = getRandomNumber(map.get("currency"));
        } else if (map.containsKey("item"))
        {
            String item = (String) map.get("item");
            String damageString = null;

            if (item.contains(","))
            {
                String[] split = item.split(",");
                item = split[0];
                damageString = split[1];
            }

            Integer idFromName = MinecraftNames.getMaterialId((String) item);
            if (idFromName == null)
            {
                Logs.warning("Invalid trades config: Unknown item name:" + item + "!");
                return null;
            }
            id = idFromName;

            data = getDamageValue(id, damageString);
            amount = getRandomNumber(map.get("amount"));
        }
        else if (map.containsKey("name"))
        {
            Object name = map.get("name");
            if (!(name instanceof String))
            {
                Logs.warning("Invalid trades config: Invalid item name!");
                return null;
            }

            Integer idFromName = MinecraftNames.getMaterialId((String) name);
            if (idFromName == null)
            {
                Logs.warning("Invalid trades config: Unknown item name:" + name + "!");
                return null;
            }

            id = idFromName;
            amount = getRandomNumber(map.get("amount"));
        }
        else
        {
            Object idO = map.get("id");
            if (idO == null || !(idO instanceof Integer))
            {
                Logs.warning("Invalid trades config: Missing or invalid item ID!");
                return null;
            }
            else
                id = ((Integer) idO).intValue();

            amount = getRandomNumber(map.get("amount"));
        }

        if (map.containsKey("data"))
            data = (Integer) map.get("data");

        ItemStack stack = new ItemStack(id, amount, data.shortValue());

        Object yamlNbtTag = map.get("NBT");
        if (yamlNbtTag != null)
        {
            NBTTagCompound newTag;
            if (yamlNbtTag instanceof String)
            {
                newTag = NanobotLoader.load((String) yamlNbtTag);
                if (newTag == null)
                {
                    Logs.warning("Invalid config! Nanobot file " + ((String) yamlNbtTag) + ".yml is missing!");
                }
            }
            else
            {
                newTag = NanobotLoader.load((Map<?,?>) yamlNbtTag);
            }

            if (newTag != null)
            {
                net.minecraft.server.v1_8_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
                nmsStack.setTag(newTag);
                stack = CraftItemStack.asCraftMirror(nmsStack);
            }
        }


        List<Map<String,?>> enchants = (List<Map<String,?>>) map.get("enchants");
        if (enchants != null)
        {
            new EnchantParser(stack, itemA, itemB).parse(enchants);
        }


        return stack;
    }

    private static int getDamageValue(int itemID, Object value)
    {
        if (value == null)
            return 0;
        else if (value instanceof Integer)
            return (Integer) value;
        else if (value instanceof String)
        {
            if (Util.isInteger((String) value))
                return Integer.parseInt((String) value);

            if (itemID == Material.WOOL.getId() || itemID == Material.INK_SACK.getId() || itemID == Material.STAINED_GLASS.getId() || itemID == Material.STAINED_GLASS_PANE.getId() || itemID == Material.CARPET.getId())
            {
                DyeColor color = (DyeColor) Util.findEnum(DyeColor.values(), (String) value);
                if (color == null)
                {
                    Logs.warning("Invalid damage config in name: node! " + value + " is not a color!");
                    return 0;
                }

                if (itemID == Material.INK_SACK.getId())
                    return color.getDyeData(); //Dyes have different color values

                return color.getWoolData();
            }
        }

        Logs.warning("Invalid damage config in name: node!");
        return 0;

    }

    public static int getRandomNumber(Object node)
    {
        if (node == null)
        {
            //No warning, default to 1 silently
            //Logs.warning("Invalid trades config: missing number! Defaulting to 1");
            return 1;
        }
        if (node instanceof Integer)
            return ((Integer) node).intValue();

        String amountS = (String) node;
        if (!amountS.contains(","))
        {
            Logs.warning("Invalid trades config: Invalid number!");
        }

        int min = Integer.parseInt(amountS.substring(0, amountS.indexOf(",")));
        int max = Integer.parseInt(amountS.substring(amountS.indexOf(",") + 1)) + 1;

        return TradeCraftPlugin.random.nextInt(max - min) + min;
    }

    public static String getRandomCareer(int villagerType)
    {
        Logs.debugIngame("Got new villager: " + villagerType);

        List<String> careers = IO.config.getStringList("Professions." + villagerType);
        if (careers.size() == 0)
        {
            Logs.severe("No professions found for villager number " + villagerType + ".");
            return null;
        }

        int profCount = 0;
        String weightsS = null;
        for (String career : careers)
        {
            if (career.startsWith("weights "))
                weightsS = career;
            else
                profCount++;
        }

        int weights[] = new int[profCount];
        if (weightsS != null)
        {
            String textSplit[] = weightsS.split(" ");
            for (int i = 0; i < profCount; i++)
            {
                weights[i] = Integer.parseInt(textSplit[i + 1]);
            }
        }
        else
        {
            for (int i = 0; i < profCount; i++)
            {
                weights[i] = 1;
            }
        }

        int weightsSum = 0;
        for (int i = 0; i < profCount; i++)
            weightsSum += weights[i];

        int selection = 0;
        int pickedNumber = TradeCraftPlugin.random.nextInt(weightsSum);
        int sum = 0;
        for (int i = 0; i < profCount; i++)
        {
            sum += weights[i];
            if (pickedNumber < sum)
            {
                selection = i;
                break;
            }
        }

        int counter = 0;
        for (String career : careers)
        {
            if (career.startsWith("weights "))
                continue;

            if (counter == selection)
                return career;
            counter++;
        }

        return null;
    }


    public static String getColor(String career)
    {
        return IO.config.getString("Tiers."+career+".color", "");
    }
}
