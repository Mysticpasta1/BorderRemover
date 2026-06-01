package me.percydan.borderremover.mixins;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SectionStorage.class)
public abstract class MixinSectionStorage<R> {
    @Final
    @Shadow
    private Long2ObjectMap<Optional<R>> storage;

    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private void tryOldKeyOnMiss(long packed, CallbackInfoReturnable<Optional<R>> cir) {
        Optional<R> result = cir.getReturnValue();
        if (result.isEmpty()) {
            long oldKey = borderRemover$toOldPacking(packed);
            if (oldKey != packed) {
                Optional<R> oldResult = storage.get(oldKey);
                if (oldResult.isPresent()) {
                    storage.put(packed, oldResult);
                    storage.remove(oldKey);
                    cir.setReturnValue(oldResult);
                }
            }
        }
    }

    @Unique
    private static long borderRemover$toOldPacking(long newPacked) {
        int x = SectionPos.x(newPacked);
        int y = SectionPos.y(newPacked);
        int z = SectionPos.z(newPacked);
        return ((long) x & ((1L << 22) - 1)) << 42
             | ((long) z & ((1L << 22) - 1)) << 20
             | ((long) y & ((1L << 20) - 1));
    }
}
