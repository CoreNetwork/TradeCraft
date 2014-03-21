package us.corenetwork.tradecraft;

import net.minecraft.server.v1_7_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.craftbukkit.v1_7_R1.util.CraftMagicNumbers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Created by Matej on 23.2.2014.
 */
public class CustomVillager extends EntityVillager {
    private String carreer = "NOT_INITIALIZED";
    private MerchantRecipeList trades;

    //Schedules after window closes
    private boolean createNewTier = false;
    private boolean restockAll = false;

    public CustomVillager(World world) {
        super(world);

        init();
    }

    public CustomVillager(World world, int i) {
        super(world, i);

       init();
    }

    private void init()
    {
        trades = new MerchantRecipeList();

        Bukkit.getScheduler().scheduleSyncDelayedTask(TradeCraftPlugin.instance, new Runnable()
        {
            @Override
            public void run()
            {
                loadVillagerData();
                loadTradesFromDB();

                if (trades.size() == 0)
                {
                    addTier(0);
                }
            }
        });
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entityAgeable) {
        return b(entityAgeable);
    }


    /**
     * Returns list of offers
     */
    @Override
    public MerchantRecipeList getOffers(EntityHuman entityHuman) {
        if (trades == null || trades.size() == 0)
        {
            Console.severe("Villager " + uniqueID.toString() + " has no trades!");

            CustomRecipe recipe = new CustomRecipe(new ItemStack((Block) Block.REGISTRY.a("diamond_block"), 65), new ItemStack((Block) Block.REGISTRY.a("dirt"), 1));
            //recipe.lockManually();

            MerchantRecipeList list = new MerchantRecipeList();
            list.add(recipe);

            return list;
        }

        return trades;
    }

    /**
     * Activated when new player starts trading (or null when nobody is trading)
     */

    @Override
    public void a_(EntityHuman entityHuman)
    {
        if (entityHuman == null) //Nobody is trading now
        {
            if (createNewTier)
            {
                addTier(getLastTier() + 1);
                refreshAll();
            }
            else if (restockAll)
            {
                refreshAll();
            }

            restockAll = false;
            createNewTier = false;
        }
        else
            Bukkit.broadcastMessage("Trading with: " + carreer);

        super.a_(entityHuman);
    }

    /**
     * Activated when player makes a trade
     */
    @Override
    public void a(MerchantRecipe vanillaRecipe)
    {
        // Yes/No sound
        this.makeSound("mob.villager.yes", this.bf(), this.bg());

        //Refrehs inventory
        EntityHuman human = b();
        if (human != null && human instanceof EntityPlayer)
        {
            final org.bukkit.entity.Player player = ((EntityPlayer) human).getBukkitEntity();
            Bukkit.getScheduler().runTask(TradeCraftPlugin.instance, new Runnable()
            {
                @Override
                public void run()
                {
                    player.updateInventory();
                }
            });
            ((EntityPlayer) human).updateInventory(human.activeContainer);
        }

        CustomRecipe recipe = (CustomRecipe) vanillaRecipe;
        if (trades == null)
            return;

        int tradeID = trades.indexOf(recipe);
        if (tradeID < 0)
        {
            Console.severe("Player completed unknown trade on villager " + uniqueID.toString() + "! ");
            return;
        }

        incrementCounter(tradeID);
        Bukkit.broadcastMessage("Trade completed! Left:" + recipe.getTradesLeft());

        if (areAllTiersUnlocked())
        {
            if (random.nextDouble() < Settings.getDouble(Setting.ALL_UNLOCKED_REFRESH_CHANCE))
            {
                restockAll = true;
            }
        }
        else
        {
            if (isLastTier(recipe) || random.nextInt(100) < 20) //Add new tier when on last trade or with 20% chance
            {
                createNewTier = true;
            }
        }
    }

