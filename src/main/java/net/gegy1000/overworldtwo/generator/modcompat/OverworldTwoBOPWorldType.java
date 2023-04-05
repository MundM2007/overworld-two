package net.gegy1000.overworldtwo.generator.modcompat;

import biomesoplenty.common.world.BOPBiomeProvider;
import biomesoplenty.common.world.BOPDimensionType;
import net.gegy1000.overworldtwo.OverworldTwo;
import net.gegy1000.overworldtwo.generator.OverworldTwoChunkGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

public class OverworldTwoBOPWorldType extends ForgeWorldType {
    public OverworldTwoBOPWorldType() {
        super(null);
    }

    @Override
    public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, String generatorSettings) {
        return new OverworldTwoChunkGenerator(new BOPBiomeProvider(seed, biomeRegistry), seed, OverworldTwoChunkGenerator.OVERWORLD);
    }

    @Override
    public DimensionGeneratorSettings createSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {

        Registry<Biome> biomeRegistry = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.getRegistry(Registry.NOISE_SETTINGS_KEY);
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.getRegistry(Registry.DIMENSION_TYPE_KEY);
        return new DimensionGeneratorSettings(seed, generateStructures, generateLoot, DimensionGeneratorSettings.func_242749_a(dimensionTypeRegistry, BOPDimensionType.bopDimensions(biomeRegistry, dimensionSettingsRegistry, seed), this.createChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed, (String)null)));
    }

    public static void register() {
        OverworldTwoBOPWorldType bopWorldType = new OverworldTwoBOPWorldType();
        bopWorldType.setRegistryName(new ResourceLocation(OverworldTwo.ID, "biomesoplenty"));
        ForgeRegistries.WORLD_TYPES.register(bopWorldType);
    }
}
