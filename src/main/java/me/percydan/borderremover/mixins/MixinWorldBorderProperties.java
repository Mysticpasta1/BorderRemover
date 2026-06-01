package me.percydan.borderremover.mixins;

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.Settings.class)
public abstract class MixinWorldBorderProperties {
    @Mutable
    @Shadow
    @Final
    private double size;

    @Unique
    private static final double borderremover$MAX_SIZE = 4294000000D;

    @Inject(method = "<init>(DDDDIIDJD)V", at = @At("RETURN"))
    private void handleConstructor(double centerX, double centerZ, double damagePerBlock, double buffer, int warningBlocks, int warningTime, double size, long targetRemainingTime, double targetSize, CallbackInfo ci) {
        this.size = borderremover$MAX_SIZE;
    }
}