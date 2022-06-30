package dev.the_fireplace.unforgivingvoid.init;

import dev.the_fireplace.lib.api.events.ConfigScreenRegistration;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.UVConfigScreenFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.inject.Inject;

public final class ConfigScreenEventHandler
{
    private final UVConfigScreenFactory configScreenFactory;

    @Inject
    public ConfigScreenEventHandler(UVConfigScreenFactory configScreenFactory) {
        this.configScreenFactory = configScreenFactory;
    }

    @SubscribeEvent
    public void onConfigGuiRegistration(ConfigScreenRegistration event) {
        event.getConfigGuiRegistry().register(UnforgivingVoidConstants.MODID, configScreenFactory::getConfigScreen);
    }
}
