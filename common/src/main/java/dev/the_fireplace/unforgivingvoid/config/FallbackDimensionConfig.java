package dev.the_fireplace.unforgivingvoid.config;

import dev.the_fireplace.lib.api.io.interfaces.access.SimpleBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.lib.api.lazyio.interfaces.Config;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.domain.config.DimensionSettings;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Locale;

@Singleton
public final class FallbackDimensionConfig extends DimensionConfig implements Config
{
    private final DimensionSettings defaultSettings;

    @Inject
    public FallbackDimensionConfig(ConfigStateManager configStateManager, @Named("default") DimensionSettings defaultSettings) {
        this.defaultSettings = defaultSettings;
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return UnforgivingVoidConstants.MODID + "_defaultDimensionConfig";
    }

    @Override
    public void afterReload(SimpleBuffer changedValues) {
        super.afterReload(changedValues);
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        isEnabled = buffer.readBool("isEnabled", defaultSettings.isEnabled());
        triggerDistance = buffer.readByte("triggerDistance", defaultSettings.getTriggerDistance());
        dropObsidian = buffer.readBool("dropObsidian", defaultSettings.isDropObsidian());
        fireResistanceSeconds = buffer.readInt("fireResistanceSeconds", defaultSettings.getFireResistanceSeconds());
        slowFallingSeconds = buffer.readInt("slowFallingSeconds", defaultSettings.getSlowFallingSeconds());
        horizontalDistanceOffset = buffer.readInt("horizontalDistanceOffset", defaultSettings.getHorizontalDistanceOffset());
        targetDimension = buffer.readString("targetDimension", defaultSettings.getTargetDimension());
        transferPositionMode = TargetSpawnPositioning.valueOf(buffer.readString("transferPositionMode", defaultSettings.getTransferPositionMode().name()).toUpperCase(Locale.ROOT));
    }
}
