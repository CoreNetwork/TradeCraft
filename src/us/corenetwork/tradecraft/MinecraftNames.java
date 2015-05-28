package us.corenetwork.tradecraft;


import net.minecraft.server.v1_8_R3.Item;

/**
 * Created by Matej on 2.12.2014.
 */
public class MinecraftNames
{
    public static Integer getEnchantmentId(String name)
    {
        net.minecraft.server.v1_8_R3.Enchantment nmsEnchantment = net.minecraft.server.v1_8_R3.Enchantment.getByName(name);
        if (nmsEnchantment == null)
            return null;

        return nmsEnchantment.id;
    }

    public static Integer getMaterialId(String name)
    {
        Item item = Item.d(name);
        if (item == null)
            return null;

        return Item.getId(item);
    }
}
