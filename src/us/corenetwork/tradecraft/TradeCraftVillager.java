package us.corenetwork.tradecraft;

import java.util.List;

import net.minecraft.server.v1_7_R2.MerchantRecipeList;

/**
 * Helper class to keep needed villager data in memory. We dont keep the data in
 * CustomEntityVillager because it gets loaded/unloaded very often We will look
 * up the data stored here from the CustomEntityVillager
 * 
 * @author Ginaw
 * 
 */
public class TradeCraftVillager {

	private String career = "NOT_INITIALIZED";
	private MerchantRecipeList trades;

	// Persistance helpers
	private boolean isNew = false; // Indicates wheter the villie itself needs
								   // saving (ie. was created since last save)

	public TradeCraftVillager(String career) 
	{
		this.career = career;
		trades = new MerchantRecipeList();
	}

	public boolean getIsNew() 
	{
		return isNew;
	}

	public void setIsNew(boolean value) 
	{
		isNew = value;
	}

	public String getCareer() 
	{
		return career;
	}

	public MerchantRecipeList getTrades() 
	{
		return trades;
	}

	public void refreshAllTrades() 
	{
		for (int i = 0; i < trades.size(); i++) 
		{
			((CustomRecipe) trades.get(i)).restock();
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
             
             recipe.setIsNew(true);
             trades.add(recipe);
             
         }
         
    }

	public void addRecipe(CustomRecipe recipe) {
		trades.add(recipe);
	}
}
