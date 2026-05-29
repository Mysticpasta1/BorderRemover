package me.percydan.borderremover.mixins.offset;

import me.percydan.borderremover.BorderRemover;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoiseChunk.class)
public abstract class MixinChunkNoiseSampler {
    @Redirect(method = "forChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getPos()Lnet/minecraft/world/level/ChunkPos;"))
    private static ChunkPos applyOffset(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int offset = BorderRemover.config.genOffset.get();
        return new ChunkPos(chunkPos.x + offset, chunkPos.z + offset);
    }
}
