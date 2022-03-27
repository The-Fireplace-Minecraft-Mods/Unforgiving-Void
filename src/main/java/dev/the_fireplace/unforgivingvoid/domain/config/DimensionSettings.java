package dev.the_fireplace.unforgivingvoid.domain.config;

import dev.the_fireplace.unforgivingvoid.config.TargetSpawnPositioning;

public interface DimensionSettings
{
    boolean isEnabled();

    byte getTriggerDistance();

    boolean isDropObsidian();

    int getFireResistanceSeconds();

    int getSlowFallingSeconds();

    int getHorizontalDistanceOffset();

    String getTargetDimension();

    TargetSpawnPositioning getTransferPositionMode();
}
