package us.corenetwork.tradecraft;

import net.minecraft.server.v1_7_R1.*;
import org.bukkit.Bukkit;

/**
 * Created by Matej on 23.2.2014.
 */
public class CustomVillager extends EntityVillager {

    public CustomVillager(World world) {
        super(world);
        System.out.println("creating villager 1...");
    }

    public CustomVillager(World world, int i) {
        super(world, i);
        System.out.println("creating villager 2...");
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
        MerchantRecipe recipe = new MerchantRecipe(new ItemStack((Block) Block.REGISTRY.a("dirt"), 1), new ItemStack((Block) Block.REGISTRY.a("gravel"), 1), new ItemStack((Block) Block.REGISTRY.a("diamond_block"), 1));

        MerchantRecipeList list = new MerchantRecipeList();
        list.add(recipe);

        return list;
    }

    /**
     * Activated when player makes a trade
     */
    @Override
    public void a(MerchantRecipe recipe)
    {
    }


}
