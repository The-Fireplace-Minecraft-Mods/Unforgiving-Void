package the_fireplace.unforgivingvoid.config;

import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import dev.the_fireplace.lib.api.chat.interfaces.Translator;
import dev.the_fireplace.lib.api.client.injectables.ConfigScreenBuilderFactory;
import dev.the_fireplace.lib.api.client.interfaces.ConfigScreenBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import the_fireplace.unforgivingvoid.UnforgivingVoid;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
@Singleton
public final class UVConfigScreenFactory {
    private static final String TRANSLATION_BASE = "text.config." + UnforgivingVoid.MODID + ".";
    private static final String OPTION_TRANSLATION_BASE = TRANSLATION_BASE + "option.";

    private final Translator translator;
    private final ConfigScreenBuilderFactory configScreenBuilderFactory;
    private final DimensionConfigManager dimensionConfigManager;

    private final DimensionConfig defaultDimensionConfig;

    private ConfigScreenBuilder configScreenBuilder;

    @Inject
    public UVConfigScreenFactory(
        TranslatorFactory translatorFactory,
        ConfigScreenBuilderFactory configScreenBuilderFactory,
        DimensionConfigManager dimensionConfigManager,
        DefaultDimensionConfig defaultDimensionConfig
    ) {
        this.translator = translatorFactory.getTranslator(UnforgivingVoid.MODID);
        this.configScreenBuilderFactory = configScreenBuilderFactory;
        this.dimensionConfigManager = dimensionConfigManager;
        this.defaultDimensionConfig = defaultDimensionConfig;
    }

    public Screen getConfigScreen(Screen parent) {
        this.configScreenBuilder = configScreenBuilderFactory.create(
            translator,
            TRANSLATION_BASE + "title",
            TRANSLATION_BASE + "default",
            parent,
            dimensionConfigManager::saveAll
        );

        buildDefaultDimensionConfigCategory(defaultDimensionConfig);
        for (Identifier customMobId : dimensionConfigManager.getDimensionIdsWithCustomSettings()) {
            buildCustomDimensionConfigCategory(customMobId, dimensionConfigManager.getSettings(customMobId));
        }

        return this.configScreenBuilder.build();
    }

    private void addCommonDimensionConfigCategoryOptions(DimensionConfig dimensionConfig) {
        configScreenBuilder.addBoolToggle(
            OPTION_TRANSLATION_BASE + "isEnabled",
            dimensionConfig.isEnabled(),
            defaultDimensionConfig.isEnabled(),
            dimensionConfig::setEnabled,
            (byte) 2
        );
    }

    private void buildDefaultDimensionConfigCategory(DimensionConfig dimensionConfig) {
        addCommonDimensionConfigCategoryOptions(dimensionConfig);
        createAddCustomDimensionDropdown();
    }

    private void createAddCustomDimensionDropdown() {
        // Not a real, savable option, but it's the easiest way to allow adding custom dimension settings in the GUI without hacking cloth config or building something custom that users won't be familiar with.
        configScreenBuilder.addStringDropdown(
            OPTION_TRANSLATION_BASE + "addCustomDimensionConfig",
            "",
            "",
            dimensionConfigManager.getDimensionIdsWithoutCustomSettings().stream().map(Identifier::toString).sorted().collect(Collectors.toList()),
            newValue -> {
                if (!newValue.isEmpty()) {
                    dimensionConfigManager.addCustom(new Identifier(newValue), defaultDimensionConfig.clone());
                }
            },
            false,
            (byte) 2,
            value ->
                dimensionConfigManager.isCustom(new Identifier(value))
                    ? Optional.of(translator.getTranslatedText(OPTION_TRANSLATION_BASE + "addCustomDimensionConfig.err"))
                    : Optional.empty()
        );
    }

    private void buildCustomDimensionConfigCategory(Identifier identifier, DimensionConfig dimensionConfig) {
        addCommonDimensionConfigCategoryOptions(dimensionConfig);
        createRemoveCustomDimensionButton(identifier);
    }

    private void createRemoveCustomDimensionButton(Identifier identifier) {
        // Fake option, used to allow deleting custom settings without hacking cloth config
        configScreenBuilder.addBoolToggle(
            OPTION_TRANSLATION_BASE + "deleteCustomDimensionConfig",
            false,
            false,
            newValue -> {
                if (newValue) {
                    dimensionConfigManager.deleteCustom(identifier);
                }
            },
            (byte) 2
        );
    }
}
