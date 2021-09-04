package the_fireplace.unforgivingvoid.entrypoints;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.the_fireplace.annotateddi.api.DIContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import the_fireplace.unforgivingvoid.config.UVConfigScreenFactory;

@Environment(EnvType.CLIENT)
public final class ModMenuEntrypoint implements ModMenuApi {
    private final UVConfigScreenFactory configScreenFactory = DIContainer.get().getInstance(UVConfigScreenFactory.class);

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return configScreenFactory::getConfigScreen;
    }
}
