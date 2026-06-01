package me.percydan.borderremover.mixins;

import me.percydan.borderremover.IStorageExtreme;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LayerLightSectionStorage.class)
public abstract class LayerLightSectionStorageMixin implements IStorageExtreme {
    @Shadow
    protected DataLayerStorageMap updatingSectionData;

    @Shadow
    protected LongSet changedSections;

    @Shadow
    protected Long2ObjectMap<DataLayer> queuedSections;

    @Shadow
    @Nullable
    protected abstract DataLayer getDataLayer(long pSectionPos, boolean pCached);

    @Shadow
    protected abstract DataLayer createDataLayer(long pSectionPos);

    @Shadow
    protected LongSet sectionsAffectedByLightUpdates;

    @Inject(method = "getStoredLevel", at = @At("HEAD"), cancellable = true)
    private void getStoredLevelSafe(long pLevelPos, CallbackInfoReturnable<Integer> cir) {
        long i = SectionPos.blockToSection(pLevelPos);
        DataLayer datalayer = this.getDataLayer(i, true);
        if (datalayer == null) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "setStoredLevel", at = @At("HEAD"), cancellable = true)
    private void setStoredLevelSafe(long pLevelPos, int pLightLevel, CallbackInfo ci) {
        long i = SectionPos.blockToSection(pLevelPos);
        if (!this.updatingSectionData.hasLayer(i)) {
            DataLayer queued = this.queuedSections.get(i);
            DataLayer layer = queued != null ? queued : this.createDataLayer(i);
            this.updatingSectionData.setLayer(i, layer);
            this.changedSections.add(i);
        }
    }

    @Override
    public int getStoredLevelExtreme(long sectionPos, int offset) {
        DataLayer datalayer = this.getDataLayer(sectionPos, true);
        if (datalayer == null) return 0;
        return datalayer.get(offset & 15, (offset >> 8) & 15, (offset >> 4) & 15);
    }

    @Override
    public void setStoredLevelExtreme(long sectionPos, int offset, int lightLevel) {
        DataLayer datalayer;
        if (this.changedSections.add(sectionPos)) {
            DataLayer existing = this.updatingSectionData.getLayer(sectionPos);
            if (existing != null) {
                datalayer = existing.copy();
            } else {
                DataLayer queued = this.queuedSections.get(sectionPos);
                datalayer = queued != null ? queued : new DataLayer();
            }
            this.updatingSectionData.setLayer(sectionPos, datalayer);
            this.updatingSectionData.clearCache();
        } else {
            datalayer = this.updatingSectionData.getLayer(sectionPos);
            if (datalayer == null) {
                DataLayer queued = this.queuedSections.get(sectionPos);
                datalayer = queued != null ? queued : new DataLayer();
                this.updatingSectionData.setLayer(sectionPos, datalayer);
                this.updatingSectionData.clearCache();
            }
        }
        datalayer.set(offset & 15, (offset >> 8) & 15, (offset >> 4) & 15, lightLevel);
        int sx = SectionPos.x(sectionPos);
        int sy = SectionPos.y(sectionPos);
        int sz = SectionPos.z(sectionPos);
        int bx = SectionPos.sectionToBlockCoord(sx) + (offset & 15);
        int by = SectionPos.sectionToBlockCoord(sy) + ((offset >> 8) & 15);
        int bz = SectionPos.sectionToBlockCoord(sz) + ((offset >> 4) & 15);
        SectionPos.aroundAndAtBlockPos(bx, by, bz, this.sectionsAffectedByLightUpdates::add);
    }
}
