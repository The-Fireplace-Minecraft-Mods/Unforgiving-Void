package the_fireplace.unforgivingvoid;

import dev.the_fireplace.lib.api.chat.Translator;
import dev.the_fireplace.lib.api.chat.TranslatorManager;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class UnforgivingVoid implements ModInitializer {
    public static final String MODID = "unforgivingvoid";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    public static Logger getLogger() {
        return LOGGER;
    }

    private static final TranslatorManager TRANSLATOR_MANAGER = TranslatorManager.getInstance();
    private static final Translator TRANSLATOR = TRANSLATOR_MANAGER.getTranslator(MODID);
    public static Translator getTranslator() {
        return TRANSLATOR;
    }

    @Override
    public void onInitialize() {
        TranslatorManager.getInstance().addTranslator(MODID);
    }
}
