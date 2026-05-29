package me.percydan.borderremover.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LevelReader.class)
public interface LevelReaderMixin {
    /**
     * @author Mysticpasta1
     * @reason seeing if this helps dark patch in super far chunks
     */
    @Overwrite
    default int getMaxLocalRawBrightness(BlockPos pPos, int pAmount) {
        return ((LevelReader) (Object) this).getRawBrightness(pPos, pAmount);
    }
}
