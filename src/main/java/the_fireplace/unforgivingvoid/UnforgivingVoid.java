package the_fireplace.unforgivingvoid;

import dev.the_fireplace.lib.api.chat.TranslatorManager;
import net.fabricmc.api.ModInitializer;

public class UnforgivingVoid implements ModInitializer {
    public static final String MODID = "unforgivingvoid";
    public static ModConfig config;

    @Override
    public void onInitialize() {
        TranslatorManager.getInstance().addTranslator(MODID);
    }
}
