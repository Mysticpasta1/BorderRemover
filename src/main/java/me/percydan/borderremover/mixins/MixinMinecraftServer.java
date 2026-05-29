package me.percydan.borderremover.mixins;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Inject(method = "getAbsoluteMaxWorldSize", at = @At("HEAD"), cancellable = true)
    public void getMaxWorldBorderRadius(CallbackInfoReturnable<Integer> ci) {
        ci.setReturnValue(2000000000);
        ci.cancel();
    }
}
