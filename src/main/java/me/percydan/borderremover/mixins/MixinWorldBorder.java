package me.percydan.borderremover.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder {
    @Shadow
    int absoluteMaxSize;
    @Shadow
    private WorldBorder.BorderExtent extent;

    @Unique
    private static final double borderremover$MAX_SIZE = 4294000000D;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void handleConstructor(CallbackInfo ci) {
        this.absoluteMaxSize = Integer.MAX_VALUE;
        this.extent = ((WorldBorder) (Object) this).new StaticBorderExtent(borderremover$MAX_SIZE);
    }

    @Inject(method = "isWithinBounds(Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void noborder$containsBlockPos(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isWithinBounds(Lnet/minecraft/world/level/ChunkPos;)Z", at = @At("HEAD"), cancellable = true)
    private void noborder$containsChunkPos(ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isWithinBounds(Lnet/minecraft/world/phys/AABB;)Z", at = @At("HEAD"), cancellable = true)
    private void noborder$containsBox(AABB box, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isWithinBounds(DD)Z", at = @At("HEAD"), cancellable = true)
    private void noborder$containsXZ(double x, double z, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isWithinBounds(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void noborder$containsXYZ(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "getDistanceToBorder(DD)D", at = @At("HEAD"), cancellable = true)
    private void noborder$getDistanceInsideBorderXZ(double x, double z, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(Double.POSITIVE_INFINITY);
    }

    @Inject(method = "isInsideCloseToBorder", at = @At("HEAD"), cancellable = true)
    private void noborder$canCollide(Entity entity, AABB box, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}