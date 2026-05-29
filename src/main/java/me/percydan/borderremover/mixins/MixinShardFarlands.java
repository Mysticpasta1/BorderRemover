package me.percydan.borderremover.mixins;

import me.percydan.borderremover.BorderRemover;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ImprovedNoise.class)
public abstract class MixinShardFarlands {
    @Shadow
    @Final
    public double xo;
    @Shadow
    @Final
    public double yo;
    @Shadow
    @Final
    public double zo;

    @Inject(method = "noise(DDDDD)D", at = @At("RETURN"), cancellable = true)
    public void sample(double x, double y, double z, double yScale, double yMax, CallbackInfoReturnable<Double> cir) {
        if (BorderRemover.config.shardFarlands.get()) {
            double d = x + xo;
            double e = y + yo;
            double f = z + zo;
            int i = Mth.floor(d);
            int j = Mth.floor(e);
            int k = Mth.floor(f);
            double g = d - (float) i;
            double h = e - (float) j;
            double l = f - (float) k;
            double n;
            if (yScale != 0.0) {
                double m;
                if (yMax >= 0.0 && yMax < h) {
                    m = yMax;
                } else {
                    m = h;
                }

                n = Mth.floor(m / yScale + 1.0000000116860974E-7) * yScale;
            } else {
                n = 0.0;
            }

            cir.setReturnValue(sampleAndLerp(i, j, k, g, h - n, l, h));
        }
    }

    @Inject(method = "noiseWithDerivative", at = @At("RETURN"), cancellable = true)
    public void sampleDerivative(double x, double y, double z, double[] ds, CallbackInfoReturnable<Double> cir) {
        if (BorderRemover.config.shardFarlands.get()) {
            double d = x + xo;
            double e = y + yo;
            double f = z + zo;
            int i = Mth.floor(d);
            int j = Mth.floor(e);
            int k = Mth.floor(f);
            double g, h, l;
            g = d - (float) i;
            h = e - (float) j;
            l = f - (float) k;

            cir.setReturnValue(sampleWithDerivative(i, j, k, g, h, l, ds));
        }
    }

    @Shadow
    private double sampleWithDerivative(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double[] ds) {
        throw new AssertionError();
    }

    @Shadow
    private double sampleAndLerp(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalY) {
        throw new AssertionError();
    }
}
