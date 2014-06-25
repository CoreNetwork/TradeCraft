package us.corenetwork.tradecraft;

import net.minecraft.server.v1_7_R3.ItemStack;
import net.minecraft.server.v1_7_R3.MerchantRecipeList;
import org.bukkit.craftbukkit.v1_7_R3.util.CraftMagicNumbers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

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
        tcv.setIsNew(true);
        villagers.put(UUID, tcv);
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
                boolean alive = set.getBoolean("Alive");
                if(alive)
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

	/**
	 * Saves new or modified villagers and offers to the db
	 */
	public static void SaveVillagers()
	{
		//go through all the villagers, check if they are new, save ones that are (only villies, not the offers)
		//use batch statements, to do it faster, commit
		int savedVillies = saveVillagersData();

		//go through all the villies and all the offers, if offer needs saving, batch, go, done, commit
		int savedOffers = saveOffers();
		
		//after everything saved cleanly, lets clear db from dead villies!
		int removedVillies = removeDeadVillagers();
		
		int thingsDone = savedVillies + savedOffers + removedVillies;

		Logs.info("Tradecraft saved. " + thingsDone + " things saved.");
		Logs.debug("Saved villagers: " + savedVillies + "  Saved offers: " + savedOffers + "  Removed villies: " + removedVillies);
	}

	private static int saveVillagersData() 
	{
		int counter = 0;
		try 
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO villagers (ID, Career, Alive) VALUES (?,?,1)");
			for(String UUID : villagers.keySet())
			{
				TradeCraftVillager villager = villagers.get(UUID);
				if (villager.getIsNew() && villager.isDead() == false)
				{
					counter++;
					villager.setIsNew(false);
					
					statement.setString(1, UUID);
		            statement.setString(2, villager.getCareer());
		            statement.addBatch();
				}
			}
			
            statement.executeBatch();
            statement.close();
            IO.getConnection().commit();
            
		} catch (SQLException e) {
			Logs.severe("Error while saving villagers to database !");
			e.printStackTrace();
		}
		return counter;
	}

	private static int saveOffers()
	{
		int counter = 0;
		//To simplify it all - I'll look over all offers two times. 
		//First, I'll save new offers
		try 
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO offers (Villager, ID, "
					+ "FirstItemID, FirstItemDamage, FirstItemNBT, FirstItemAmount, "
					+ "SecondItemID, SecondItemDamage, SecondItemNBT, SecondItemAmount, "
					+ "ThirdItemID, ThirdItemDamage, ThirdItemNBT, ThirdItemAmount, "
					+ "Tier, TradesLeft, TradesPerformed) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			for(String UUID : villagers.keySet())
			{
				TradeCraftVillager villager = villagers.get(UUID);
				MerchantRecipeList recipes = villager.getTrades();
				for(int i = 0; i<recipes.size();i++)
				{
					CustomRecipe recipe = (CustomRecipe) recipes.get(i); 

					if (recipe.getIsNew() == true && villager.isDead() == false)
					{
						counter++;
						recipe.setIsNew(false);
						recipe.setShouldSave(false);
						
						
						statement.setString(1, UUID);
						statement.setInt(2, recipe.getTradeID());

						statement.setInt(3, CraftMagicNumbers.getId(recipe.getBuyItem1().getItem()));
						statement.setInt(4, recipe.getBuyItem1().getData());

						statement
								.setBytes(5, Util.getNBT(recipe.getBuyItem1()));
						statement.setInt(6, recipe.getBuyItem1().count);

						if (recipe.hasSecondItem()) 
						{
							statement.setInt(7, CraftMagicNumbers.getId(recipe.getBuyItem2().getItem()));
							statement.setInt(8, recipe.getBuyItem2().getData());
							statement.setBytes(9, Util.getNBT(recipe.getBuyItem2()));
							statement.setInt(10, recipe.getBuyItem2().count);
						} else {
							statement.setInt(7, 0);
							statement.setInt(8, 0);
							statement.setBytes(9, new byte[0]);
							statement.setInt(10, 0);
						}

						statement.setInt(11, CraftMagicNumbers.getId(recipe.getBuyItem3().getItem()));
						statement.setInt(12, recipe.getBuyItem3().getData());
						statement.setBytes(13,
								Util.getNBT(recipe.getBuyItem3()));
						statement.setInt(14, recipe.getBuyItem3().count);

						statement.setInt(15, recipe.getTier());
						statement.setInt(16, recipe.getTradesLeft());
						statement.setInt(17, recipe.getTradesPerformed());

						statement.addBatch();


					}
				}
			}

            statement.executeBatch();
            statement.close();
            IO.getConnection().commit();
            
		} catch (SQLException e) {
			Logs.severe("Error while saving new offers to database !");
			e.printStackTrace();
		}


		//Second, I'll save modified offers (tradesLeft)
		try 
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE offers SET TradesLeft = ?, TradesPerformed = ? WHERE Villager = ? AND ID = ?");
			for(String UUID : villagers.keySet())
			{
				TradeCraftVillager villager = villagers.get(UUID);
				MerchantRecipeList recipes = villager.getTrades();
				for(int i = 0; i<recipes.size();i++)
				{
					CustomRecipe recipe = (CustomRecipe) recipes.get(i); 

					if (recipe.shouldSave() == true && villager.isDead() == false)
					{
						counter++;
						recipe.setShouldSave(false);

						statement.setInt(1, recipe.getTradesLeft());
			            statement.setInt(2, recipe.getTradesPerformed());
			            statement.setString(3, UUID);
			            statement.setInt(4, recipe.getTradeID());

						statement.addBatch();
					}
				}
			}

            statement.executeBatch();
            statement.close();
            IO.getConnection().commit();
            
		} catch (SQLException e) {
			Logs.severe("Error while saving new offers to database !");
			e.printStackTrace();
		}
		return counter;
	}


	private static int removeDeadVillagers() 
	{
		int counter = 0;
		
		//mark villies as dead
		try 
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE villagers SET Alive = 0 WHERE ID = ?");
			for(String UUID : villagers.keySet())
			{
				TradeCraftVillager villager = villagers.get(UUID);
				if (villager.isDead() && villager.getIsNew() == false)
				{
					counter++;
					statement.setString(1, UUID);
		            statement.addBatch();
				}
			}
            statement.executeBatch();
            statement.close();
            IO.getConnection().commit();
            
		} catch (SQLException e) {
			Logs.severe("Error while saving villagers to database !");
			e.printStackTrace();
		}
		
		//remove dead villies from main collection
		
		Iterator<String> it = villagers.keySet().iterator();
		
		while(it.hasNext())
		{
			String val = it.next();
			if (villagers.get(val).isDead())
			{
				it.remove();
			}
		}
		
		return counter;
	}
}
