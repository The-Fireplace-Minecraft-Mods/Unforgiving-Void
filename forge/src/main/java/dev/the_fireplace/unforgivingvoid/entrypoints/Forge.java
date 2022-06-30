package dev.the_fireplace.unforgivingvoid.entrypoints;

import com.google.inject.Injector;
import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import dev.the_fireplace.lib.api.events.FLEventBus;
import dev.the_fireplace.lib.api.lazyio.injectables.ReloadableManager;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfigManager;
import dev.the_fireplace.unforgivingvoid.init.ConfigScreenEventHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;
import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;

@Mod(UnforgivingVoidConstants.MODID)
public final class Forge
{
    private final ReloadableManager reloadableManager;

    public Forge() {
        Injector injector = UnforgivingVoidConstants.getInjector();
        injector.getInstance(TranslatorFactory.class).addTranslator(UnforgivingVoidConstants.MODID);
        reloadableManager = injector.getInstance(ReloadableManager.class);
        MinecraftForge.EVENT_BUS.register(this);
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            FLEventBus.BUS.register(injector.getInstance(ConfigScreenEventHandler.class));
            return null;
        });

        // Register as optional on both sides
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        UnforgivingVoidConstants.setServer(event.getServer());
        reloadableManager.reload("dynamic_" + DimensionConfigManager.DOMAIN);
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        UnforgivingVoidConstants.setServer(null);
    }
}
