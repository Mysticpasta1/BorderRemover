package me.percydan.borderremover.mixins;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class MixinWorldGenRegion {
    @Shadow
    @Final
    private ChunkPos firstPos;
    @Shadow
    @Final
    private ChunkPos lastPos;

    @Shadow
    public abstract ChunkAccess getChunk(int i, int j);

    @Shadow
    public abstract ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl);

    @Shadow
    public abstract boolean hasChunk(int i, int j);

    @Shadow
    public abstract int getMinBuildHeight();

    @Unique
    private static final Logger borderremover$LOGGER = LogUtils.getLogger();

    @Unique
    private void borderremover$logGetChunk4(int x, int z) {
        if (x < firstPos.x || x > lastPos.x || z < firstPos.z || z > lastPos.z) {
            borderremover$LOGGER.debug("getChunk(4-param) called with out-of-bounds coords: ({}, {}), bounds: first=({},{}), last=({},{})",
                    x, z, firstPos.x, firstPos.z, lastPos.x, lastPos.z);
        }
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("HEAD"))
    private void borderremover$watchGetChunk4(int x, int z, ChunkStatus status, boolean required, CallbackInfoReturnable<ChunkAccess> cir) {
        borderremover$logGetChunk4(x, z);
    }

    @Unique
    private int borderremover$correct(int coord, int first, int last) {
        if (first > 0 && coord < 0) {
            int shifted = coord + (1 << 28);
            if (shifted >= first && shifted <= last) {
                return shifted;
            }
        }
        if (coord < first) {
            return first;
        }
        if (coord > last) {
            return last;
        }
        return coord;
    }

    @Unique
    private BlockPos borderremover$correctPos(BlockPos pos) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        int cx2 = borderremover$correct(cx, firstPos.x, lastPos.x);
        int cz2 = borderremover$correct(cz, firstPos.z, lastPos.z);
        if (cx2 == cx && cz2 == cz) {
            return pos;
        }
        return new BlockPos(cx2 << 4 | (pos.getX() & 15), pos.getY(), cz2 << 4 | (pos.getZ() & 15));
    }

    @ModifyVariable(method = "getChunk(II)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int borderremover$fixGetChunk2X(int x) {
        return borderremover$correct(x, firstPos.x, lastPos.x);
    }

    @ModifyVariable(method = "getChunk(II)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int borderremover$fixGetChunk2Z(int z) {
        return borderremover$correct(z, firstPos.z, lastPos.z);
    }

    @ModifyVariable(method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int borderremover$fixGetChunk4X(int x) {
        return borderremover$correct(x, firstPos.x, lastPos.x);
    }

    @ModifyVariable(method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int borderremover$fixGetChunk4Z(int z) {
        return borderremover$correct(z, firstPos.z, lastPos.z);
    }

    @ModifyVariable(method = "hasChunk(II)Z",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int borderremover$fixHasChunkX(int x) {
        return borderremover$correct(x, firstPos.x, lastPos.x);
    }

    @ModifyVariable(method = "hasChunk(II)Z",
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int borderremover$fixHasChunkZ(int z) {
        return borderremover$correct(z, firstPos.z, lastPos.z);
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    private void borderremover$guardSetBlock(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> cir) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        if (cx < firstPos.x || cx > lastPos.x || cz < firstPos.z || cz > lastPos.z) {
            borderremover$LOGGER.debug("setBlock cancelled at far chunk ({}, {}), pos={}", cx, cz, pos);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I",
            at = @At("HEAD"), cancellable = true)
    private void borderremover$fixGetHeight(Heightmap.Types type, int x, int z, CallbackInfoReturnable<Integer> cir) {
        int cx = SectionPos.blockToSectionCoord(x);
        int cz = SectionPos.blockToSectionCoord(z);
        if (cx < firstPos.x || cx > lastPos.x || cz < firstPos.z || cz > lastPos.z) {
            cir.setReturnValue(getMinBuildHeight());
        }
    }
}