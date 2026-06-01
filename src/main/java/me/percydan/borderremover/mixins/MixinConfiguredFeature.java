package me.percydan.borderremover.mixins;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConfiguredFeature.class)
public abstract class MixinConfiguredFeature {
    @Unique
    private static final Logger borderremover$LOGGER = LogUtils.getLogger();

    @Inject(method = "place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void borderremover$safePlace(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        if (Math.abs(cx) > 134_187_500 || Math.abs(cz) > 134_187_500) {
            borderremover$LOGGER.debug("Feature cancelled at extreme chunk ({}, {}), origin={}", cx, cz, pos);
            cir.setReturnValue(false);
        }
    }
}
