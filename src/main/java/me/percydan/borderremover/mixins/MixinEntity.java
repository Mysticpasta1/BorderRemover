package me.percydan.borderremover.mixins;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Unique
    private static final double BORDER_CLAMP_MIN = -2_147_000_000;
    @Unique
    private static final double BORDER_CLAMP_MAX = 2_147_000_000;

    @Redirect(method = "absMoveTo(DDD)V", at = @At(target = "Lnet/minecraft/util/Mth;clamp(DDD)D", value = "INVOKE"))
    private double redirectClamp(double value, double min, double max) {
        return Mth.clamp(value, BORDER_CLAMP_MIN, BORDER_CLAMP_MAX);
    }

    @Redirect(method = "load", at = @At(target = "Lnet/minecraft/util/Mth;clamp(DDD)D", value = "INVOKE"))
    private double redirectClampLoad(double value, double min, double max) {
        return Mth.clamp(value, BORDER_CLAMP_MIN, BORDER_CLAMP_MAX);
    }
}