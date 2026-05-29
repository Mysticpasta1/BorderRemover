package me.percydan.borderremover.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class MixinWorld implements LevelAccessor {
    @Inject(method = "isInWorldBounds", at = @At("RETURN"), cancellable = true)
    private static void isInWorldBounds(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(true);
    }

    @Inject(method = "isOutsideSpawnableHeight", at = @At("RETURN"), cancellable = true)
    private static void isOutsideBuildHeight(int y, CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(false);
    }
}
