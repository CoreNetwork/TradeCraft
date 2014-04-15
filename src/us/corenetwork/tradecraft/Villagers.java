package us.corenetwork.tradecraft;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import net.minecraft.server.v1_7_R2.ItemStack;

import org.bukkit.craftbukkit.v1_7_R2.util.CraftMagicNumbers;

import us.corenetwork.tradecraft.db.AddVillagerTask;
import us.corenetwork.tradecraft.db.DbWorker;

/**
 * Class for villager persistence.
 * Everything that concerns saving villagers, offers will go here, to clear out the CustomVillager and CustomRecipe code.
 * @author Ginaf
 *
 */
public class Villagers {

    public static HashMap<String, TradeCraftVillager> villagers = new HashMap<String, TradeCraftVillager>() ;

    /**
     *
     */

    public static boolean exists(String UUID)
    {
        return villagers.containsKey(UUID);
    }

    public static boolean exists(TradeCraftVillager	tcv)
    {
        return exists(tcv.getUUID());
    }
    
    public static void create(String UUID, String career)
    {
        TradeCraftVillager tcv = new TradeCraftVillager(UUID, career);
        villagers.put(UUID, tcv);
        DbWorker.queue.add(new AddVillagerTask(UUID, career));
    }


    public static TradeCraftVillager getVillager(String UUID)
    {
        return villagers.get(UUID);
    }


    public static int getDefaultNumberOfTrades()
    {
        return 2 + TradeCraftPlugin.random.nextInt(6) + TradeCraftPlugin.random.nextInt(6);
    }

    /**
     * Loads all villagers and offers from db to memory
     */
    public static void LoadVillagers()
    {
        try
        {
            PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM villagers");

            ResultSet set = statement.executeQuery();
            while (set.next())
            {
                String uuid = set.getString("ID");
                String career = set.getString("Career");

                villagers.put(uuid, new TradeCraftVillager(uuid, career));
            }
            statement.close();
        }
        catch (SQLException e) {
            Logs.severe("Error while reading villager data from database !");
            e.printStackTrace();
        }

        try {
            PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM offers");

            ResultSet set = statement.executeQuery();
            while (set.next())
            {

                TradeCraftVillager villager = villagers.get(set.getString("Villager"));
                if (villager == null)
                    continue;

                ItemStack itemA;
                ItemStack itemB = null;
                ItemStack itemC;

                int tradeID = set.getInt("ID");
                //First item
                int id = set.getInt("FirstItemID");
                int data = set.getInt("FirstItemDamage");
                int amount = set.getInt("FirstItemAmount");
                itemA = new ItemStack(CraftMagicNumbers.getItem(id), amount, data);
                Util.loadNBT(set.getBytes("FirstItemNBT"), itemA);

                //Second item
                id = set.getInt("SecondItemID");
                if (id > 0)
                {
                    data = set.getInt("SecondItemDamage");
                    amount = set.getInt("SecondItemAmount");
                    itemB = new ItemStack(CraftMagicNumbers.getItem(id), amount, data);
                    Util.loadNBT(set.getBytes("SecondItemNBT"), itemB);
                }

                //Result item
                id = set.getInt("ThirdItemID");
                data = set.getInt("ThirdItemDamage");
                amount = set.getInt("ThirdItemAmount");
                itemC = new ItemStack(CraftMagicNumbers.getItem(id), amount, data);
                Util.loadNBT(set.getBytes("ThirdItemNBT"), itemC);

                CustomRecipe recipe;
                if (itemB == null)
                    recipe = new CustomRecipe(itemA, itemC);
                else
                    recipe = new CustomRecipe(itemA, itemB, itemC);

                recipe.setTradeID(tradeID);
                recipe.setTier(set.getInt("Tier"));
                recipe.setTradesLeft(set.getInt("TradesLeft"));
                recipe.setTradesPerformed(set.getInt("TradesPerformed"));
                villager.addRecipe(recipe);
            }

            statement.close();
        } catch (SQLException e) {
            Logs.severe("Error while reading offers data from database !");
            e.printStackTrace();
        }

        Logs.debug("Finished loading!");
    }

}
