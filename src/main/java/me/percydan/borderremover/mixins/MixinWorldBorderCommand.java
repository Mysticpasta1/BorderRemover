package me.percydan.borderremover.mixins;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;

@Mixin(WorldBorderCommand.class)
public abstract class MixinWorldBorderCommand {
    @Final
    @Shadow
    private static SimpleCommandExceptionType ERROR_SAME_SIZE;

    @Redirect(method = "register", at = @At(target = "Lcom/mojang/brigadier/arguments/DoubleArgumentType;doubleArg(DD)Lcom/mojang/brigadier/arguments/DoubleArgumentType;", value = "INVOKE"))
    private static DoubleArgumentType handleConstructor(double min, double max) {
        return DoubleArgumentType.doubleArg(Float.MIN_VALUE);
    }


    /**
     * @author Mysticpasta1
     * @reason idk lol
     */
    @Overwrite
    private static int setSize(CommandSourceStack source, double distance, long time) throws CommandSyntaxException {
        WorldBorder worldBorder = source.getLevel().getWorldBorder();
        double d = worldBorder.getSize();
        if (d == distance) {
            throw ERROR_SAME_SIZE.create();
        } else {
            if (time > 0L) {
                worldBorder.lerpSizeBetween(d, distance, time);
                if (distance > d) {
                    source.sendSuccess(() -> Component.translatable("commands.worldborder.set.grow", String.format(Locale.ROOT, "%.1f", distance), Long.toString(time / 1000L)), true);
                } else {
                    source.sendSuccess(() -> Component.translatable("commands.worldborder.set.shrink", String.format(Locale.ROOT, "%.1f", distance), Long.toString(time / 1000L)), true);
                }
            } else {
                worldBorder.setSize(distance);
                source.sendSuccess(() -> Component.translatable("commands.worldborder.set.immediate", String.format(Locale.ROOT, "%.1f", distance)), true);
            }
            return (int) (distance - d);
        }
    }
}
