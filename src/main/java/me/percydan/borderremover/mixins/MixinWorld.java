package me.percydan.borderremover.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class MixinWorld implements LevelAccessor {
    @Inject(method = "isInWorldBoundsHorizontal", at = @At("RETURN"), cancellable = true)
    private static void isInWorldBounds(BlockPos pPos, CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(pPos.getX() >= -2000000000 && pPos.getZ() >= -2000000000 && pPos.getX() < 2000000000 && pPos.getZ() < 2000000000);
    }

    @Inject(method = "isOutsideSpawnableHeight", at = @At("RETURN"), cancellable = true)
    private static void isOutsideBuildHeight(int y, CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(false);
    }

    @Override
    public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ) {
        int i;
        if (pX >= -2000000000 && pZ >= -2000000000 && pX < 2000000000 && pZ < 2000000000) {
            if (this.hasChunk(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ))) {
                i = this.getChunk(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ)).getHeight(pHeightmapType, pX & 15, pZ & 15) + 1;
            } else {
                i = this.getMinBuildHeight();
            }
        } else {
            i = this.getSeaLevel() + 1;
        }

        return i;
    }
}
