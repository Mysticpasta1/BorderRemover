package me.percydan.borderremover.mixins;

import com.mojang.serialization.Codec;
import me.percydan.borderremover.BorderRemover;
import me.percydan.borderremover.config.Config;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlendedNoise.class)
public abstract class MixinInterpolatedNoiseSampler {
    @Mutable
    @Shadow
    @Final
    private static Codec<Double> SCALE_RANGE;

    @Mutable
    @Shadow
    @Final
    private double xzScale;

    @Mutable
    @Shadow
    @Final
    private double yScale;

    @ModifyConstant(
            constant = @Constant(
                    doubleValue = 684.412D,
                    ordinal = 0
            ),
            method = "<init>(Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;DDDDD)V"
    )
    private double setXZCoordinateScale(double original) {
        return BorderRemover.config == null ? original : BorderRemover.config.xzCoordinateScale.get();
    }

    @ModifyConstant(
            constant = @Constant(
                    doubleValue = 684.412D,
                    ordinal = 1
            ),
            method = "<init>(Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;DDDDD)V"
    )
    private double setYCoordinateScale(double original) {
        return BorderRemover.config == null ? original : BorderRemover.config.yCoordinateScale.get();
    }

    @Inject(at = @At(value = "RETURN"), method = "<init>(Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;DDDDD)V")
    private void setScaleAndFactorRange(PerlinNoise lowerInterpolatedNoise, PerlinNoise upperInterpolatedNoise, PerlinNoise interpolationNoise, double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier, CallbackInfo ci) {
        SCALE_RANGE = Codec.doubleRange(-Double.MAX_VALUE, Double.MAX_VALUE);
        Config options = BorderRemover.config;
        if (options == null)
            return;

        String xzScaleMultiplier = options.xzScaleMultiplier.get();
        xzScaleMultiplier = xzScaleMultiplier.replace(",", "");
        options.xzScaleMultiplier.set(xzScaleMultiplier);
        double multiplier;

        try {
            multiplier = Double.parseDouble(xzScaleMultiplier);
            this.xzScale = multiplier;
        } catch (NumberFormatException e) {
        }

        String yScaleMultiplier = options.yScaleMultiplier.get();
        yScaleMultiplier = yScaleMultiplier.replace(",", "");
        options.yScaleMultiplier.set(yScaleMultiplier);

        try {
            multiplier = Double.parseDouble(yScaleMultiplier);
            this.yScale = multiplier;
        } catch (NumberFormatException e) {
        }
    }
}
