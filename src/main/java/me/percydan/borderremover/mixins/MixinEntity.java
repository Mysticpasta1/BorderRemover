package me.percydan.borderremover.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Redirect(method = "updatePosition", at = @At(target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D", value = "INVOKE"))
    private double redirectClamp(double value, double min, double max) {
        return value;
    }

    @Redirect(method = "readNbt", at = @At(target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D", value = "INVOKE"))
    private double redirectClampReadNbt(double value, double min, double max) {
        return value;
    }
}
