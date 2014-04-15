package us.corenetwork.tradecraft;

import java.util.List;

import net.minecraft.server.v1_7_R2.MerchantRecipeList;

import org.bukkit.craftbukkit.v1_7_R2.util.CraftMagicNumbers;

import us.corenetwork.tradecraft.db.AddOfferTask;
import us.corenetwork.tradecraft.db.DbWorker;
import us.corenetwork.tradecraft.db.RefreshOfferTask;

/**
 * Helper class to keep needed villager data in memory. We dont keep the data in
 * CustomEntityVillager because it gets loaded/unloaded very often We will look
 * up the data stored here from the CustomEntityVillager
 * 
 * @author Ginaw
 * 
 */
public class TradeCraftVillager {

	private String UUID = null;
	private String career = "NOT_INITIALIZED";
	private MerchantRecipeList trades;

	public TradeCraftVillager(String UUID, String career) 
	{
		this.UUID = UUID;
		this.career = career;
		trades = new MerchantRecipeList();
	}

	public String getUUID()
	{
		return UUID;
	}

	public String getCareer() 
	{
		return career;
	}

	public MerchantRecipeList getTrades() 
	{
		return trades;
	}

	public void useTrade(CustomRecipe recipe) 
	{
		recipe.useTrade();
		saveModifiedRecipe(recipe);
	}
	
	public void refreshAllTrades() 
	{
		for (int i = 0; i < trades.size(); i++) 
		{
			CustomRecipe recipe = (CustomRecipe) trades.get(i);
			recipe.restock();
			saveModifiedRecipe(recipe);
		}
	}

	public boolean isLastTier(CustomRecipe recipe)
    {
        return getLastTier() == recipe.getTier();
    }
    
	public boolean areAllTiersUnlocked()
    {
        return !VillagerConfig.hasTrades(getCareer(), getLastTier() + 1);
    }
    
	public int getLastTier()
    {
        if (trades.size() == 0)
            return 0;
        else
            return ((CustomRecipe) trades.get(trades.size() - 1)).getTier();
    }

    public void addTier(int tier)
    {
    	 List<CustomRecipe> recipes = VillagerConfig.getTrades(getCareer(), tier);
         
    	 Logs.debugIngame("Adding trades: " + recipes.size());         
         
         for(CustomRecipe recipe : recipes)
         {
             int amountOfTrades = Villagers.getDefaultNumberOfTrades();
             recipe.setTier(tier);
             recipe.setTradesLeft(amountOfTrades);
             recipe.setTradesPerformed(0);
             recipe.setTradeID(trades.size());
             addRecipe(recipe);
             saveRecipe(recipe);
         }
    }

	public void addRecipe(CustomRecipe recipe) 
	{
		trades.add(recipe);
	}

	private void saveRecipe(CustomRecipe recipe)
	{
		AddOfferTask task;
		if(recipe.hasSecondItem())
		{
			task = new AddOfferTask(UUID, recipe.getTradeID(), 
					CraftMagicNumbers.getId(recipe.getBuyItem1().getItem()), recipe.getBuyItem1().getData(), Util.getNBT(recipe.getBuyItem1()), recipe.getBuyItem1().count, 
					CraftMagicNumbers.getId(recipe.getBuyItem2().getItem()), recipe.getBuyItem2().getData(), Util.getNBT(recipe.getBuyItem2()), recipe.getBuyItem2().count, 
					CraftMagicNumbers.getId(recipe.getBuyItem3().getItem()), recipe.getBuyItem3().getData(), Util.getNBT(recipe.getBuyItem3()), recipe.getBuyItem3().count, 
					recipe.getTier(), recipe.getTradesLeft(), recipe.getTradesPerformed());
		}
		else
		{
			task = new AddOfferTask(UUID, recipe.getTradeID(), 
					CraftMagicNumbers.getId(recipe.getBuyItem1().getItem()), recipe.getBuyItem1().getData(), Util.getNBT(recipe.getBuyItem1()), recipe.getBuyItem1().count,  
					CraftMagicNumbers.getId(recipe.getBuyItem3().getItem()), recipe.getBuyItem3().getData(), Util.getNBT(recipe.getBuyItem3()), recipe.getBuyItem3().count, 
					recipe.getTier(), recipe.getTradesLeft(), recipe.getTradesPerformed());
		}
		DbWorker.queue.add(task);
	}
	
	private void saveModifiedRecipe(CustomRecipe recipe)
	{
		DbWorker.queue.add(new RefreshOfferTask(UUID, recipe.getTradeID(), recipe.getTradesLeft(), recipe.getTradesPerformed()));
	}
}
