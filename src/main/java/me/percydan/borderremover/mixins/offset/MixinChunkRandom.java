package me.percydan.borderremover.mixins.offset;

import me.percydan.borderremover.BorderRemover;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldgenRandom.class)
public abstract class MixinChunkRandom {
    @Inject(method = "setLargeFeatureSeed", at = @At("HEAD"))
    private void applyOffset(long worldSeed, int chunkX, int chunkZ, CallbackInfo ci) {
        int offset = BorderRemover.config.genOffset.get();
        chunkX += offset;
        chunkZ += offset;
    }

    @Inject(method = "setDecorationSeed", at = @At("HEAD"))
    private void applyOffset(long worldSeed, int blockX, int blockZ, CallbackInfoReturnable<Long> cir) {
        int offset = BorderRemover.config.genOffset.get() << 4;
        blockX += offset;
        blockZ += offset;
    }
}
