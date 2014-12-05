package us.corenetwork.tradecraft;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.world.WorldSaveEvent;

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
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void EntityDeathEvent(EntityDeathEvent event)
    {
        if (event.getEntityType() == EntityType.VILLAGER)
        {
            final Entity villager = event.getEntity();
            TradeCraftVillager tcv = Villagers.getVillager(villager.getUniqueId().toString());
            if(tcv != null)
            {
            	tcv.setDead(true);
            }
        }
    }
    

    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void EntityPortalEvent(EntityPortalEvent  event)
    {
    	if (event.getEntityType() == EntityType.VILLAGER)
        {
            final Entity villager = event.getEntity();
            TradeCraftVillager tcv = Villagers.getVillager(villager.getUniqueId().toString());
            if(tcv != null)
            {
            	Logs.debug("Portal enter " + tcv.getUUID());
            	tcv.setPortaling(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void EntityPortalExitEvent(EntityPortalExitEvent  event)
    {
    	if (event.getEntityType() == EntityType.VILLAGER)
        {
            final Entity villager = event.getEntity();
            TradeCraftVillager tcv = Villagers.getVillager(villager.getUniqueId().toString());
            if(tcv != null)
            {
            	Logs.debug("portal exit " + tcv.getUUID());
            	tcv.setPortaling(false);
            }
        }
    }
 
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldSaveEvent(WorldSaveEvent event)
    {
    	if(event.getWorld().getName().equals("world"))
    		Villagers.SaveVillagers();
    }
}
