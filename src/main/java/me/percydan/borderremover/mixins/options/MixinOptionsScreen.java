package me.percydan.borderremover.mixins.options;

import me.percydan.borderremover.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen implements IMixinScreen {
    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        int lastX = 0;
        int lastY = 0;
        int buttonCount = 0;
        List<AbstractWidget> widgets = null;
        Button doneButton = null;

        for (Renderable drawable : getRenderables()) {
            if (drawable instanceof GridLayout gridWidget) {
                widgets = ((IMixinGridWidget) gridWidget).getChildren();
                for (AbstractWidget clickableWidget : widgets) {
                    if (clickableWidget instanceof Button button) {
                        if (button.getWidth() != 200) {
                            lastX = button.getX();
                            lastY = button.getY();
                            buttonCount++;
                        } else {
                            doneButton = button;
                        }
                    }
                }
            }
        }

        if (widgets == null) {
            for (Renderable drawable : getRenderables()) {
                if (drawable instanceof Button button) {
                    if (button.getWidth() != 200) {
                        lastX = button.getX();
                        lastY = button.getY();
                        buttonCount++;
                    } else {
                        doneButton = button;
                    }
                }
            }
        }

        int posX, posY;
        if (buttonCount % 2 == 0) {
            posX = lastX + 160;
            posY = lastY;
        } else {
            posX = lastX - 160;
            posY = lastY + doneButton.getHeight() + 5;
            doneButton.setY(doneButton.getY() + doneButton.getHeight() + 5);
        }

        callAddRenderableWidget(Button.builder(Component.translatable("text.autoconfig.borderremover.title"), (button) -> {
                    Minecraft.getInstance().setScreen(ClientConfig.createConfigScreen((Screen) (Object) this));
                }
        ).bounds(posX, posY, 150, 20).build());
    }
}
