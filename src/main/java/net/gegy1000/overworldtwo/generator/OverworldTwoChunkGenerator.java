package net.gegy1000.overworldtwo.generator;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.HashCommon;
import net.gegy1000.overworldtwo.OverworldTwo;
import net.gegy1000.overworldtwo.noise.Noise;
import net.gegy1000.overworldtwo.noise.NoiseFactory;
import net.gegy1000.overworldtwo.noise.NormalizedNoise;
import net.gegy1000.overworldtwo.noise.OctaveNoise;
import net.gegy1000.overworldtwo.noise.PerlinNoise;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;

import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.*;

public class OverworldTwoChunkGenerator extends NoiseChunkGenerator {
    public static final OverworldTwoGenerationSettings OVERWORLD = createOverworld();
    public static final OverworldTwoGenerationSettings NETHER = createNether();

    public static final Codec<OverworldTwoChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeProvider.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeProvider),
            Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.field_236084_w_),
            OverworldTwoGenerationSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
    ).apply(instance, instance.stable(OverworldTwoChunkGenerator::new)));

    private static final int NOISE_RES_XZ = 1;
    private static final int NOISE_RES_Y = 2;

    private final Noise[] surfaceNoise;
    private final Noise tearNoise;
    private final ThreadLocal<BiomeCache> biomeCache;
    private final ThreadLocal<NoiseCache> noiseCache;
    private final Noise extraDensityNoise;
    private final OverworldTwoGenerationSettings settings;

    public OverworldTwoChunkGenerator(BiomeProvider biomes, long seed, OverworldTwoGenerationSettings settings) {
        super(biomes, seed, () -> settings.wrapped);
        this.settings = settings;

        SharedSeedRandom random = new SharedSeedRandom(seed);

        NoiseFactory surfaceNoise = surfaceNoise(settings);
        this.surfaceNoise = new Noise[] {
                surfaceNoise.create(random.nextLong()),
                surfaceNoise.create(random.nextLong()),
        };

        this.extraDensityNoise = extraDensityNoise(settings).create(random.nextLong());

        NoiseFactory tearNoise = tearNoise(settings);
        this.tearNoise = tearNoise.create(random.nextLong());

        this.biomeCache = ThreadLocal.withInitial(() -> new BiomeCache(128, biomes));
        this.noiseCache = ThreadLocal.withInitial(() -> new NoiseCache(128, this.noiseSizeY + 1));
    }

    private static NoiseFactory surfaceNoise(OverworldTwoGenerationSettings settings) {
        ScalingSettings config = settings.wrapped.getNoise().func_236171_b_();

        OctaveNoise.Builder octaves = OctaveNoise.builder()
                .setHorizontalFrequency(1.0 / config.func_236151_a_())
                .setVerticalFrequency(1.0 / config.func_236151_a_())
                .setLacunarity(settings.surfaceLacunarity)
                .setPersistence(1.0 / settings.surfacePersistence);

        octaves.add(PerlinNoise.create(), 6);

        return NormalizedNoise.of(octaves.build());
    }

    private static NoiseFactory tearNoise(OverworldTwoGenerationSettings settings) {
        ScalingSettings config = settings.wrapped.getNoise().func_236171_b_();

        OctaveNoise.Builder octaves = OctaveNoise.builder()
                .setHorizontalFrequency(1.0 / config.func_236154_c_())
                .setVerticalFrequency(1.0 / config.func_236155_d_())
                .setLacunarity(settings.tearLacunarity)
                .setPersistence(1.0 / settings.tearPersistence);

        octaves.add(PerlinNoise.create(), 4);

        return NormalizedNoise.of(octaves.build());
    }

    private static NoiseFactory extraDensityNoise(OverworldTwoGenerationSettings settings) {
        OctaveNoise.Builder octaves = OctaveNoise.builder()
                .setHorizontalFrequency(1.0 / settings.extraDensityScale)
                .setVerticalFrequency(1.0 / settings.extraDensityScale)
                .setLacunarity(settings.extraDensityLacunarity)
                .setPersistence(1.0 / settings.extraDensityPersistence);

        octaves.add(PerlinNoise.create(), 4);

        return NormalizedNoise.of(octaves.build());
    }

    public static void register() {
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, new ResourceLocation(OverworldTwo.ID, "overworld_two"), CODEC);
    }

    private static OverworldTwoGenerationSettings createOverworld() {
        DimensionStructuresSettings structures = WorldGenRegistries.NOISE_SETTINGS.getValueForKey(DimensionSettings.field_242734_c).getStructures();

        // Vanilla: 1.0, 1.0, 40.0, 22.0
        ScalingSettings noiseSampler = new ScalingSettings(24.0, 24.0, 40.0, 18.0);
        NoiseSettings noise = new NoiseSettings(
                256,
                noiseSampler,
                new SlideSettings(-10, 3, 0),
                new SlideSettings(-30, 0, 0),
                NOISE_RES_XZ, NOISE_RES_Y,
                1.0,
                -60.0 / (256.0 / 2.0),
                true,
                true,
                false,
                false
        );

        DimensionSettings type =  new DimensionSettings(
                structures, noise,
                Blocks.STONE.getDefaultState(),
                Blocks.WATER.getDefaultState(),
                -10, 0, 63,
                false
        );

        return new OverworldTwoGenerationSettings(
                type,
                1.7, 1.8,
                1.35, 2.0,
                150, 1.5, 1.4
        );
    }

    private static OverworldTwoGenerationSettings createNether() {
        DimensionStructuresSettings structures = WorldGenRegistries.NOISE_SETTINGS.getValueForKey(DimensionSettings.field_242736_e).getStructures();
        Map<Structure<?>, StructureSeparationSettings> map = Maps.newHashMap(DimensionStructuresSettings.field_236191_b_);
        map.put(Structure.RUINED_PORTAL, new StructureSeparationSettings(25, 10, 34222645));

        // Vanilla: 1.0, 3.0, 80.0, 60.0
        ScalingSettings noiseSampler = new ScalingSettings(48.0, 18.0, 120.0, 40.0);
        NoiseSettings noise = new NoiseSettings(
                128,
                noiseSampler,
                new SlideSettings(120, 3, 0),
                new SlideSettings(320, 4, -1),
                1,
                2,
                0.0,
                0.019921875,
                false,
                false,
                false,
                false
        );

        DimensionSettings type = new DimensionSettings(
                new DimensionStructuresSettings(Optional.ofNullable(structures.func_236199_b_()), map),
                noise,
                Blocks.NETHERRACK.getDefaultState(),
                Blocks.LAVA.getDefaultState(),
                0, 0, 32,
                false
        );

        return new OverworldTwoGenerationSettings(
                type,
                1.7, 1.8,
                1.5, 1.75,
                // The nether doesn't use these values
                150, 1.5, 1.4
        );
    }

    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return OverworldTwoChunkGenerator.CODEC;
    }


    @Override
    public ChunkGenerator func_230349_a_(long seed) {
        return new OverworldTwoChunkGenerator(this.biomeProvider.getBiomeProvider(seed), seed, this.settings);
    }


    private static float biomeWeight(int x, int z) {
        int idx = (x + 2) + (z + 2) * 5;
        return field_236081_j_[idx];
    }

    private SurfaceParameters sampleSurfaceParameters(int x, int z) {
        BiomeCache cache = biomeCache.get();

        float totalScale = 0.0F;
        float totalDepth = 0.0F;
        float totalWeight = 0.0F;

        float depthHere = cache.get(x, z).getDepth();

        for (int oz = -2; oz <= 2; oz++) {
            for (int ox = -2; ox <= 2; ox++) {
                Biome biome = cache.get(x + ox, z + oz);
                float depth = biome.getDepth();
                float scale = biome.getScale();

                float weight = biomeWeight(ox, oz) / (depth + 2.0F);
                if (depth > depthHere) {
                    weight *= 0.5F;
                }

                totalScale += scale * weight;
                totalDepth += depth * weight;
                totalWeight += weight;
            }
        }

        float depth = totalDepth / totalWeight;
        float scale = totalScale / totalWeight;

        return new SurfaceParameters((depth * 0.5F) - 0.125F, (scale * 0.9F) + 0.1F);
    }

    @Override
    public double[] func_222547_b(int x, int z) {
        return this.noiseCache.get().get(new double[this.noiseSizeY + 1], x, z);
    }


    @Override
    public void fillNoiseColumn(double[] buffer, int x, int z) {
        this.noiseCache.get().get(buffer, x, z);
    }


    public void fillNoiseCache(double[] buffer, int x, int z) {
        NoiseSettings noiseConfig = this.settings.wrapped.getNoise();
        SurfaceParameters params = sampleSurfaceParameters(x, z);
        double scaledDepth = params.depth * 0.265625D;
        double scaledScale = 96.0D / params.scale;

        double topTarget = noiseConfig.func_236172_c_().func_236186_a_();
        double topSize = noiseConfig.func_236172_c_().func_236188_b_();
        double topOffset = noiseConfig.func_236172_c_().func_236189_c_();
        double bottomTarget = noiseConfig.func_236173_d_().func_236186_a_();
        double bottomSize = noiseConfig.func_236173_d_().func_236188_b_();
        double bottomOffset = noiseConfig.func_236173_d_().func_236189_c_();
        double randomDensityOffset = noiseConfig.func_236179_j_() ? this.extraDensityNoiseAt(x, z) : 0.0D;
        double densityFactor = noiseConfig.func_236176_g_();
        double densityOffset = noiseConfig.func_236177_h_();

        for(int y = 0; y <= this.noiseSizeY; ++y) {
            double noise = this.getNoiseAt(x, y, z);
            double yOffset = 1.0D - (double) y * 2.0D / (double)this.noiseSizeY + randomDensityOffset;
            double density = yOffset * densityFactor + densityOffset;
            double falloff = (density + scaledDepth) * scaledScale;
            if (falloff > 0.0D) {
                noise += falloff * 4.0D;
            } else {
                noise += falloff;
            }
            double slide;
            if (topSize > 0.0D) {
                slide = ((double)(this.noiseSizeY - y) - topOffset) / topSize;
                noise = MathHelper.clampedLerp(topTarget, noise, slide);
            }

            if (bottomSize > 0.0D) {
                slide = ((double) y - bottomOffset) / bottomSize;
                noise = MathHelper.clampedLerp(bottomTarget, noise, slide);
            }

            buffer[y] = noise;
        }
    }

    private double getNoiseAt(int x, int y, int z) {
        double tearNoise = this.tearNoise.get(x, y, z) * 15.0;
        double surfaceNoise;

        if (tearNoise <= 0.0) {
            surfaceNoise = this.surfaceNoise[0].get(x, y, z);
        } else if (tearNoise >= 1.0) {
            surfaceNoise = this.surfaceNoise[1].get(x, y, z);
        } else {
            double left = this.surfaceNoise[0].get(x, y, z);
            double right = this.surfaceNoise[1].get(x, y, z);
            surfaceNoise = MathHelper.lerp(tearNoise, left, right);
        }

        return surfaceNoise * 200;
    }

    protected double extraDensityNoiseAt(int x, int z) {
        double rawDensity = this.extraDensityNoise.get(x, 10.0D, z);
        double scaledDensity;
        if (rawDensity < 0.0D) {
            scaledDensity = -rawDensity * 0.3D;
        } else {
            scaledDensity = rawDensity;
        }

        double finalDensity = scaledDensity * 24.575625D - 2.0D;
        return finalDensity < 0.0D ? finalDensity * 0.009486607142857142D : Math.min(finalDensity, 1.0D) * 0.006640625D;
    }

    private static class SurfaceParameters {
        private final float depth;
        private final float scale;

        public SurfaceParameters(float depth, float scale) {
            this.depth = depth;
            this.scale = scale;
        }
    }

    private static class BiomeCache {
        private final long[] keys;
        private final Biome[] values;

        private final int mask;
        private final BiomeProvider source;

        private BiomeCache(int size, BiomeProvider source) {
            this.source = source;
            size = MathHelper.smallestEncompassingPowerOfTwo(size);
            this.mask = size - 1;

            this.keys = new long[size];
            Arrays.fill(this.keys, Long.MIN_VALUE);
            this.values = new Biome[size];
        }

        public Biome get(int x, int z) {
            long key = key(x, z);
            int idx = hash(key) & this.mask;

            // if the entry here has a key that matches ours, we have a cache hit
            if (this.keys[idx] == key) {
                return this.values[idx];
            }

            // cache miss: sample the source and put the result into our cache entry
            Biome sampled = source.getNoiseBiome(x, 0, z);
            this.values[idx] = sampled;
            this.keys[idx] = key;

            return sampled;
        }

        private static int hash(long key) {
            return (int) HashCommon.mix(key);
        }

        private static long key(int x, int z) {
            return ChunkPos.asLong(x, z);
        }
    }

    private class NoiseCache {
        private final long[] keys;
        private final double[] values;

        private final int mask;

        private NoiseCache(int size, int noiseSize) {
            size = MathHelper.smallestEncompassingPowerOfTwo(size);
            this.mask = size - 1;

            this.keys = new long[size];
            Arrays.fill(this.keys, Long.MIN_VALUE);
            this.values = new double[size * noiseSize];
        }

        public double[] get(double[] buffer, int x, int z) {
            long key = key(x, z);
            int idx = hash(key) & this.mask;

            // if the entry here has a key that matches ours, we have a cache hit
            if (this.keys[idx] == key) {
                // Copy values into buffer
                System.arraycopy(this.values, idx * buffer.length, buffer, 0, buffer.length);
            } else {
                // cache miss: sample and put the result into our cache entry

                // Sample the noise column to store the new values
                fillNoiseCache(buffer, x, z);

                // Create copy of the array
                System.arraycopy(buffer, 0, this.values, idx * buffer.length, buffer.length);

                this.keys[idx] = key;
            }

            return buffer;
        }

        private int hash(long key) {
            return (int) HashCommon.mix(key);
        }

        private long key(int x, int z) {
            return ChunkPos.asLong(x, z);
        }
    }
}
