package the_fireplace.unforgivingvoid.domain.config;

import the_fireplace.unforgivingvoid.config.TargetSpawnPositioning;

public interface DimensionSettings {
    boolean isEnabled();

    byte getTriggerDistance();

    boolean isDropObsidian();

    int getFireResistanceSeconds();

    int getHorizontalDistanceOffset();

    String getTargetDimension();

    TargetSpawnPositioning getTransferPositionMode();
}
