package us.corenetwork.tradecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.UUID;

/**
 * Created by Matej on 24.2.2014.
 */
public class TradeCraftListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        //Replace spawned villager if new one spawns
        if (event.getEntityType() == EntityType.VILLAGER)
        {
            final Entity villager = event.getEntity();
            if (!NMSVillagerManager.isCustomVillager(villager))
            {
                Bukkit.getScheduler().runTask(TradeCraftPlugin.instance, new Runnable() {
                    @Override
                    public void run() {
                        NMSVillagerManager.convert(villager);
                    }
                });
            }
        }
    }
}
