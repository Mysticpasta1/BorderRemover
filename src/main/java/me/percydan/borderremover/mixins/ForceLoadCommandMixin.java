package me.percydan.borderremover.mixins;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;

import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.ForceLoadCommand;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ForceLoadCommand.class)
public class ForceLoadCommandMixin {
    @Shadow
    @Final
    private static Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS;

    @Shadow
    @Final
    private static SimpleCommandExceptionType ERROR_ALL_ADDED;

    @Shadow
    @Final
    private static SimpleCommandExceptionType ERROR_NONE_REMOVED;

    /**
     * @author Mysticpasta1
     * @reason allow force loading in super far chunks
     */
    @Overwrite
    private static int changeForceLoad(CommandSourceStack pSource, ColumnPos pFrom, ColumnPos pTo, boolean pAdd) throws CommandSyntaxException {
        int i = Math.min(pFrom.x(), pTo.x());
        int j = Math.min(pFrom.z(), pTo.z());
        int k = Math.max(pFrom.x(), pTo.x());
        int l = Math.max(pFrom.z(), pTo.z());
        int i1 = SectionPos.blockToSectionCoord(i);
        int j1 = SectionPos.blockToSectionCoord(j);
        int k1 = SectionPos.blockToSectionCoord(k);
        int l1 = SectionPos.blockToSectionCoord(l);
        long i2 = ((long) (k1 - i1) + 1L) * ((long) (l1 - j1) + 1L);
        if (i2 > 256L) {
            throw ERROR_TOO_MANY_CHUNKS.create(256, i2);
        } else {
            ServerLevel serverlevel = pSource.getLevel();
            ResourceKey<Level> resourcekey = serverlevel.dimension();
            ChunkPos chunkpos = null;
            int j2 = 0;

            for (int k2 = i1; k2 <= k1; ++k2) {
                for (int l2 = j1; l2 <= l1; ++l2) {
                    boolean flag = serverlevel.setChunkForced(k2, l2, pAdd);
                    if (flag) {
                        ++j2;
                        if (chunkpos == null) {
                            chunkpos = new ChunkPos(k2, l2);
                        }
                    }
                }
            }

            ChunkPos chunkpos1 = chunkpos;
            if (j2 == 0) {
                throw (pAdd ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
            } else {
                if (j2 == 1) {
                    pSource.sendSuccess(() -> {
                        return Component.translatable("commands.forceload." + (pAdd ? "added" : "removed") + ".single", chunkpos1, resourcekey.location());
                    }, true);
                } else {
                    ChunkPos chunkpos2 = new ChunkPos(i1, j1);
                    ChunkPos chunkpos3 = new ChunkPos(k1, l1);
                    pSource.sendSuccess(() -> {
                        return Component.translatable("commands.forceload." + (pAdd ? "added" : "removed") + ".multiple", chunkpos1, resourcekey.location(), chunkpos2, chunkpos3);
                    }, true);
                }

                return j2;
            }
        }
    }
}
