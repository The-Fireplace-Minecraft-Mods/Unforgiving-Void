package the_fireplace.unforgivingvoid.config;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.interfaces.HierarchicalConfig;
import the_fireplace.unforgivingvoid.domain.config.DimensionSettings;

import java.util.Locale;

public class DimensionConfig implements HierarchicalConfig, DimensionSettings {
    protected boolean isEnabled;
    protected byte triggerDistance;
    protected boolean dropObsidian;
    protected int fireResistanceSeconds;
    protected int horizontalDistanceOffset;
    protected String targetDimension;
    protected TargetSpawnPositioning transferPositionMode;

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DimensionConfig clone() {
        DimensionConfig clone = new DimensionConfig();
        clone.isEnabled = isEnabled;
        clone.triggerDistance = triggerDistance;
        clone.dropObsidian = dropObsidian;
        clone.fireResistanceSeconds = fireResistanceSeconds;
        clone.horizontalDistanceOffset = horizontalDistanceOffset;
        clone.targetDimension = targetDimension;
        clone.transferPositionMode = transferPositionMode;

        return clone;
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        DimensionConfig defaultConfig = DIContainer.get().getInstance(FallbackDimensionConfig.class);
        isEnabled = buffer.readBool("isEnabled", defaultConfig.isEnabled());
        triggerDistance = buffer.readByte("triggerDistance", defaultConfig.getTriggerDistance());
        dropObsidian = buffer.readBool("dropObsidian", defaultConfig.isDropObsidian());
        fireResistanceSeconds = buffer.readInt("fireResistanceSeconds", defaultConfig.getFireResistanceSeconds());
        horizontalDistanceOffset = buffer.readInt("horizontalDistanceOffset", defaultConfig.getHorizontalDistanceOffset());
        targetDimension = buffer.readString("targetDimension", defaultConfig.getTargetDimension());
        transferPositionMode = TargetSpawnPositioning.valueOf(buffer.readString("transferPositionMode", defaultConfig.getTransferPositionMode().name()).toUpperCase(Locale.ROOT));
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeBool("isEnabled", isEnabled);
        buffer.writeByte("triggerDistance", triggerDistance);
        buffer.writeBool("dropObsidian", dropObsidian);
        buffer.writeInt("fireResistanceSeconds", fireResistanceSeconds);
        buffer.writeInt("horizontalDistanceOffset", horizontalDistanceOffset);
        buffer.writeString("targetDimension", targetDimension);
        buffer.writeString("transferPositionMode", transferPositionMode.name());
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public byte getTriggerDistance() {
        return triggerDistance;
    }

    public void setTriggerDistance(byte triggerDistance) {
        this.triggerDistance = triggerDistance;
    }

    @Override
    public boolean isDropObsidian() {
        return dropObsidian;
    }

    public void setDropObsidian(boolean dropObsidian) {
        this.dropObsidian = dropObsidian;
    }

    @Override
    public int getFireResistanceSeconds() {
        return fireResistanceSeconds;
    }

    public void setFireResistanceSeconds(int fireResistanceSeconds) {
        this.fireResistanceSeconds = fireResistanceSeconds;
    }

    @Override
    public int getHorizontalDistanceOffset() {
        return horizontalDistanceOffset;
    }

    public void setHorizontalDistanceOffset(int horizontalDistanceOffset) {
        this.horizontalDistanceOffset = horizontalDistanceOffset;
    }

    @Override
    public String getTargetDimension() {
        return targetDimension;
    }

    public void setTargetDimension(String targetDimension) {
        this.targetDimension = targetDimension;
    }

    @Override
    public TargetSpawnPositioning getTransferPositionMode() {
        return transferPositionMode;
    }

    public void setTransferPositionMode(TargetSpawnPositioning transferPositionMode) {
        this.transferPositionMode = transferPositionMode;
    }
}
