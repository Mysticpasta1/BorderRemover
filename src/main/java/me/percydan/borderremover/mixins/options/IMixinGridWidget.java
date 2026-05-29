package me.percydan.borderremover.mixins.options;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GridLayout.class)
public interface IMixinGridWidget {
    @Accessor("children")
    List<AbstractWidget> getChildren();
}
