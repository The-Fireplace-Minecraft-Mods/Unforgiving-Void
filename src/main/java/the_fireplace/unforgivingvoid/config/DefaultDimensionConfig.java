package the_fireplace.unforgivingvoid.config;

import dev.the_fireplace.lib.api.io.interfaces.access.SimpleBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.lib.api.lazyio.interfaces.Config;
import the_fireplace.unforgivingvoid.UnforgivingVoid;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class DefaultDimensionConfig extends DimensionConfig implements Config {

    @Inject
    public DefaultDimensionConfig(ConfigStateManager configStateManager) {
        configStateManager.initialize(this);
    }

    @Override
    public String getId() {
        return UnforgivingVoid.MODID + "_defaultDimensionConfig";
    }

    @Override
    public void afterReload(SimpleBuffer changedValues) {
        super.afterReload(changedValues);
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        isEnabled = buffer.readBool("isEnabled", true);
        triggerDistance = buffer.readByte("triggerDistance", (byte) 32);
        dropObsidian = buffer.readBool("dropObsidian", false);
        fireResistanceSeconds = buffer.readInt("fireResistanceSeconds", 180);
        horizontalDistanceOffset = buffer.readInt("horizontalDistanceOffset", 128);
        targetDimension = buffer.readString("targetDimension", "nether");
        approximateSpawnY = buffer.readShort("approximateSpawnY", (short) 128);
        attemptFindSafePlatform = buffer.readBool("attemptFindSafePlatform", true);
        avoidSkySpawning = buffer.readBool("avoidSkySpawning", true);
    }
}
