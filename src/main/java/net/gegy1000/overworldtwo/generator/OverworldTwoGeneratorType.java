package net.gegy1000.overworldtwo.generator;


import net.gegy1000.overworldtwo.OverworldTwo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;

import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public final class OverworldTwoGeneratorType extends ForgeWorldType {

    private OverworldTwoGeneratorType() {
        super(null);
    }

    public static void register() {
        OverworldTwoGeneratorType type = new OverworldTwoGeneratorType();
        OverworldTwo.LOGGER.warn("The registry warning that is about to be printed is harmless.");
        type.setRegistryName(new ResourceLocation("overworld_two"));
        ForgeRegistries.WORLD_TYPES.register(type);
    }

    @Override
    public DimensionGeneratorSettings createSettings(DynamicRegistries dynamicRegistryManager, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        Registry<DimensionType> dimensions = dynamicRegistryManager.getRegistry(Registry.DIMENSION_TYPE_KEY);
        Registry<Biome> biomes = dynamicRegistryManager.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionSettings> chunkgens = dynamicRegistryManager.getRegistry(Registry.NOISE_SETTINGS_KEY);
        SimpleRegistry<Dimension> dimensionOptions = DimensionType.getDefaultSimpleRegistry(dimensions, biomes, chunkgens, seed);

        return new DimensionGeneratorSettings(seed, generateStructures, false, DimensionGeneratorSettings.func_242749_a(dimensions, dimensionOptions, this.createChunkGenerator(biomes, chunkgens, seed, (String)null)));
    }

    @Override
    public ChunkGenerator createChunkGenerator(Registry<Biome> registry, Registry<DimensionSettings> settings, long seed, String generatorSettings) {
        return new OverworldTwoChunkGenerator(new OverworldBiomeProvider(seed, false, false, registry), seed, OverworldTwoChunkGenerator.OVERWORLD);
    }
}
