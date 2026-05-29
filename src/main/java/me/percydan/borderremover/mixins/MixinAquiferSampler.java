package me.percydan.borderremover.mixins;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Aquifer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Aquifer.NoiseBasedAquifer.class)
public abstract class MixinAquiferSampler {
    @Shadow
    @Final
    protected int minGridX;

    @Shadow
    @Final
    protected int minGridY;

    @Shadow
    @Final
    protected int minGridZ;

    @Shadow
    @Final
    protected int gridSizeX;

    @Shadow
    @Final
    protected int gridSizeZ;

    @Shadow
    @Final
    protected Aquifer.FluidStatus[] aquiferCache;

    /**
     * @author Mysticpasta1
     * @reason idk lol
     */
    @Overwrite
    protected int getIndex(int x, int y, int z) {
        int i = x - minGridX;
        int j = y - minGridY;
        int k = z - minGridZ;
        int dx = (j * gridSizeZ + k) * gridSizeX + i;
        return Mth.clamp(dx, 0, aquiferCache.length - 1);
    }
}
