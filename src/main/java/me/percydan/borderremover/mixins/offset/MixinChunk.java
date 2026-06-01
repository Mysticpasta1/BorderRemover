package me.percydan.borderremover.mixins.offset;

import me.percydan.borderremover.BorderRemover;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.math.BigInteger;

@Mixin(ChunkAccess.class)
public abstract class MixinChunk {
    @ModifyVariable(method = "fillBiomesFromNoise", at = @At("STORE"))
    private ChunkPos applyOffset(ChunkPos chunkPos) {
        BigInteger offset = new BigInteger(BorderRemover.config.genOffset.get());
        if (offset.bitLength() <= 63) {
            long off = offset.longValue();
            return new ChunkPos(
                    (int) (chunkPos.x + off),
                    (int) (chunkPos.z + off)
            );
        }
        return new ChunkPos(
                BigInteger.valueOf(chunkPos.x).add(offset).intValue(),
                BigInteger.valueOf(chunkPos.z).add(offset).intValue()
        );
    }
}
