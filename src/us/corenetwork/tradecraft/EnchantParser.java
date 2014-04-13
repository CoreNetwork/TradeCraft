package us.corenetwork.tradecraft;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Matej on 29.3.2014.
 */
public class EnchantParser extends NodeParser
{
    private ItemStack itemA;
    private ItemStack itemB;
    private ItemStack enchantedItem;

    public EnchantParser(ItemStack enchantedItem, ItemStack itemA, ItemStack itemB)
    {
        this.itemA = itemA;
        this.itemB = itemB;
        this.enchantedItem = enchantedItem;
    }

    public void parse(List<?> nodes)
    {
        parseNodeList(nodes);

    }

    @Override
    protected void parseNode(String type, LinkedHashMap<?, ?> node)
    {
        if (type.equals("enchant"))
        {
            Number enchantID = (Number) node.get("id");
            if (enchantID == null)
            {
                Logs.warning("Invalid trades config: Missing enchant ID!");
                return;
            }
            Enchantment enchantment = Enchantment.getById(enchantID.intValue());
            if (enchantment == null)
            {
                Logs.warning("Invalid enchantment ID: " + enchantID);
                return;

            }

            int enchantLevel = VillagerConfig.getRandomNumber(node.get("level"));
            if (enchantLevel == 0)
            {
                Logs.warning("Invalid trades config: Missing or invalid enchant level!");
                return;
            }

            Object bonusA = node.get("bonusAmountA");
            if (bonusA != null && itemA != null)
            {
                int bonusAmount = VillagerConfig.getRandomNumber(bonusA);
                itemA.setAmount(Math.min(itemA.getAmount() + bonusAmount, itemA.getType().getMaxStackSize()));
            }

            Object bonusB = node.get("bonusAmountB");
            if (bonusB != null && itemB != null)
            {
                int bonusAmount = VillagerConfig.getRandomNumber(bonusB);
                itemB.setAmount(Math.min(itemB.getAmount() + bonusAmount, itemB.getType().getMaxStackSize()));
            }

            if (enchantedItem.getType() == Material.ENCHANTED_BOOK)
            {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedItem.getItemMeta();
                meta.addStoredEnchant(enchantment, enchantLevel, true);
                enchantedItem.setItemMeta(meta);
            }
            else
                enchantedItem.addUnsafeEnchantment(enchantment, enchantLevel);
        }
    }
}
