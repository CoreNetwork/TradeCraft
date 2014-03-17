package us.corenetwork.tradecraft;

import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.Item;
import net.minecraft.server.v1_7_R1.MerchantRecipe;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
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

            ItemStack itemA = getItemStack((Map<?, ?>) map.get("itemA"), null, null);

            ItemStack itemB = null;
            if (map.containsKey("itemB"))
                itemB = getItemStack((Map<?, ?>) map.get("itemB"), itemA, null);

            ItemStack itemC = getItemStack((Map<?, ?>) map.get("result"), itemA, itemB);

            if (itemA == null || itemC == null)
                continue;

            CustomRecipe recipe;
            if (itemB == null)
                recipe = new CustomRecipe(CraftItemStack.asNMSCopy(itemA), CraftItemStack.asNMSCopy(itemC));
            else
                recipe = new CustomRecipe(CraftItemStack.asNMSCopy(itemA), CraftItemStack.asNMSCopy(itemB), CraftItemStack.asNMSCopy(itemC));

            trades.add(recipe);
        }

        return trades;
    }

    private static ItemStack getItemStack(Map<?,?> map, ItemStack itemA, ItemStack itemB)
    {
        int id;
        int amount;
        if (map.containsKey("currency"))
        {
            id = Settings.getInt(Setting.CURRENCY);
            amount = getRandomNumber(map.get("currency"));
        }
        else
        {
            Object idO = map.get("id");
            if (idO == null || !(idO instanceof Integer))
            {
                Console.warning("Invalid trades config: Missing or invalid item ID!");
                return null;
            }

            id = ((Integer) idO).intValue();

            amount = getRandomNumber(map.get("amount"));
        }


        Integer data = 0;
        if (map.containsKey("data"))
            data = (Integer) map.get("data");

        ItemStack stack = new ItemStack(id, amount, data.shortValue());

        List<Map<String,?>> enchants = (List<Map<String,?>>) map.get("enchants");
        if (enchants != null)
        {
            for (Map<String, ?> enchantOuterMap : enchants)
            {
                Map<String, ?> enchant = (Map<String, ?>) enchantOuterMap.values().toArray()[0];

                Number chance = (Number) enchant.get("chance");
                if (chance == null)
                {
                    Console.warning("Invalid trades config: Missing enchant chance!");
                    continue;

                }
                if (chance.doubleValue() < TradeCraftPlugin.random.nextDouble())
                    continue;

                Number enchantID = (Number) enchant.get("id");
                if (enchantID == null)
                {
                    Console.warning("Invalid trades config: Missing enchant ID!");
                    continue;
                }

                int enchantLevel = getRandomNumber(enchant.get("level"));
                if (enchantLevel == 0)
                {
                    Console.warning("Invalid trades config: Missing or invalid enchant level!");
                    continue;
                }

                Object bonusA = enchant.get("bonusAmountA");
                if (bonusA != null && itemA != null)
                {
                    int bonusAmount = getRandomNumber(bonusA);
                    itemA.setAmount(Math.min(itemA.getAmount() + bonusAmount, itemA.getType().getMaxStackSize()));
                }

                Object bonusB = enchant.get("bonusAmountB");
                if (bonusB != null && itemB != null)
                {
                    int bonusAmount = getRandomNumber(bonusB);
                    itemB.setAmount(Math.min(itemB.getAmount() + bonusAmount, itemB.getType().getMaxStackSize()));
                }

                stack.addUnsafeEnchantment(Enchantment.getById(enchantID.intValue()), enchantLevel);
            }
        }


        return stack;
    }

    private static int getRandomNumber(Object node)
    {
        if (node == null)
        {
            Console.warning("Invalid trades config: Invalid number!");
            return 0;
        }
        if (node instanceof Integer)
            return ((Integer) node).intValue();

        String amountS = (String) node;
        if (!amountS.contains(","))
        {
            Console.warning("Invalid trades config: Invalid number!");
        }

        int min = Integer.parseInt(amountS.substring(0, amountS.indexOf(",")));
        int max = Integer.parseInt(amountS.substring(amountS.indexOf(",") + 1));

        return TradeCraftPlugin.random.nextInt(max - min) + min;
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
