package dev.the_fireplace.unforgivingvoid.entrypoints;

import com.google.inject.Injector;
import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import dev.the_fireplace.lib.api.lazyio.injectables.ReloadableManager;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public final class Main implements ModInitializer
{

    private ReloadableManager reloadableManager;

    @Override
    public void onInitialize() {
        Injector injector = UnforgivingVoidConstants.getInjector();
        injector.getInstance(TranslatorFactory.class).addTranslator(UnforgivingVoidConstants.MODID);
        reloadableManager = injector.getInstance(ReloadableManager.class);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    }

    private void onServerStarted(MinecraftServer server) {
        UnforgivingVoidConstants.setServer(server);
        reloadableManager.reload("dynamic_" + DimensionConfigManager.DOMAIN);
    }

    private void onServerStopping(MinecraftServer server) {
        UnforgivingVoidConstants.setServer(null);
    }
}
