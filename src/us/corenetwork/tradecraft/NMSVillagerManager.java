package us.corenetwork.tradecraft;

import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.BiomeMeta;
import net.minecraft.server.v1_7_R1.EntityTypes;
import net.minecraft.server.v1_7_R1.EntityVillager;

import java.util.List;
import java.util.Map;

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
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(biome, "as"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(biome, "at"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(biome, "au"));
            fixBiomeMeta((List<BiomeMeta>) ReflectionUtils.get(biome, "av"));

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
}
