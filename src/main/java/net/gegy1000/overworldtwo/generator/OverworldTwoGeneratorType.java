package net.gegy1000.overworldtwo.generator;


import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class OverworldTwoGeneratorType extends BiomeGeneratorTypeScreens {
    public static final BiomeGeneratorTypeScreens INSTANCE = new OverworldTwoGeneratorType();

    private OverworldTwoGeneratorType() {
        super("overworld_two");
    }

    public static void register() {
        BiomeGeneratorTypeScreens.field_239068_c_.add(INSTANCE);
    }


    @Override
    protected ChunkGenerator func_241869_a(Registry<Biome> registry, Registry<DimensionSettings> settings, long seed) {
        return new OverworldTwoChunkGenerator(new OverworldBiomeProvider(seed, false, false, registry), seed, OverworldTwoChunkGenerator.OVERWORLD);
    }
}
