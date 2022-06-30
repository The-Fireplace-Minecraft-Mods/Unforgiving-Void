package dev.the_fireplace.unforgivingvoid.config;

import dev.the_fireplace.lib.api.chat.injectables.TranslatorFactory;
import dev.the_fireplace.lib.api.chat.interfaces.Translator;
import dev.the_fireplace.lib.api.client.injectables.ConfigScreenBuilderFactory;
import dev.the_fireplace.lib.api.client.interfaces.ConfigScreenBuilder;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.domain.config.DimensionSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public final class UVConfigScreenFactory
{
    private static final String TRANSLATION_BASE = "text.config." + UnforgivingVoidConstants.MODID + ".";
    private static final String OPTION_TRANSLATION_BASE = TRANSLATION_BASE + "option.";

    private final Translator translator;
    private final ConfigScreenBuilderFactory configScreenBuilderFactory;
    private final DimensionConfigManager dimensionConfigManager;

    private final DimensionConfig fallbackDimensionConfig;
    private final DimensionSettings defaultFallbackSettings;

    private ConfigScreenBuilder configScreenBuilder;

    @Inject
    public UVConfigScreenFactory(
        TranslatorFactory translatorFactory,
        ConfigScreenBuilderFactory configScreenBuilderFactory,
        DimensionConfigManager dimensionConfigManager,
        FallbackDimensionConfig fallbackDimensionConfig,
        @Named("default") DimensionSettings defaultFallbackSettings
    ) {
        this.translator = translatorFactory.getTranslator(UnforgivingVoidConstants.MODID);
        this.configScreenBuilderFactory = configScreenBuilderFactory;
        this.dimensionConfigManager = dimensionConfigManager;
        this.fallbackDimensionConfig = fallbackDimensionConfig;
        this.defaultFallbackSettings = defaultFallbackSettings;
    }

    public Screen getConfigScreen(Screen parent) {
        this.configScreenBuilder = configScreenBuilderFactory.create(
            translator,
            TRANSLATION_BASE + "title",
            TRANSLATION_BASE + "default",
            parent,
            dimensionConfigManager::saveAll
        ).get();

        buildDefaultDimensionConfigCategory(fallbackDimensionConfig);
        for (ResourceLocation customMobId : dimensionConfigManager.getDimensionIdsWithCustomSettings()) {
            buildCustomDimensionConfigCategory(customMobId, dimensionConfigManager.getSettings(customMobId));
        }

        return this.configScreenBuilder.build();
    }

    private void addCommonDimensionConfigCategoryOptions(DimensionConfig dimensionConfig, DimensionSettings defaultSettings) {
        configScreenBuilder.addBoolToggle(
            OPTION_TRANSLATION_BASE + "isEnabled",
            dimensionConfig.isEnabled(),
            defaultSettings.isEnabled(),
            dimensionConfig::setEnabled
        ).setDescriptionRowCount((byte) 0);
        configScreenBuilder.addByteField(
            OPTION_TRANSLATION_BASE + "triggerDistance",
            dimensionConfig.getTriggerDistance(),
            defaultSettings.getTriggerDistance(),
            dimensionConfig::setTriggerDistance
        ).setMinimum((byte) 1).setDescriptionRowCount((byte) 2);
        configScreenBuilder.addBoolToggle(
            OPTION_TRANSLATION_BASE + "dropObsidian",
            dimensionConfig.isDropObsidian(),
            defaultSettings.isDropObsidian(),
            dimensionConfig::setDropObsidian
        );
        configScreenBuilder.addIntField(
            OPTION_TRANSLATION_BASE + "fireResistanceSeconds",
            dimensionConfig.getFireResistanceSeconds(),
            defaultSettings.getFireResistanceSeconds(),
            dimensionConfig::setFireResistanceSeconds
        ).setMinimum(0).setDescriptionRowCount((byte) 2);
        configScreenBuilder.addIntField(
            OPTION_TRANSLATION_BASE + "slowFallingSeconds",
            dimensionConfig.getSlowFallingSeconds(),
            defaultSettings.getSlowFallingSeconds(),
            dimensionConfig::setSlowFallingSeconds
        ).setMinimum(0).setDescriptionRowCount((byte) 2);
        configScreenBuilder.addIntField(
            OPTION_TRANSLATION_BASE + "horizontalDistanceOffset",
            dimensionConfig.getHorizontalDistanceOffset(),
            defaultSettings.getHorizontalDistanceOffset(),
            dimensionConfig::setHorizontalDistanceOffset
        ).setMinimum(0);
        Set<String> dimensionIds = getDimensionIds();
        configScreenBuilder.addStringDropdown(
            OPTION_TRANSLATION_BASE + "targetDimension",
            dimensionConfig.getTargetDimension(),
            defaultSettings.getTargetDimension(),
            dimensionIds,
            dimensionConfig::setTargetDimension
        ).setDescriptionRowCount((byte) 2);
        configScreenBuilder.addEnumDropdown(
            OPTION_TRANSLATION_BASE + "transferPositionMode",
            dimensionConfig.getTransferPositionMode(),
            defaultSettings.getTransferPositionMode(),
            TargetSpawnPositioning.values(),
            dimensionConfig::setTransferPositionMode
        ).setDescriptionRowCount((byte) 0);
    }

    private Set<String> getDimensionIds() {
        Set<String> dimensionIds = new HashSet<>();
        for (ResourceLocation dimensionId : dimensionConfigManager.getDimensionIds()) {
            dimensionIds.add(dimensionId.toString());
        }
        return dimensionIds;
    }

    private void buildDefaultDimensionConfigCategory(DimensionConfig dimensionConfig) {
        addCommonDimensionConfigCategoryOptions(dimensionConfig, defaultFallbackSettings);
        createAddCustomDimensionDropdown();
    }

    private void createAddCustomDimensionDropdown() {
        // Not a real, savable option, but it's the easiest way to allow adding custom dimension settings in the GUI without hacking cloth config or building something custom that users won't be familiar with.
        configScreenBuilder.addStringDropdown(
            OPTION_TRANSLATION_BASE + "addCustomDimensionConfig",
            "",
            "",
            dimensionConfigManager.getDimensionIdsWithoutCustomSettings().stream().map(ResourceLocation::toString).sorted().collect(Collectors.toList()),
            newValue -> {
                if (!newValue.isEmpty()) {
                    dimensionConfigManager.addCustom(new ResourceLocation(newValue), fallbackDimensionConfig.clone());
                }
            }
        ).setDescriptionRowCount((byte) 3).setErrorSupplier(value ->
            dimensionConfigManager.isCustom(new ResourceLocation(value))
                ? Optional.of(translator.getTranslatedString(OPTION_TRANSLATION_BASE + "addCustomDimensionConfig.error"))
                : Optional.empty()
        );
    }

    private void buildCustomDimensionConfigCategory(ResourceLocation identifier, DimensionConfig dimensionConfig) {
        configScreenBuilder.startCategory(TRANSLATION_BASE + "custom", identifier.toString());
        addCommonDimensionConfigCategoryOptions(dimensionConfig, fallbackDimensionConfig);
        createRemoveCustomDimensionButton(identifier);
    }

    private void createRemoveCustomDimensionButton(ResourceLocation identifier) {
        // Fake option, used to allow deleting custom settings without hacking cloth config
        configScreenBuilder.addBoolToggle(
            OPTION_TRANSLATION_BASE + "deleteCustomDimensionConfig",
            false,
            false,
            newValue -> {
                if (newValue) {
                    dimensionConfigManager.deleteCustom(identifier);
                }
            }
        ).setDescriptionRowCount((byte) 2);
    }
}
