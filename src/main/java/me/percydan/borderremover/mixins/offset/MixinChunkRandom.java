package me.percydan.borderremover.mixins.offset;

import me.percydan.borderremover.BorderRemover;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.math.BigInteger;

@Mixin(WorldgenRandom.class)
public abstract class MixinChunkRandom {
    @ModifyVariable(method = "setLargeFeatureSeed", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int offsetChunkX(int chunkX) {
        BigInteger offset = new BigInteger(BorderRemover.config.genOffset.get());
        if (offset.bitLength() <= 63) {
            return chunkX + (int) offset.longValue();
        }
        return chunkX + offset.intValue();
    }

    @ModifyVariable(method = "setLargeFeatureSeed", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int offsetChunkZ(int chunkZ) {
        BigInteger offset = new BigInteger(BorderRemover.config.genOffset.get());
        if (offset.bitLength() <= 63) {
            return chunkZ + (int) offset.longValue();
        }
        return chunkZ + offset.intValue();
    }

    @ModifyVariable(method = "setDecorationSeed", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int offsetBlockX(int blockX) {
        BigInteger offset = new BigInteger(BorderRemover.config.genOffset.get());
        if (offset.bitLength() <= 63) {
            return blockX + (int) (offset.longValue() << 4);
        }
        return blockX + offset.shiftLeft(4).intValue();
    }

    @ModifyVariable(method = "setDecorationSeed", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int offsetBlockZ(int blockZ) {
        BigInteger offset = new BigInteger(BorderRemover.config.genOffset.get());
        if (offset.bitLength() <= 63) {
            return blockZ + (int) (offset.longValue() << 4);
        }
        return blockZ + offset.shiftLeft(4).intValue();
    }
}
