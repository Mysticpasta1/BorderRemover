package me.percydan.borderremover.mixins;

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder {
    @Shadow
    int absoluteMaxSize;
    @Shadow
    private WorldBorder.BorderExtent extent;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void handleConstructor(CallbackInfo ci) {
        this.absoluteMaxSize = Integer.MAX_VALUE;
        this.extent = ((WorldBorder) (Object) this).new StaticBorderExtent(4294967294D);
    }
}
