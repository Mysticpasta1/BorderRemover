package me.percydan.borderremover.mixins;

import me.percydan.borderremover.BorderRemover;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PerlinNoise.class)
public abstract class MixinOctavePerlinNoiseSampler {
    @Inject(method = "wrap", at = @At("HEAD"), cancellable = true)
    private static void maintainPrecision(double value, CallbackInfoReturnable<Double> cir) {
        if (!BorderRemover.config.enableFarlands.get())
            return;

        cir.setReturnValue(value);
        cir.cancel();
    }
}
