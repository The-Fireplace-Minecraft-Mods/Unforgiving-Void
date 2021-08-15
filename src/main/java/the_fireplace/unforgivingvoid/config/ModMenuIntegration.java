package the_fireplace.unforgivingvoid.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.the_fireplace.lib.api.client.ConfigScreenBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.StringVisitable;
import the_fireplace.unforgivingvoid.UnforgivingVoid;

@Environment(EnvType.CLIENT)
public final class ModMenuIntegration extends ConfigScreenBuilder implements ModMenuApi {
    private static final ModConfig.Access DEFAULT_CONFIG = ModConfig.getDefaultData();
    private static final String TRANSLATION_BASE = "text.config.unforgivingvoid.";
    private static final String OPTION_TRANSLATION_BASE = TRANSLATION_BASE + "option.";

    private final ModConfig.Access config = ModConfig.getData();

    public ModMenuIntegration() {
        super(UnforgivingVoid.getTranslator());
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(translator.getTranslatedText(TRANSLATION_BASE + "title"));

            buildConfigCategories(builder);

            builder.setSavingRunnable(() -> ModConfig.getInstance().save());
            return builder.build();
        };
    }

    private void buildConfigCategories(ConfigBuilder builder) {
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory nativeEconomy = builder.getOrCreateCategory(translator.getTranslatedText(TRANSLATION_BASE + "nativeEconomy"));
        nativeEconomy.setDescription(new StringVisitable[]{translator.getTranslatedText(TRANSLATION_BASE + "nativeEconomy.desc")});
        addNativeEconomyCategoryEntries(entryBuilder, nativeEconomy);
    }

    private void addNativeEconomyCategoryEntries(ConfigEntryBuilder entryBuilder, ConfigCategory nativeEconomy) {

    }
}
