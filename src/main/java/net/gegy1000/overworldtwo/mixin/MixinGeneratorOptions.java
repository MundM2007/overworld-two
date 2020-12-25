package net.gegy1000.overworldtwo.mixin;

import com.google.common.base.MoreObjects;

import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;


import net.gegy1000.overworldtwo.generator.OverworldTwoChunkGenerator;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Properties;
import java.util.Random;

@Mixin(DimensionGeneratorSettings.class)
public class MixinGeneratorOptions {
    @Inject(method = "func_242753_a", at = @At("HEAD"), cancellable = true)
    private static void injectOverworldTwo(DynamicRegistries dynamicRegistryManager, Properties properties, CallbackInfoReturnable<DimensionGeneratorSettings> cir) {
        // no server.properties file generated
        if (properties.get("level-type") == null) {
            return;
        }

        // check for our world type and return if so
        if (properties.get("level-type").toString().trim().toLowerCase().equals("overworld_two")) {
            // get or generate seed
            String seedField = (String) MoreObjects.firstNonNull(properties.get("level-seed"), "");
            long seed = new Random().nextLong();
            if (!seedField.isEmpty()) {
                try {
                    long parsedSeed = Long.parseLong(seedField);
                    if (parsedSeed != 0L) {
                        seed = parsedSeed;
                    }
                } catch (NumberFormatException var14) {
                    seed = seedField.hashCode();
                }
            }

            // get other misc data
            Registry<DimensionType> dimensions = dynamicRegistryManager.getRegistry(Registry.DIMENSION_TYPE_KEY);
            Registry<Biome> biomes = dynamicRegistryManager.getRegistry(Registry.BIOME_KEY);
            Registry<DimensionSettings> chunkgens = dynamicRegistryManager.getRegistry(Registry.NOISE_SETTINGS_KEY);
            SimpleRegistry<Dimension> dimensionOptions = DimensionType.getDefaultSimpleRegistry(dimensions, biomes, chunkgens, seed);

            String generate_structures = (String)properties.get("generate-structures");
            boolean generateStructures = generate_structures == null || Boolean.parseBoolean(generate_structures);

            // return our chunk generator
            cir.setReturnValue(new DimensionGeneratorSettings(seed, generateStructures, false, DimensionGeneratorSettings.func_242749_a(dimensions, dimensionOptions, new OverworldTwoChunkGenerator(new OverworldBiomeProvider(seed, false, false, biomes), seed, OverworldTwoChunkGenerator.OVERWORLD))));
        }
    }
}