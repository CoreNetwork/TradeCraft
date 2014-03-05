package us.corenetwork.tradecraft;

import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.Item;
import net.minecraft.server.v1_7_R1.ItemStack;
import net.minecraft.server.v1_7_R1.MerchantRecipe;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_7_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            Console.warning("No trades found for career " + career + "!");
            return trades;
        }

        for (Object mapO : tradesConfig)
        {
            Map<String, ?> outerMap = (Map<String, ?>) mapO;
            if (outerMap.size() != 1)
            {
                Console.warning("Invalid trades config for career " + career + "!");
                return trades;
            }

            Map<String, ?> map = (Map<String, ?>) outerMap.get("trade");
            if (map == null)
            {
                Console.warning("Invalid trades config for career " + career + "!");
                return trades;
            }

            ItemStack itemA = getItemStack((Map<?, ?>) map.get("itemA"));
            ItemStack itemC = getItemStack((Map<?, ?>) map.get("result"));

            ItemStack itemB = null;
            if (map.containsKey("itemB"))
                itemB = getItemStack((Map<?, ?>) map.get("itemB"));

            if (itemA == null || itemC == null)
                continue;

            CustomRecipe recipe;
            if (itemB == null)
                recipe = new CustomRecipe(itemA, itemC);
            else
                recipe = new CustomRecipe(itemA, itemB, itemC);

            trades.add(recipe);
        }

        return trades;
    }

    private static ItemStack getItemStack(Map<?,?> map)
    {
        int id;
        Object idO = map.get("id");
        if (idO == null)
        {
            Console.warning("Invalid trades config: Missing item ID!");
            return null;
        }
        if (idO instanceof String)
        {
            if ("currency".equalsIgnoreCase((String) idO))
            {
                id = Settings.getInt(Setting.CURRENCY);
            }
            else
            {
                Console.warning("Invalid trades config: Invalid item ID!");
                return null;
            }
        }
        else
            id = ((Integer) idO).intValue();


        int amount;
        Object amountO = map.get("amount");
        if (amountO == null)
        {
            Console.warning("Invalid trades config: Missing item amount!");
            return null;
        }
        if (amountO instanceof Integer)
            amount = ((Integer) amountO).intValue();
        else
        {
            String amountS = (String) amountO;
            if (!amountS.contains(","))
            {
                Console.warning("Invalid trades config: Invalid item amount!");
            }

            int min = Integer.parseInt(amountS.substring(0, amountS.indexOf(",")));
            int max = Integer.parseInt(amountS.substring(amountS.indexOf(",") + 1));

            amount = TradeCraftPlugin.random.nextInt(max - min) + min;
        }

        Integer data = 0;
        if (map.containsKey("data"))
            data = (Integer) map.get("data");

        ItemStack stack = new ItemStack(CraftMagicNumbers.getItem(id), amount, data);
        return stack;
    }

    public static String getRandomCareer(int villagerType)
    {
        List<String> careers = IO.config.getStringList("Professions." + villagerType);
        if (careers.size() == 0)
        {
            Console.severe("No professions found for villager number " + villagerType + ".");
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
}
