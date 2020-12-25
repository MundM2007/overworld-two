package net.gegy1000.overworldtwo.mixin;

import net.gegy1000.overworldtwo.config.OverworldTwoConfig;
import net.gegy1000.overworldtwo.generator.OverworldTwoChunkGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(DimensionType.class)
public class MixinDimensionType {

    @Inject(at = @At("RETURN"), method = "getNetherChunkGenerator(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/registry/Registry;J)Lnet/minecraft/world/gen/ChunkGenerator;", cancellable = true)
    private static void netherDimensionOverworldTwo(Registry<Biome> registry, Registry<DimensionSettings> dimSettings, long seed, CallbackInfoReturnable<ChunkGenerator> cir) {
        if (OverworldTwoConfig.get().generateNether)
            cir.setReturnValue(new OverworldTwoChunkGenerator(cir.getReturnValue().getBiomeProvider(), seed, OverworldTwoChunkGenerator.NETHER));
    }
}
