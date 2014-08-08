package us.corenetwork.tradecraft;

import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_7_R4.BiomeBase;
import net.minecraft.server.v1_7_R4.BiomeMeta;
import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.EntityVillager;
import net.minecraft.server.v1_7_R4.NBTReadLimiter;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Created by Matej on 23.2.2014.
 */
public class NMSVillagerManager {
    public static void register()
    {
        //Replace "Villager" entity type
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "c")).put("Villager", CustomVillager.class);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "d")).put(CustomVillager.class, "Villager");
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "e")).put(120, CustomVillager.class);
        ((Map) ReflectionUtils.getStatic(EntityTypes.class, "f")).put(CustomVillager.class, 120);

        //Replace all villagers in biomes
        BiomeBase[] biomes = (BiomeBase[]) ReflectionUtils.getStatic(BiomeBase.class, "biomes");
        for (BiomeBase biome : biomes)
        {
            if (biome == null)
                continue;

            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "as"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "at"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "au"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(BiomeBase.class, biome, "av"));

        }
    }

    private static void fixBiomeMeta(List<BiomeMeta> meta)
    {
        for (BiomeMeta m : meta)
        {
            if (m.b.equals(EntityVillager.class))
            {
                m.b = CustomVillager.class;
            }
        }
    }

    /***
     * Convert regular villager into custom villager.
     */
    public static void convert(Entity entity)
    {
        if (entity.getType() != EntityType.VILLAGER)
            return;

        EntityVillager nmsVillager = ((CraftVillager) entity).getHandle();

        World world = nmsVillager.world;
        Location location = entity.getLocation();
        int profession = nmsVillager.getProfession();

        CustomVillager newVillager = new CustomVillager(world, profession);
        newVillager.setPosition(location.getX(), location.getY(), location.getZ());
        newVillager.setAge(nmsVillager.getAge());

        world.removeEntity(nmsVillager);
        world.addEntity(newVillager, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public static boolean isCustomVillager(Entity entity)
    {
        return ((CraftEntity) entity).getHandle() instanceof CustomVillager;
    }

    public static int getType(Villager villager)
    {
        EntityVillager nmsVillager = ((CraftVillager) villager).getHandle();
        return nmsVillager.getProfession();
    }


    public static NBTReadLimiter UNLIMTED_NBT_READER_INSTANCE = new UnlimitedNBTLimiter();
    private static class UnlimitedNBTLimiter extends NBTReadLimiter
    {
        public UnlimitedNBTLimiter()
        {
            super(0);
        }

        @Override
        public void a(long l)
        {
        }
    }
}
