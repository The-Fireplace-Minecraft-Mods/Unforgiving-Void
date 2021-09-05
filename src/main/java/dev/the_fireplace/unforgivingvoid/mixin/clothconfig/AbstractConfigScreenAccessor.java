package dev.the_fireplace.unforgivingvoid.mixin.clothconfig;

import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(value = AbstractConfigScreen.class, remap = false)
public interface AbstractConfigScreenAccessor {
    @Accessor
    Screen getParent();
}
