package the_fireplace.unforgivingvoid;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.entrypoints.DIModInitializer;
import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class UnforgivingVoid implements DIModInitializer {
    public static final String MODID = "unforgivingvoid";
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    public static Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void onInitialize(Injector diContainer) {
        diContainer.getInstance(TranslatorFactory.class).addTranslator(MODID);
    }
}
