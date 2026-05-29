package me.percydan.borderremover.mixins.offset;

import me.percydan.borderremover.BorderRemover;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkAccess.class)
public abstract class MixinChunk {
    @ModifyVariable(method = "fillBiomesFromNoise", at = @At("STORE"))
    private ChunkPos applyOffset(ChunkPos chunkPos) {
        int offset = BorderRemover.config.genOffset.get();
        return new ChunkPos(chunkPos.x + offset, chunkPos.z + offset);
    }
}
