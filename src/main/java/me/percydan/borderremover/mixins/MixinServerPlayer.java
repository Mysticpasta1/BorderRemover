package me.percydan.borderremover.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {
    @Unique
    private static final double BORDER_CLAMP_MIN = -2147000000;
    @Unique
    private static final double BORDER_CLAMP_MAX = 2147000000;

    @ModifyVariable(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double borderremover$clampTeleportX(double x) {
        return Mth.clamp(x, BORDER_CLAMP_MIN, BORDER_CLAMP_MAX);
    }

    @ModifyVariable(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z",
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private double borderremover$clampTeleportY(double y) {
        return Mth.clamp(y, BORDER_CLAMP_MIN, BORDER_CLAMP_MAX);
    }

    @ModifyVariable(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z",
            at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private double borderremover$clampTeleportZ(double z) {
        return Mth.clamp(z, BORDER_CLAMP_MIN, BORDER_CLAMP_MAX);
    }
}
