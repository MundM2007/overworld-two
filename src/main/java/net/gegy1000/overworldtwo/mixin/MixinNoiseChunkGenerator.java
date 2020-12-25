package net.gegy1000.overworldtwo.mixin;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.structure.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {
    @Redirect(
            method = "func_230352_b_",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/structure/Structure;field_236384_t_:Ljava/util/List;"
            )
    )
    private List<Structure<?>> disableStructureIterator(IWorld world, StructureManager structures, IChunk chunk) {
        return Collections.emptyList();
    }

    @Inject(
            method = "func_230352_b_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/IChunk;getPos()Lnet/minecraft/util/math/ChunkPos;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void collectStructures(
            IWorld world, StructureManager structures, IChunk chunk,
            CallbackInfo ci, ObjectList<StructurePiece> pieces, ObjectList<JigsawJunction> junctions
    ) {
        ChunkPos chunkPos = chunk.getPos();

        int minX = chunkPos.getXStart();
        int minZ = chunkPos.getZStart();
        int maxX = chunkPos.getXEnd();
        int maxZ = chunkPos.getZEnd();

        Map<Structure<?>, LongSet> structureReferences = chunk.getStructureReferences();
        if (structureReferences.isEmpty()) {
            return;
        }

        for (Structure<?> feature : Structure.field_236384_t_) {
            LongSet references = structureReferences.get(feature);
            if (references == null) continue;

            LongIterator referenceIterator = references.iterator();

            while (referenceIterator.hasNext()) {
                long packedReference = referenceIterator.nextLong();
                int referenceX = ChunkPos.getX(packedReference);
                int referenceZ = ChunkPos.getZ(packedReference);

                IChunk referenceChunk = world.getChunk(referenceX, referenceZ, ChunkStatus.STRUCTURE_STARTS);
                StructureStart<?> start = referenceChunk.func_230342_a_(feature);
                if (start == null || !start.isValid()) {
                    continue;
                }

                for (StructurePiece piece : start.getComponents()) {
                    int radius = 12;
                    if (!piece.func_214810_a(chunkPos, radius)) {
                        continue;
                    }

                    if (piece instanceof AbstractVillagePiece) {
                        AbstractVillagePiece pooledPiece = (AbstractVillagePiece) piece;
                        JigsawPattern.PlacementBehaviour projection = pooledPiece.getJigsawPiece().getPlacementBehaviour();
                        if (projection == JigsawPattern.PlacementBehaviour.RIGID) {
                            pieces.add(pooledPiece);
                        }

                        for (JigsawJunction junction : pooledPiece.getJunctions()) {
                            int junctionX = junction.getSourceX();
                            int junctionZ = junction.getSourceZ();
                            if (junctionX > minX - radius && junctionZ > minZ - radius && junctionX < maxX + radius && junctionZ < maxZ + radius) {
                                junctions.add(junction);
                            }
                        }
                    } else {
                        pieces.add(piece);
                    }
                }
            }
        }
    }
}
