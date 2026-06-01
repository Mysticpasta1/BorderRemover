package me.percydan.borderremover.mixins;

import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.lighting.SkyLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkyLightEngine.class)
public interface SkyLightEngineAccessor {
    @Accessor("emptyChunkSources")
    ChunkSkyLightSources getEmptyChunkSources();

    @Invoker("getLowestSourceY")
    int invokeGetLowestSourceY(int pX, int pZ, int pDefaultReturnValue);

    @Invoker("getChunkSources")
    ChunkSkyLightSources invokeGetChunkSources(int pChunkX, int pChunkZ);

    @Invoker("updateSourcesInColumn")
    void invokeUpdateSourcesInColumn(int pX, int pZ, int pLowestY);
}
