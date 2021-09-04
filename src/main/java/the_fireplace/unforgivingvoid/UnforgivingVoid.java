package the_fireplace.unforgivingvoid;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.entrypoints.DIModInitializer;
import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import dev.the_fireplace.lib.api.lazyio.injectables.ReloadableManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import the_fireplace.unforgivingvoid.config.DimensionConfigManager;

import javax.annotation.Nullable;

public final class UnforgivingVoid implements DIModInitializer {
    public static final String MODID = "unforgivingvoid";
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    public static Logger getLogger() {
        return LOGGER;
    }

    @Nullable
    private static MinecraftServer server = null;

    private ReloadableManager reloadableManager;

    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitialize(Injector diContainer) {
        diContainer.getInstance(TranslatorFactory.class).addTranslator(MODID);
        reloadableManager = diContainer.getInstance(ReloadableManager.class);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    }

    private void onServerStarted(MinecraftServer server) {
        UnforgivingVoid.server = server;
        reloadableManager.reload("dynamic_" + DimensionConfigManager.DOMAIN);
    }

    private void onServerStopping(MinecraftServer server) {
        UnforgivingVoid.server = null;
    }
}
