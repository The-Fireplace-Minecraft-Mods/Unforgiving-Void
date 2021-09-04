package the_fireplace.unforgivingvoid.config;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.interfaces.HierarchicalConfig;

public class DimensionConfig implements HierarchicalConfig {
    protected boolean isEnabled;
    protected short triggerAtY;
    protected boolean dropObsidian;
    protected int fireResistanceSeconds;
    protected int horizontalDistanceOffset;
    protected String targetDimension;
    protected short approximateSpawnY;
    protected boolean attemptFindSafePlatform;
    protected boolean avoidSkySpawning;

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DimensionConfig clone() {
        DimensionConfig clone = new DimensionConfig();
        clone.isEnabled = isEnabled;
        clone.triggerAtY = triggerAtY;
        clone.dropObsidian = dropObsidian;
        clone.fireResistanceSeconds = fireResistanceSeconds;
        clone.horizontalDistanceOffset = horizontalDistanceOffset;
        clone.targetDimension = targetDimension;
        clone.approximateSpawnY = approximateSpawnY;
        clone.attemptFindSafePlatform = attemptFindSafePlatform;
        clone.avoidSkySpawning = avoidSkySpawning;

        return clone;
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        DimensionConfig defaultConfig = DIContainer.get().getInstance(DefaultDimensionConfig.class);
        isEnabled = buffer.readBool("isEnabled", defaultConfig.isEnabled());
        triggerAtY = buffer.readShort("triggerAtY", defaultConfig.getTriggerAtY());
        dropObsidian = buffer.readBool("dropObsidian", defaultConfig.isDropObsidian());
        fireResistanceSeconds = buffer.readInt("fireResistanceSeconds", defaultConfig.getFireResistanceSeconds());
        horizontalDistanceOffset = buffer.readInt("horizontalDistanceOffset", defaultConfig.getHorizontalDistanceOffset());
        targetDimension = buffer.readString("targetDimension", defaultConfig.getTargetDimension());
        approximateSpawnY = buffer.readShort("approximateSpawnY", defaultConfig.getApproximateSpawnY());
        attemptFindSafePlatform = buffer.readBool("attemptFindSafePlatform", defaultConfig.isAttemptFindSafePlatform());
        avoidSkySpawning = buffer.readBool("avoidSkySpawning", defaultConfig.isAvoidSkySpawning());
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeBool("isEnabled", isEnabled);
        buffer.writeShort("triggerAtY", triggerAtY);
        buffer.writeBool("dropObsidian", dropObsidian);
        buffer.writeInt("fireResistanceSeconds", fireResistanceSeconds);
        buffer.writeInt("horizontalDistanceOffset", horizontalDistanceOffset);
        buffer.writeString("targetDimension", targetDimension);
        buffer.writeShort("approximateSpawnY", approximateSpawnY);
        buffer.writeBool("attemptFindSafePlatform", attemptFindSafePlatform);
        buffer.writeBool("avoidSkySpawning", avoidSkySpawning);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public short getTriggerAtY() {
        return triggerAtY;
    }

    public void setTriggerAtY(short triggerAtY) {
        this.triggerAtY = triggerAtY;
    }

    public boolean isDropObsidian() {
        return dropObsidian;
    }

    public void setDropObsidian(boolean dropObsidian) {
        this.dropObsidian = dropObsidian;
    }

    public int getFireResistanceSeconds() {
        return fireResistanceSeconds;
    }

    public void setFireResistanceSeconds(int fireResistanceSeconds) {
        this.fireResistanceSeconds = fireResistanceSeconds;
    }

    public int getHorizontalDistanceOffset() {
        return horizontalDistanceOffset;
    }

    public void setHorizontalDistanceOffset(int horizontalDistanceOffset) {
        this.horizontalDistanceOffset = horizontalDistanceOffset;
    }

    public String getTargetDimension() {
        return targetDimension;
    }

    public void setTargetDimension(String targetDimension) {
        this.targetDimension = targetDimension;
    }

    public short getApproximateSpawnY() {
        return approximateSpawnY;
    }

    public void setApproximateSpawnY(short approximateSpawnY) {
        this.approximateSpawnY = approximateSpawnY;
    }

    public boolean isAttemptFindSafePlatform() {
        return attemptFindSafePlatform;
    }

    public void setAttemptFindSafePlatform(boolean attemptFindSafePlatform) {
        this.attemptFindSafePlatform = attemptFindSafePlatform;
    }

    public boolean isAvoidSkySpawning() {
        return avoidSkySpawning;
    }

    public void setAvoidSkySpawning(boolean avoidSkySpawning) {
        this.avoidSkySpawning = avoidSkySpawning;
    }
}
