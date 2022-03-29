package dev.the_fireplace.unforgivingvoid.entrypoints;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.UVConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class ModMenu implements ModMenuApi
{
    private final UVConfigScreenFactory configScreenFactory = DIContainer.get().getInstance(UVConfigScreenFactory.class);

    @Override
    public String getModId() {
        return UnforgivingVoidConstants.MODID;
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return configScreenFactory::getConfigScreen;
    }
}
