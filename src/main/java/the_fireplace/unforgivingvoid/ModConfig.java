package the_fireplace.unforgivingvoid;

import dev.the_fireplace.lib.api.storage.access.intermediary.StorageReadBuffer;
import dev.the_fireplace.lib.api.storage.access.intermediary.StorageWriteBuffer;
import dev.the_fireplace.lib.api.storage.lazy.LazyConfig;

import java.util.Collection;

public class ModConfig extends LazyConfig {
    public short triggerAtY = -32;
    public boolean dropObsidian = false;
    public int fireResistanceSeconds = 180;
    public int horizontalDistanceOffset = 128;
    public String voidDimension = "*";
    public String targetDimension = "nether";
    public short approximateSpawnY = 128;
    public static final String[] SAFE_PLATFORM_MODES = {"always", "attempt", "off"};
    public String safePlatformMode = "always";
    public static final String[] SKY_SPAWNING_MODES = {"allow", "avoid", "never"};
    public String skySpawningMode = "avoid";

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
        safePlatformMode = buffer.readString("safePlatformMode", safePlatformMode);
        skySpawningMode = buffer.readString("skySpawningMode", skySpawningMode);
    }

    public String validate(String string, Collection<String> validOptions, String defaultValue) {
        if (!validOptions.contains(string)) {
            return defaultValue;
        }

        return string;
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {

    }
}
