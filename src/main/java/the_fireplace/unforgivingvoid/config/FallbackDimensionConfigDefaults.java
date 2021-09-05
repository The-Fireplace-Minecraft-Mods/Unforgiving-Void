package the_fireplace.unforgivingvoid.config;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import the_fireplace.unforgivingvoid.domain.config.DimensionSettings;

import javax.inject.Singleton;

@Implementation(name = "default")
@Singleton
public final class FallbackDimensionConfigDefaults implements DimensionSettings {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public byte getTriggerDistance() {
        return 32;
    }

    @Override
    public boolean isDropObsidian() {
        return false;
    }

    @Override
    public int getFireResistanceSeconds() {
        return 180;
    }

    @Override
    public int getHorizontalDistanceOffset() {
        return 128;
    }

    @Override
    public String getTargetDimension() {
        return "the_nether";
    }

    @Override
    public TargetSpawnPositioning getTransferPositionMode() {
        return TargetSpawnPositioning.SIMILAR;
    }
}
