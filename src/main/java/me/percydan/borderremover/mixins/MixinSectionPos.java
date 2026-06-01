package me.percydan.borderremover.mixins;

import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SectionPos.class)
public abstract class MixinSectionPos {
    private static final int X_BITS = 32;
    private static final int Z_BITS = 22;
    private static final int Y_BITS = 64 - X_BITS - Z_BITS;

    private static final long X_MASK = (1L << X_BITS) - 1;
    private static final long Z_MASK = (1L << Z_BITS) - 1;
    private static final long Y_MASK = (1L << Y_BITS) - 1;

    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = Y_BITS;
    private static final int X_OFFSET = Y_BITS + Z_BITS;

    @Overwrite
    public static long asLong(int x, int y, int z) {
        long l = 0L;
        l |= ((long) x & X_MASK) << X_OFFSET;
        l |= ((long) z & Z_MASK) << Z_OFFSET;
        l |= ((long) y & Y_MASK) << Y_OFFSET;
        return l;
    }

    @Overwrite
    public long asLong() {
        return asLong(
                ((SectionPos) (Object) this).getX(),
                ((SectionPos) (Object) this).getY(),
                ((SectionPos) (Object) this).getZ()
        );
    }

    @Overwrite
    public static int x(long packed) {
        return (int) (packed >> X_OFFSET);
    }

    @Overwrite
    public static int y(long packed) {
        return (int) ((packed << (64 - Y_BITS)) >> (64 - Y_BITS));
    }

    @Overwrite
    public static int z(long packed) {
        return (int) ((packed << (64 - Z_OFFSET - Z_BITS)) >> (64 - Z_BITS));
    }

    @Overwrite
    public static long getZeroNode(int x, int z) {
        return asLong(x, 0, z);
    }

    @Overwrite
    public static long getZeroNode(long packed) {
        return packed & ~(Y_MASK << Y_OFFSET);
    }
}
