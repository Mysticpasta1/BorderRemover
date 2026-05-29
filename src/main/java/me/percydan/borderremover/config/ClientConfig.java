package me.percydan.borderremover.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;

@OnlyIn(Dist.CLIENT)
public class ClientConfig {
    public static Screen createConfigScreen(Screen parent) {
        return ModList.get().getModContainerById("borderremover")
                .flatMap(container -> ConfigScreenHandler.getScreenFactoryFor(container.getModInfo()))
                .map(factory -> factory.apply(Minecraft.getInstance(), parent))
                .orElse(parent);
    }
}
