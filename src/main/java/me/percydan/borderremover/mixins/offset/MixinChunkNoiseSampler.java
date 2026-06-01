package me.percydan.borderremover.mixins.offset;

import me.percydan.borderremover.BorderRemover;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.math.BigInteger;

@Mixin(NoiseChunk.class)
public abstract class MixinChunkNoiseSampler {
    @Redirect(method = "forChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getPos()Lnet/minecraft/world/level/ChunkPos;"))
    private static ChunkPos applyOffset(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
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
