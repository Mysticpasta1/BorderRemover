package me.percydan.borderremover.mixins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.world.level.dimension.DimensionType.*;

@Mixin(DimensionType.class)
public abstract class MixinDimensionType {
    @Shadow @Final @Mutable
    private static Codec<DimensionType> DIRECT_CODEC;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void replaceCodec(CallbackInfo ci) {
        DIRECT_CODEC = ExtraCodecs.catchDecoderException(RecordCodecBuilder.create((p_223568_) -> p_223568_
                .group(ExtraCodecs.asOptionalLong(Codec.LONG.optionalFieldOf("fixed_time"))
                        .forGetter(DimensionType::fixedTime), Codec.BOOL.fieldOf("has_skylight")
                        .forGetter(DimensionType::hasSkyLight), Codec.BOOL.fieldOf("has_ceiling")
                        .forGetter(DimensionType::hasCeiling), Codec.BOOL.fieldOf("ultrawarm")
                        .forGetter(DimensionType::ultraWarm), Codec.BOOL.fieldOf("natural")
                        .forGetter(DimensionType::natural), Codec.doubleRange(Integer.MIN_VALUE, Integer.MAX_VALUE).fieldOf("coordinate_scale")
                        .forGetter(DimensionType::coordinateScale), Codec.BOOL.fieldOf("bed_works")
                        .forGetter(DimensionType::bedWorks), Codec.BOOL.fieldOf("respawn_anchor_works")
                        .forGetter(DimensionType::respawnAnchorWorks), Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y")
                        .forGetter(DimensionType::minY), Codec.intRange(16, Y_SIZE).fieldOf("height")
                        .forGetter(DimensionType::height), Codec.intRange(0, Y_SIZE).fieldOf("logical_height")
                        .forGetter(DimensionType::logicalHeight), TagKey.hashedCodec(Registries.BLOCK).fieldOf("infiniburn")
                        .forGetter(DimensionType::infiniburn), ResourceLocation.CODEC.fieldOf("effects").orElse(BuiltinDimensionTypes.OVERWORLD_EFFECTS)
                        .forGetter(DimensionType::effectsLocation), Codec.FLOAT.fieldOf("ambient_light")
                        .forGetter(DimensionType::ambientLight), DimensionType.MonsterSettings.CODEC
                        .forGetter(DimensionType::monsterSettings)).apply(p_223568_, DimensionType::new)));


    }
}
