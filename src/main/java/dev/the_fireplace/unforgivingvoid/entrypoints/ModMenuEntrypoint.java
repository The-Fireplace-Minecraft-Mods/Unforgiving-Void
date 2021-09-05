package dev.the_fireplace.unforgivingvoid.entrypoints;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.unforgivingvoid.config.UVConfigScreenFactory;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ModMenuEntrypoint implements ModMenuApi {
    private final UVConfigScreenFactory configScreenFactory = DIContainer.get().getInstance(UVConfigScreenFactory.class);

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return configScreenFactory::getConfigScreen;
    }
}
