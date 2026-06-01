package me.percydan.borderremover.mixins;

import com.mojang.logging.LogUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator {
    @Unique
    private static final Logger borderremover$LOGGER = LogUtils.getLogger();

    @Inject(method = "createStructures", at = @At("HEAD"), cancellable = true)
    private void borderremover$skipStructuresAtExtreme(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunkAccess, StructureTemplateManager templateManager, CallbackInfo ci) {
        ChunkPos chunkPos = chunkAccess.getPos();
        if (Math.abs(chunkPos.x) > 134_187_500 || Math.abs(chunkPos.z) > 134_187_500) {
            borderremover$LOGGER.debug("Skipping structures at extreme chunk ({}, {})", chunkPos.x, chunkPos.z);
            ci.cancel();
        }
    }
}
