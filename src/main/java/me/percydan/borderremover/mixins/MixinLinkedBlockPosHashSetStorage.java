package me.percydan.borderremover.mixins;

import net.minecraft.world.level.lighting.SpatialLongSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpatialLongSet.InternalMap.class)
public abstract class MixinLinkedBlockPosHashSetStorage {
    @Shadow
    private static int X_BITS;
    @Shadow
    private static int Z_BITS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void handleConstructor(CallbackInfo ci) {
        X_BITS = 28;
        Z_BITS = 28;
    }
}
