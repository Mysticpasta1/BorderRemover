package me.percydan.borderremover.mixins;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.percydan.borderremover.IStorageExtreme;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.SkyLightEngine;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightEngine.class)
public abstract class LightEngineMixin {
    @Final
    @Shadow
    private BlockPos.MutableBlockPos mutablePos;

    @Unique
    private final LongArrayFIFOQueue borderRemover$increaseExtremeQueue = new LongArrayFIFOQueue();

    @Unique
    private final LongArrayFIFOQueue borderRemover$decreaseExtremeQueue = new LongArrayFIFOQueue();

    @Unique
    private final Long2ObjectOpenHashMap<IntArrayList> borderRemover$extremeBlockNodes = new Long2ObjectOpenHashMap<>();

    @Unique
    private static final long PULL_LIGHT_IN_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(1);

    @Unique
    private static final Direction[] PROPAGATION_DIRECTIONS = Direction.values();

    @Unique
    private static boolean borderremover$isEmptyShape(BlockState state) {
        return !state.canOcclude() || !state.useShapeForLightOcclusion();
    }

    @Unique
    private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);

    @Unique
    private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);

    @Unique
    private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);

    @Unique
    private LayerLightSectionStorage borderremover$storage() {
        return ((LightEngineAccessor)(Object)this).getStorage();
    }

    @Unique
    private LightChunkGetter borderremover$chunkSource() {
        return ((LightEngineAccessor)(Object)this).getChunkSource();
    }

    @Unique
    private static final int borderremover$BLOCKPOS_MAX = 33554431;

    @Unique
    private static final int borderremover$BLOCKPOS_MIN = -33554432;

    @Unique
    private static boolean borderremover$isExtremeBlock(int x, int z) {
        return x < borderremover$BLOCKPOS_MIN || x > borderremover$BLOCKPOS_MAX || z < borderremover$BLOCKPOS_MIN || z > borderremover$BLOCKPOS_MAX;
    }

    @Unique
    private static int borderremover$encodeOffset(int localX, int localY, int localZ) {
        return (localY & 15) << 8 | (localZ & 15) << 4 | (localX & 15);
    }

    @Unique
    private static int borderremover$decodeX(int offset) { return offset & 15; }

    @Unique
    private static int borderremover$decodeY(int offset) { return (offset >> 8) & 15; }

    @Unique
    private static int borderremover$decodeZ(int offset) { return (offset >> 4) & 15; }

    @Unique
    private static long borderremover$packQueueEntry(long queueEntry, int localOffset) {
        return queueEntry | ((long) localOffset << 12);
    }

    @Unique
    private static long borderremover$unpackQueueEntry(long combined) {
        return combined & 0xFFF;
    }

    @Unique
    private static int borderremover$unpackOffset(long combined) {
        return (int) ((combined >> 12) & 0xFFF);
    }

    @Inject(method = "checkBlock(Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"), cancellable = true)
    private void borderremover$onCheckBlock(BlockPos pPos, CallbackInfo ci) {
        if (!borderremover$isExtremeBlock(pPos.getX(), pPos.getZ())) return;
        ci.cancel();
        long sectionPos = SectionPos.asLong(pPos.getX() >> 4, pPos.getY() >> 4, pPos.getZ() >> 4);
        int offset = borderremover$encodeOffset(pPos.getX() & 15, pPos.getY() & 15, pPos.getZ() & 15);
        this.borderRemover$extremeBlockNodes.computeIfAbsent(sectionPos, k -> new IntArrayList()).add(offset);
    }

    @Inject(method = "runLightUpdates()I", at = @At("RETURN"), cancellable = true)
    private void borderremover$afterRunLightUpdates(CallbackInfoReturnable<Integer> cir) {
        int extra = 0;
        if (!this.borderRemover$extremeBlockNodes.isEmpty()) {
            extra += this.borderremover$processExtremeNodes();
            this.borderRemover$extremeBlockNodes.clear();
        }
        if (!this.borderRemover$decreaseExtremeQueue.isEmpty()) {
            extra += this.borderremover$processExtremeDecreases();
        }
        if (!this.borderRemover$increaseExtremeQueue.isEmpty()) {
            extra += this.borderremover$processExtremeIncreases();
        }
        if (extra > 0) {
            cir.setReturnValue(cir.getReturnValue() + extra);
        }
    }

    @Unique
    private int borderremover$processExtremeNodes() {
        int count = 0;
        ObjectIterator<Long2ObjectMap.Entry<IntArrayList>> it = this.borderRemover$extremeBlockNodes.long2ObjectEntrySet().fastIterator();
        while (it.hasNext()) {
            Long2ObjectMap.Entry<IntArrayList> e = it.next();
            long sectionPos = e.getLongKey();
            for (int offset : e.getValue()) {
                this.borderremover$checkExtremeNode(sectionPos,
                        borderremover$decodeX(offset),
                        borderremover$decodeY(offset),
                        borderremover$decodeZ(offset));
                count++;
            }
        }
        return count;
    }

    @Unique
    private void borderremover$checkExtremeNode(long sectionPos, int localX, int localY, int localZ) {
        if ((Object)this instanceof BlockLightEngine) {
            this.borderremover$checkExtremeBlockNode(sectionPos, localX, localY, localZ);
        } else if ((Object)this instanceof SkyLightEngine) {
            this.borderremover$checkExtremeSkyNode(sectionPos, localX, localY, localZ);
        }
    }

    @Unique
    private void borderremover$checkExtremeBlockNode(long sectionPos, int localX, int localY, int localZ) {
        BlockLightSectionStorage storage = (BlockLightSectionStorage) this.borderremover$storage();
        LightChunkGetter chunkSource = this.borderremover$chunkSource();
        if (storage.storingLightForSection(sectionPos)) {
            int worldX = SectionPos.sectionToBlockCoord(SectionPos.x(sectionPos)) + localX;
            int worldY = SectionPos.sectionToBlockCoord(SectionPos.y(sectionPos)) + localY;
            int worldZ = SectionPos.sectionToBlockCoord(SectionPos.z(sectionPos)) + localZ;
            this.mutablePos.set(worldX, worldY, worldZ);
            BlockState blockstate = this.borderremover$getState(this.mutablePos);
            int opacity = this.borderremover$getOpacity(blockstate, this.mutablePos);
            int emission = blockstate.getLightEmission(chunkSource.getLevel(), this.mutablePos);
            if (emission > 0 && !storage.lightOnInSection(sectionPos)) emission = 0;
            int offset = borderremover$encodeOffset(localX, localY, localZ);
            int currentLevel = this.borderremover$getStoredLevelExtreme(sectionPos, offset);
            if (currentLevel > 0 || opacity > 0) {
                this.borderremover$setStoredLevelExtreme(sectionPos, offset, 0);
                this.borderremover$enqueueDecreaseExtreme(sectionPos, offset, LightEngine.QueueEntry.decreaseAllDirections(Math.max(currentLevel, opacity)));
            } else {
                this.borderremover$enqueueDecreaseExtreme(sectionPos, offset, PULL_LIGHT_IN_ENTRY);
            }
            if (emission > 0) {
                this.borderremover$enqueueIncreaseExtreme(sectionPos, offset, LightEngine.QueueEntry.increaseLightFromEmission(emission, borderremover$isEmptyShape(blockstate)));
            }
        }
    }

    @Unique
    private void borderremover$checkExtremeSkyNode(long sectionPos, int localX, int localY, int localZ) {
        SkyLightSectionStorage storage = (SkyLightSectionStorage) this.borderremover$storage();
        int worldX = SectionPos.sectionToBlockCoord(SectionPos.x(sectionPos)) + localX;
        int worldY = SectionPos.sectionToBlockCoord(SectionPos.y(sectionPos)) + localY;
        int worldZ = SectionPos.sectionToBlockCoord(SectionPos.z(sectionPos)) + localZ;
        this.mutablePos.set(worldX, worldY, worldZ);
        SkyLightEngineAccessor sky = (SkyLightEngineAccessor) this;
        int i1 = storage.lightOnInSection(sectionPos) ? sky.invokeGetLowestSourceY(worldX, worldZ, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (i1 != Integer.MAX_VALUE) {
            sky.invokeUpdateSourcesInColumn(worldX, worldZ, i1);
        }
        if (storage.storingLightForSection(sectionPos)) {
            int offset = borderremover$encodeOffset(localX, localY, localZ);
            boolean flag = worldY >= i1;
            if (flag) {
                this.borderremover$enqueueDecreaseExtreme(sectionPos, offset, REMOVE_TOP_SKY_SOURCE_ENTRY);
                this.borderremover$enqueueIncreaseExtreme(sectionPos, offset, ADD_SKY_SOURCE_ENTRY);
            } else if (i1 == Integer.MAX_VALUE) {
                int currentLevel = this.borderremover$getStoredLevelExtreme(sectionPos, offset);
                if (currentLevel > 0) {
                    this.borderremover$setStoredLevelExtreme(sectionPos, offset, 0);
                    this.borderremover$enqueueDecreaseExtreme(sectionPos, offset, LightEngine.QueueEntry.decreaseAllDirections(currentLevel));
                }
            }
        }
    }

    @Unique
    private int borderremover$processExtremeDecreases() {
        int count = 0;
        while (!this.borderRemover$decreaseExtremeQueue.isEmpty()) {
            long sectionPos = this.borderRemover$decreaseExtremeQueue.dequeueLong();
            long combined = this.borderRemover$decreaseExtremeQueue.dequeueLong();
            int offset = borderremover$unpackOffset(combined);
            long entry = borderremover$unpackQueueEntry(combined);
            this.borderremover$propagateDecreaseExtreme(sectionPos, offset, entry);
            count++;
        }
        return count;
    }

    @Unique
    private int borderremover$processExtremeIncreases() {
        int count = 0;
        while (!this.borderRemover$increaseExtremeQueue.isEmpty()) {
            long sectionPos = this.borderRemover$increaseExtremeQueue.dequeueLong();
            long combined = this.borderRemover$increaseExtremeQueue.dequeueLong();
            int offset = borderremover$unpackOffset(combined);
            long entry = borderremover$unpackQueueEntry(combined);
            int currentLevel = this.borderremover$getStoredLevelExtreme(sectionPos, offset);
            int targetLevel = LightEngine.QueueEntry.getFromLevel(entry);
            if (LightEngine.QueueEntry.isIncreaseFromEmission(entry) && currentLevel < targetLevel) {
                this.borderremover$setStoredLevelExtreme(sectionPos, offset, targetLevel);
                currentLevel = targetLevel;
            }
            if (currentLevel == targetLevel) {
                this.borderremover$propagateIncreaseExtreme(sectionPos, offset, entry, currentLevel);
            }
            count++;
        }
        return count;
    }

    @Unique
    private void borderremover$propagateIncreaseExtreme(long sectionPos, int localOffset, long queueEntry, int lightLevel) {
        LayerLightSectionStorage storage = this.borderremover$storage();

        int localX = borderremover$decodeX(localOffset);
        int localY = borderremover$decodeY(localOffset);
        int localZ = borderremover$decodeZ(localOffset);
        int sectionX = SectionPos.x(sectionPos);
        int sectionY = SectionPos.y(sectionPos);
        int sectionZ = SectionPos.z(sectionPos);
        int worldX = SectionPos.sectionToBlockCoord(sectionX) + localX;
        int worldY = SectionPos.sectionToBlockCoord(sectionY) + localY;
        int worldZ = SectionPos.sectionToBlockCoord(sectionZ) + localZ;

        int emptyCount = localY == 0 && storage instanceof SkyLightSectionStorage
                ? this.borderremover$countEmptySectionsBelow(sectionPos, worldY)
                : 0;

        BlockState centerState = null;

        for (Direction direction : PROPAGATION_DIRECTIONS) {
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(queueEntry, direction)) continue;

            int ddx = direction.getStepX();
            int ddy = direction.getStepY();
            int ddz = direction.getStepZ();

            int newLocalX = localX + ddx;
            int newLocalY = localY + ddy;
            int newLocalZ = localZ + ddz;
            int adjX = 0, adjY = 0, adjZ = 0;

            if (newLocalX < 0) { newLocalX += 16; adjX = -1; }
            else if (newLocalX >= 16) { newLocalX -= 16; adjX = 1; }
            if (newLocalY < 0) { newLocalY += 16; adjY = -1; }
            else if (newLocalY >= 16) { newLocalY -= 16; adjY = 1; }
            if (newLocalZ < 0) { newLocalZ += 16; adjZ = -1; }
            else if (newLocalZ >= 16) { newLocalZ -= 16; adjZ = 1; }

            long neighborSection = SectionPos.offset(sectionPos, adjX, adjY, adjZ);
            int neighborOffset = borderremover$encodeOffset(newLocalX, newLocalY, newLocalZ);

            if (!storage.storingLightForSection(neighborSection)) continue;

            int neighborLevel = this.borderremover$getStoredLevelExtreme(neighborSection, neighborOffset);
            int propagatedLevel = lightLevel - 1;

            if (propagatedLevel > neighborLevel) {
                int nWorldX = SectionPos.sectionToBlockCoord(SectionPos.x(neighborSection)) + newLocalX;
                int nWorldY = SectionPos.sectionToBlockCoord(SectionPos.y(neighborSection)) + newLocalY;
                int nWorldZ = SectionPos.sectionToBlockCoord(SectionPos.z(neighborSection)) + newLocalZ;

                this.mutablePos.set(nWorldX, nWorldY, nWorldZ);
                BlockState neighborState = this.borderremover$getState(this.mutablePos);
                int opacity = lightLevel - this.borderremover$getOpacity(neighborState, this.mutablePos);

                if (opacity > neighborLevel) {
                    if (centerState == null) {
                        if (LightEngine.QueueEntry.isFromEmptyShape(queueEntry)) {
                            centerState = Blocks.AIR.defaultBlockState();
                        } else {
                            this.mutablePos.set(worldX, worldY, worldZ);
                            centerState = this.borderremover$getState(this.mutablePos);
                        }
                    }

                    if (!this.borderremover$shapeOccludesExtreme(worldX, worldY, worldZ, centerState, nWorldX, nWorldY, nWorldZ, neighborState, direction)) {
                        this.borderremover$setStoredLevelExtreme(neighborSection, neighborOffset, opacity);
                        if (opacity > 1) {
                            this.borderremover$enqueueIncreaseExtreme(neighborSection, neighborOffset,
                                    LightEngine.QueueEntry.increaseSkipOneDirection(opacity, borderremover$isEmptyShape(neighborState), direction.getOpposite()));
                        }
                        if (storage instanceof SkyLightSectionStorage) {
                            this.borderremover$propagateFromEmptySectionsExtreme(nWorldX, nWorldZ, direction, opacity, true, emptyCount);
                        }
                    }
                }
            }
        }
    }

    @Unique
    private void borderremover$propagateDecreaseExtreme(long sectionPos, int localOffset, long queueEntry) {
        LayerLightSectionStorage storage = this.borderremover$storage();

        int lightLevel = LightEngine.QueueEntry.getFromLevel(queueEntry);
        int localX = borderremover$decodeX(localOffset);
        int localY = borderremover$decodeY(localOffset);
        int localZ = borderremover$decodeZ(localOffset);
        int sectionY = SectionPos.y(sectionPos);
        int worldY = SectionPos.sectionToBlockCoord(sectionY) + localY;

        int emptyCount = localY == 0 && storage instanceof SkyLightSectionStorage
                ? this.borderremover$countEmptySectionsBelow(sectionPos, worldY)
                : 0;

        for (Direction direction : PROPAGATION_DIRECTIONS) {
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(queueEntry, direction)) continue;

            int ddx = direction.getStepX();
            int ddy = direction.getStepY();
            int ddz = direction.getStepZ();

            int newLocalX = localX + ddx;
            int newLocalY = localY + ddy;
            int newLocalZ = localZ + ddz;
            int adjX = 0, adjY = 0, adjZ = 0;

            if (newLocalX < 0) { newLocalX += 16; adjX = -1; }
            else if (newLocalX >= 16) { newLocalX -= 16; adjX = 1; }
            if (newLocalY < 0) { newLocalY += 16; adjY = -1; }
            else if (newLocalY >= 16) { newLocalY -= 16; adjY = 1; }
            if (newLocalZ < 0) { newLocalZ += 16; adjZ = -1; }
            else if (newLocalZ >= 16) { newLocalZ -= 16; adjZ = 1; }

            long neighborSection = SectionPos.offset(sectionPos, adjX, adjY, adjZ);
            int neighborOffset = borderremover$encodeOffset(newLocalX, newLocalY, newLocalZ);

            if (!storage.storingLightForSection(neighborSection)) continue;

            int neighborLevel = this.borderremover$getStoredLevelExtreme(neighborSection, neighborOffset);
            if (neighborLevel != 0) {
                if (neighborLevel <= lightLevel - 1) {
                    int nWorldX = SectionPos.sectionToBlockCoord(SectionPos.x(neighborSection)) + newLocalX;
                    int nWorldY = SectionPos.sectionToBlockCoord(SectionPos.y(neighborSection)) + newLocalY;
                    int nWorldZ = SectionPos.sectionToBlockCoord(SectionPos.z(neighborSection)) + newLocalZ;

                    this.mutablePos.set(nWorldX, nWorldY, nWorldZ);
                    this.borderremover$setStoredLevelExtreme(neighborSection, neighborOffset, 0);

                    if (storage instanceof BlockLightSectionStorage) {
                        LightChunkGetter chunkSource = this.borderremover$chunkSource();
                        BlockState neighborState = this.borderremover$getState(this.mutablePos);
                        int emission = neighborState.getLightEmission(chunkSource.getLevel(), this.mutablePos);
                        if (emission > 0 && !storage.lightOnInSection(neighborSection)) emission = 0;
                        if (emission < neighborLevel) {
                            this.borderremover$enqueueDecreaseExtreme(neighborSection, neighborOffset,
                                    LightEngine.QueueEntry.decreaseSkipOneDirection(neighborLevel, direction.getOpposite()));
                        }
                        if (emission > 0) {
                            this.borderremover$enqueueIncreaseExtreme(neighborSection, neighborOffset,
                                    LightEngine.QueueEntry.increaseLightFromEmission(emission, borderremover$isEmptyShape(neighborState)));
                        }
                    } else {
                        this.borderremover$enqueueDecreaseExtreme(neighborSection, neighborOffset,
                                LightEngine.QueueEntry.decreaseSkipOneDirection(neighborLevel, direction.getOpposite()));
                        this.borderremover$propagateFromEmptySectionsExtreme(nWorldX, nWorldZ, direction, neighborLevel, false, emptyCount);
                    }
                } else {
                    this.borderremover$enqueueIncreaseExtreme(neighborSection, neighborOffset,
                            LightEngine.QueueEntry.increaseOnlyOneDirection(neighborLevel, false, direction.getOpposite()));
                }
            }
        }
    }

    @Unique
    private void borderremover$propagateFromEmptySectionsExtreme(int worldX, int worldZ, Direction direction, int level, boolean shouldIncrease, int emptySections) {
        if (emptySections == 0) return;
        int sectionRelativeX = SectionPos.sectionRelative(worldX);
        int sectionRelativeZ = SectionPos.sectionRelative(worldZ);
        boolean crossed = switch (direction) {
            case NORTH -> sectionRelativeZ == 15;
            case SOUTH -> sectionRelativeZ == 0;
            case WEST -> sectionRelativeX == 15;
            case EAST -> sectionRelativeX == 0;
            default -> false;
        };
        if (!crossed) return;

        int sectionX = SectionPos.blockToSectionCoord(worldX);
        int sectionZ = SectionPos.blockToSectionCoord(worldZ);
        int startSectionY = SectionPos.blockToSectionCoord(this.mutablePos.getY()) - 1;
        int endSectionY = startSectionY - emptySections + 1;

        for (int sy = startSectionY; sy >= endSectionY; --sy) {
            long sec = SectionPos.asLong(sectionX, sy, sectionZ);
            LayerLightSectionStorage storage = this.borderremover$storage();
            if (!storage.storingLightForSection(sec)) continue;

            int baseY = SectionPos.sectionToBlockCoord(sy);
            for (int dy = 15; dy >= 0; --dy) {
                int offset = borderremover$encodeOffset(worldX & 15, dy, worldZ & 15);
                if (shouldIncrease) {
                    this.borderremover$setStoredLevelExtreme(sec, offset, level);
                    if (level > 1) {
                        this.borderremover$enqueueIncreaseExtreme(sec, offset,
                                LightEngine.QueueEntry.increaseSkipOneDirection(level, true, direction.getOpposite()));
                    }
                } else {
                    this.borderremover$setStoredLevelExtreme(sec, offset, 0);
                    this.borderremover$enqueueDecreaseExtreme(sec, offset,
                            LightEngine.QueueEntry.decreaseSkipOneDirection(level, direction.getOpposite()));
                }
            }
        }
    }

    @Unique
    private boolean borderremover$shapeOccludesExtreme(int wx1, int wy1, int wz1, BlockState s1, int wx2, int wy2, int wz2, BlockState s2, Direction dir) {
        this.mutablePos.set(wx1, wy1, wz1);
        VoxelShape vs1 = borderremover$isEmptyShape(s1) ? Shapes.empty() : s1.getFaceOcclusionShape(this.borderremover$chunkSource().getLevel(), this.mutablePos, dir);
        this.mutablePos.set(wx2, wy2, wz2);
        VoxelShape vs2 = borderremover$isEmptyShape(s2) ? Shapes.empty() : s2.getFaceOcclusionShape(this.borderremover$chunkSource().getLevel(), this.mutablePos, dir.getOpposite());
        return Shapes.faceShapeOccludes(vs1, vs2);
    }

    @Unique
    private void borderremover$enqueueIncreaseExtreme(long sectionPos, int offset, long entry) {
        this.borderRemover$increaseExtremeQueue.enqueue(sectionPos);
        this.borderRemover$increaseExtremeQueue.enqueue(borderremover$packQueueEntry(entry, offset));
    }

    @Unique
    private void borderremover$enqueueDecreaseExtreme(long sectionPos, int offset, long entry) {
        this.borderRemover$decreaseExtremeQueue.enqueue(sectionPos);
        this.borderRemover$decreaseExtremeQueue.enqueue(borderremover$packQueueEntry(entry, offset));
    }

    @Unique
    private int borderremover$getStoredLevelExtreme(long sectionPos, int offset) {
        return ((IStorageExtreme)(Object)this.borderremover$storage()).getStoredLevelExtreme(sectionPos, offset);
    }

    @Unique
    private void borderremover$setStoredLevelExtreme(long sectionPos, int offset, int level) {
        ((IStorageExtreme)(Object)this.borderremover$storage()).setStoredLevelExtreme(sectionPos, offset, level);
    }

    @Unique
    private BlockState borderremover$getState(BlockPos pos) {
        int cx = SectionPos.blockToSectionCoord(pos.getX());
        int cz = SectionPos.blockToSectionCoord(pos.getZ());
        net.minecraft.world.level.chunk.LightChunk chunk = this.borderremover$chunkSource().getChunkForLighting(cx, cz);
        return chunk == null ? net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState() : chunk.getBlockState(pos);
    }

    @Unique
    private int borderremover$getOpacity(BlockState state, BlockPos pos) {
        return Math.max(1, state.getLightBlock(this.borderremover$chunkSource().getLevel(), pos));
    }

    @Unique
    private int borderremover$countEmptySectionsBelow(long sectionPos, int worldY) {
        SkyLightSectionStorage skyStorage = (SkyLightSectionStorage) this.borderremover$storage();
        int baseSectionY = SectionPos.blockToSectionCoord(worldY);
        int emptyCount = 0;
        while (!skyStorage.storingLightForSection(
                SectionPos.asLong(SectionPos.x(sectionPos), baseSectionY - emptyCount - 1, SectionPos.z(sectionPos)))
                && skyStorage.hasLightDataAtOrBelow(baseSectionY - emptyCount - 1)) {
            emptyCount++;
        }
        return emptyCount;
    }
}
