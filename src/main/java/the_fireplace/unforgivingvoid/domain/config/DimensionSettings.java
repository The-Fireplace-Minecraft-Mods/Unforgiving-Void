package the_fireplace.unforgivingvoid.domain.config;

public interface DimensionSettings {
    boolean isEnabled();

    byte getTriggerDistance();

    boolean isDropObsidian();

    int getFireResistanceSeconds();

    int getHorizontalDistanceOffset();

    String getTargetDimension();

    short getApproximateSpawnY();

    boolean isAttemptFindSafePlatform();

    boolean isAvoidSkySpawning();
}