    public void loadVillagerData()
    {
        try
        {
            PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM villagers WHERE ID = ?");
            statement.setString(1, uniqueID.toString());

            ResultSet set = statement.executeQuery();
            if (set.next())
            {
                carreer = set.getString("Career");

                statement.close();
            }
            else
            {
                statement.close();
                createVillagerData();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void createVillagerData()
    {
        carreer = VillagerConfig.getRandomCareer(getProfession());
        Bukkit.broadcastMessage("Decided career: " + carreer);

        try
        {
            PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO villagers (ID, Career) VALUES (?,?)");
            statement.setString(1, uniqueID.toString());
            statement.setString(2, carreer);
            statement.executeUpdate();
            IO.getConnection().commit();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void loadTradesFromDB()
    {
        try
        {
            PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM offers WHERE Villager = ?");
            statement.setString(1, uniqueID.toString());

            ResultSet set = statement.executeQuery();
            while (set.next())
            {
                ItemStack itemA;
                ItemStack itemB = null;
                ItemStack itemC;

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
                    data = set.getInt("SecondtItemDamage");
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

                recipe.setTier(set.getInt("Tier"));
                recipe.setTradesLeft(set.getInt("TradesLeft"));
                trades.add(recipe);
            }

            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    private void refreshAll()
    {


        try
        {
            PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE offers SET TradesLeft = ? WHERE Villager = ? AND ID = ?");

            for (int i = 0; i < trades.size(); i++)
            {
                int tradesLeft = getDefaultNumberOfTrades();

                CustomRecipe recipe = (CustomRecipe) trades.get(i);
                recipe.setTradesLeft(tradesLeft);

                statement.setInt(1, tradesLeft);
                statement.setString(2, uniqueID.toString());
                statement.setInt(2, i);
                statement.addBatch();
            }

            statement.executeBatch();
            statement.close();
            IO.getConnection().commit();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void incrementCounter(int tradeID)
    {
        CustomRecipe recipe  = (CustomRecipe) trades.get(tradeID);
        recipe.setTradesLeft(recipe.getTradesLeft() - 1);

        try
        {
            PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE offers SET TradesLeft = ? WHERE Villager = ? AND ID = ?");
            statement.setString(1, uniqueID.toString());
            statement.setInt(2, tradeID);
            statement.executeUpdate();
            statement.close();
            IO.getConnection().commit();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void addTier(int tier)
    {
        List<CustomRecipe> recipes = VillagerConfig.getTrades(carreer, tier);

        Console.info("Adding trades: " + recipes.size());

        try
        {
            PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO offers (Villager, ID, FirstItemID, FirstItemDamage, FirstItemNBT, FirstItemAmount, SecondItemID, SecondItemDamage, SecondItemNBT, SecondItemAmount, ThirdItemID, ThirdItemDamage, ThirdItemNBT, ThirdItemAmount, Tier, TradesLeft) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            for (int i = 0; i < recipes.size(); i++)
            {
                int id = i + trades.size();
                CustomRecipe recipe = recipes.get(i);

                statement.setString(1, uniqueID.toString());
                statement.setInt(2, id);

                statement.setInt(3, CraftMagicNumbers.getId(recipe.getBuyItem1().getItem()));
                statement.setInt(4, recipe.getBuyItem1().getData());


                statement.setBytes(5, Util.getNBT(recipe.getBuyItem1()));
                statement.setInt(6, recipe.getBuyItem1().count);

                if (recipe.hasSecondItem())
                {
                    statement.setInt(7, CraftMagicNumbers.getId(recipe.getBuyItem2().getItem()));
                    statement.setInt(8, recipe.getBuyItem2().getData());
                    statement.setBytes(9, Util.getNBT(recipe.getBuyItem2()));
                    statement.setInt(10, recipe.getBuyItem2().count);
                }
                else
                {
                    statement.setInt(7, 0);
                    statement.setInt(8, 0);
                    statement.setBytes(9, new byte[0]);
                    statement.setInt(10, 0);

                }

                statement.setInt(11, CraftMagicNumbers.getId(recipe.getBuyItem3().getItem()));
                statement.setInt(12, recipe.getBuyItem3().getData());
                statement.setBytes(13, Util.getNBT(recipe.getBuyItem3()));
                statement.setInt(14, recipe.getBuyItem3().count);

                statement.setInt(15, tier);

                int amountOfTrades = getDefaultNumberOfTrades();
                statement.setInt(16, amountOfTrades);

                statement.addBatch();

                recipe.setTier(tier);
                recipe.setTradesLeft(amountOfTrades);

                trades.add(recipe);
            }

            statement.executeBatch();
            statement.close();
            IO.getConnection().commit();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        //Particle effect when adding tier
        this.addEffect(new MobEffect(MobEffectList.REGENERATION.id, 200, 0));
    }

    private int getLastTier()
    {
        if (trades.size() == 0)
            return 0;
        else
            return ((CustomRecipe) trades.get(trades.size() - 1)).getTier();
    }

    private boolean isLastTier(CustomRecipe recipe)
    {
        return getLastTier() == recipe.getTier();
    }

    private boolean areAllTiersUnlocked()
    {
        return !VillagerConfig.hasTrades(carreer, getLastTier() + 1);
    }

    private static int getDefaultNumberOfTrades()
    {
        return 2 + TradeCraftPlugin.random.nextInt(6) + TradeCraftPlugin.random.nextInt(6);
    }
}
