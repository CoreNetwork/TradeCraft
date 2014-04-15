package us.corenetwork.tradecraft;

import net.minecraft.server.v1_7_R2.Block;
import net.minecraft.server.v1_7_R2.EntityAgeable;
import net.minecraft.server.v1_7_R2.EntityHuman;
import net.minecraft.server.v1_7_R2.EntityPlayer;
import net.minecraft.server.v1_7_R2.EntityVillager;
import net.minecraft.server.v1_7_R2.ItemStack;
import net.minecraft.server.v1_7_R2.MerchantRecipe;
import net.minecraft.server.v1_7_R2.MerchantRecipeList;
import net.minecraft.server.v1_7_R2.MobEffect;
import net.minecraft.server.v1_7_R2.MobEffectList;
import net.minecraft.server.v1_7_R2.Village;
import net.minecraft.server.v1_7_R2.World;

import org.bukkit.Bukkit;

/**
 * Created by Matej on 23.2.2014.
 */
public class CustomVillager extends EntityVillager {
	
	TradeCraftVillager tradeCraftVillager = null;

	private String lastTradingPlayer = null;
    private boolean overrideName = false;

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
        Bukkit.getScheduler().scheduleSyncDelayedTask(TradeCraftPlugin.instance, new Runnable()
        {
            @Override
            public void run()
            {
                loadVillagerData();

                if (tradeCraftVillager.getTrades().size() == 0)
                {
                    tradeCraftVillager.addTier(0);
                }
            }
        });
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entityAgeable) 
    {
        return b(entityAgeable);
    }


    /**
     * Returns list of offers
     */
    @Override
    public MerchantRecipeList getOffers(EntityHuman entityHuman) 
    {
    	MerchantRecipeList trades = tradeCraftVillager.getTrades();
    	
        if (trades == null || trades.size() == 0)
        {
            Logs.severe("Villager " + uniqueID.toString() + " has no trades!");

            CustomRecipe recipe = new CustomRecipe(new ItemStack((Block) Block.REGISTRY.a("bedrock"), 65), new ItemStack((Block) Block.REGISTRY.a("bedrock"), 1));
            //recipe.lockManually();

            MerchantRecipeList list = new MerchantRecipeList();
            list.add(recipe);

            return list;
        }

        return trades;
    }

    /**
     * Activated when new player starts trading (or null when nobody is trading (trading window closes))
     */
    @Override
    public void a_(EntityHuman entityHuman)
    {
        if (entityHuman == null) //Nobody is trading now
        {
            if (createNewTier)
            {
            	tradeCraftVillager.addTier(tradeCraftVillager.getLastTier() + 1);
                tradeCraftVillager.refreshAllTrades();

                //Particle effects and increasing village population
                Village village = (Village) ReflectionUtils.get(EntityVillager.class, this, "village");

                if (village != null && lastTradingPlayer != null) {
                    Logs.debugIngame("Reputation UP!");
                    this.world.broadcastEntityEffect(this, (byte) 14);
                    village.a(lastTradingPlayer, 1);
                }

                //Particle effect when new tier is created
                this.addEffect(new MobEffect(MobEffectList.REGENERATION.id, 200, 0));
            }
            else if (restockAll)
            {
            	tradeCraftVillager.refreshAllTrades();

                //Particle effect when restocking
                this.addEffect(new MobEffect(MobEffectList.REGENERATION.id, 200, 0));
            }

            restockAll = false;
            createNewTier = false;
        }
        else
            Logs.debugIngame("Trading with: " + tradeCraftVillager.getCareer());

        super.a_(entityHuman);
    }


    /**
     * Called when somebody right clicks on villager
     * @return has trade window been opened
     */
    @Override
    public boolean a(EntityHuman entityHuman)
    {
        overrideName = true;
        boolean returningBool = super.a(entityHuman);
        overrideName = false;

        return returningBool;
    }

    @Override
    public String getCustomName()
    {
        if (overrideName)
            return tradeCraftVillager.getCareer();
        else
            return super.getCustomName();
    }

    /**
     * Activated when player makes a trade
     */
    @Override
    public void a(MerchantRecipe vanillaRecipe)
    {
    	//DEBUG variable, just to see better
    	String suuid = uniqueID.toString();
    	
        // Yes/No sound
        this.makeSound("mob.villager.yes", this.be(), this.bf());

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

            lastTradingPlayer = human.getName();
        }

        CustomRecipe recipe = (CustomRecipe) vanillaRecipe;
        if (tradeCraftVillager.getTrades() == null)
            return;

        int tradeID = tradeCraftVillager.getTrades().indexOf(recipe);
        if (tradeID < 0)
        {
            Logs.severe("Player completed unknown trade on villager " + uniqueID.toString() + "! ");
            return;
        }
        tradeCraftVillager.useTrade(recipe);
        
        Logs.debugIngame("Trade completed! Left:" + recipe.getTradesLeft());

        if (tradeCraftVillager.areAllTiersUnlocked())
        {
            if (recipe.getTradesPerformed() == 1 || random.nextDouble() < Settings.getDouble(Setting.ALL_UNLOCKED_REFRESH_CHANCE))
            {
                restockAll = true;
            }
        }
        else
        {
            if (tradeCraftVillager.isLastTier(recipe) || random.nextInt(100) < 20) //Add new tier when on last trade or with 20% chance
            {
                createNewTier = true;
            }
        }
    }

    public void loadVillagerData()
    {
    	if (Villagers.exists(uniqueID.toString()) == false)
    	{
    		String newCareer = VillagerConfig.getRandomCareer(getProfession());
            if (newCareer == null)
            	newCareer = "NO_CAREER";
    		Villagers.create(uniqueID.toString(), newCareer);
    	}
    	tradeCraftVillager = Villagers.getVillager(uniqueID.toString());
    }
}
