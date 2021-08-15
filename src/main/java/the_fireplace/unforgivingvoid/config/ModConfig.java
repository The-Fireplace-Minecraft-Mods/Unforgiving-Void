package the_fireplace.unforgivingvoid.config;

import dev.the_fireplace.lib.api.storage.access.intermediary.StorageReadBuffer;
import dev.the_fireplace.lib.api.storage.access.intermediary.StorageWriteBuffer;
import dev.the_fireplace.lib.api.storage.lazy.LazyConfig;
import dev.the_fireplace.lib.api.storage.lazy.LazyConfigInitializer;
import the_fireplace.unforgivingvoid.UnforgivingVoid;

public final class ModConfig extends LazyConfig {
    private static final ModConfig INSTANCE = LazyConfigInitializer.lazyInitialize(new ModConfig());
    private static final ModConfig DEFAULT_INSTANCE = new ModConfig();
    private final Access access = new Access();

    public static ModConfig getInstance() {
        return INSTANCE;
    }
    public static Access getData() {
        return INSTANCE.access;
    }
    static Access getDefaultData() {
        return DEFAULT_INSTANCE.access;
    }

    private ModConfig() {}

    private short triggerAtY = -32;
    private boolean dropObsidian = false;
    private int fireResistanceSeconds = 180;
    private int horizontalDistanceOffset = 128;
    private String voidDimension = "*";
    private String targetDimension = "nether";
    private short approximateSpawnY = 128;
    private boolean attemptFindSafePlatform = true;
    private boolean avoidSkySpawning = true;

    @Override
    public String getId() {
        return UnforgivingVoid.MODID;
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        triggerAtY = buffer.readShort("triggerAtY", triggerAtY);
        dropObsidian = buffer.readBool("dropObsidian", dropObsidian);
        fireResistanceSeconds = buffer.readInt("fireResistanceSeconds", fireResistanceSeconds);
        horizontalDistanceOffset = buffer.readInt("horizontalDistanceOffset", horizontalDistanceOffset);
        voidDimension = buffer.readString("voidDimension", voidDimension);
        targetDimension = buffer.readString("targetDimension", targetDimension);
        approximateSpawnY = buffer.readShort("approximateSpawnY", approximateSpawnY);
        attemptFindSafePlatform = buffer.readBool("attemptFindSafePlatform", attemptFindSafePlatform);
        avoidSkySpawning = buffer.readBool("avoidSkySpawning", avoidSkySpawning);
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        buffer.writeShort("triggerAtY", triggerAtY);
        buffer.writeBool("dropObsidian", dropObsidian);
        buffer.writeInt("fireResistanceSeconds", fireResistanceSeconds);
        buffer.writeInt("horizontalDistanceOffset", horizontalDistanceOffset);
        buffer.writeString("voidDimension", voidDimension);
        buffer.writeString("targetDimension", targetDimension);
        buffer.writeShort("approximateSpawnY", approximateSpawnY);
        buffer.writeBool("attemptFindSafePlatform", attemptFindSafePlatform);
        buffer.writeBool("avoidSkySpawning", avoidSkySpawning);
    }

    public final class Access {
        public short getTriggerAtY() {
            return triggerAtY;
        }

        public void setTriggerAtY(short triggerAtY) {
            ModConfig.this.triggerAtY = triggerAtY;
        }

        public boolean isDropObsidian() {
            return dropObsidian;
        }

        public void setDropObsidian(boolean dropObsidian) {
            ModConfig.this.dropObsidian = dropObsidian;
        }

        public int getFireResistanceSeconds() {
            return fireResistanceSeconds;
        }

        public void setFireResistanceSeconds(int fireResistanceSeconds) {
            ModConfig.this.fireResistanceSeconds = fireResistanceSeconds;
        }

        public int getHorizontalDistanceOffset() {
            return horizontalDistanceOffset;
        }

        public void setHorizontalDistanceOffset(int horizontalDistanceOffset) {
            ModConfig.this.horizontalDistanceOffset = horizontalDistanceOffset;
        }

        public String getVoidDimension() {
            return voidDimension;
        }

        public void setVoidDimension(String voidDimension) {
            ModConfig.this.voidDimension = voidDimension;
        }

        public String getTargetDimension() {
            return targetDimension;
        }

        public void setTargetDimension(String targetDimension) {
            ModConfig.this.targetDimension = targetDimension;
        }

        public short getApproximateSpawnY() {
            return approximateSpawnY;
        }

        public void setApproximateSpawnY(short approximateSpawnY) {
            ModConfig.this.approximateSpawnY = approximateSpawnY;
        }

        public boolean isAttemptFindSafePlatform() {
            return attemptFindSafePlatform;
        }

        public void setAttemptFindSafePlatform(boolean attemptFindSafePlatform) {
            ModConfig.this.attemptFindSafePlatform = attemptFindSafePlatform;
        }

        public boolean isAvoidSkySpawning() {
            return avoidSkySpawning;
        }

        public void setAvoidSkySpawning(boolean avoidSkySpawning) {
            ModConfig.this.avoidSkySpawning = avoidSkySpawning;
        }
    }
}
