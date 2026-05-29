package me.percydan.borderremover;

import com.mojang.brigadier.arguments.FloatArgumentType;
import me.percydan.borderremover.config.Config;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("borderremover")
public class BorderRemover {
    public static Config config;

    public BorderRemover() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        Config.register();
        config = Config.INSTANCE;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("flyspeed").requires((commandSource) ->
                commandSource.hasPermission(2)
        ).then(Commands.argument("level", FloatArgumentType.floatArg()).executes((commandContext) -> {
            Player player = commandContext.getSource().getPlayerOrException();
            player.getAbilities().setFlyingSpeed(FloatArgumentType.getFloat(commandContext, "level") * 0.05f);
            player.onUpdateAbilities();
            return 1;
        })));
    }
}
