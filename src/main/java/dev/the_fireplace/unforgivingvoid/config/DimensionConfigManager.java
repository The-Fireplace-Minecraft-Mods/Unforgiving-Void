package dev.the_fireplace.unforgivingvoid.config;

import com.google.common.collect.Sets;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.lib.api.lazyio.injectables.HierarchicalConfigManagerFactory;
import dev.the_fireplace.lib.api.lazyio.interfaces.NamespacedHierarchicalConfigManager;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

@Singleton
public final class DimensionConfigManager
{
    public static final String DOMAIN = UnforgivingVoidConstants.MODID + "_customDimensionConfigs";
    public static final Set<Identifier> DEFAULT_DIMENSIONS = Sets.newHashSet(DimensionType.OVERWORLD_ID, DimensionType.THE_NETHER_ID, DimensionType.THE_END_ID);
    private final NamespacedHierarchicalConfigManager<DimensionConfig> hierarchicalConfigManager;
    private final ConfigStateManager configStateManager;
    private final FallbackDimensionConfig defaultSettings;

    @Inject
    public DimensionConfigManager(
        HierarchicalConfigManagerFactory hierarchicalConfigManagerFactory,
        ConfigStateManager configStateManager,
        FallbackDimensionConfig defaultSettings
    ) {
        this.defaultSettings = defaultSettings;
        this.configStateManager = configStateManager;
        //noinspection ConstantConditions
        this.hierarchicalConfigManager = hierarchicalConfigManagerFactory.createDynamicNamespaced(
            DOMAIN,
            defaultSettings,
            DEFAULT_DIMENSIONS,
            () -> UnforgivingVoidConstants.getServer().getRegistryManager().getDimensionTypes().getIds()
        );
    }

    public Iterable<Identifier> getDimensionIds() {
        return hierarchicalConfigManager.getAllowedModuleIds();
    }

    public Collection<Identifier> getDimensionIdsWithCustomSettings() {
        return hierarchicalConfigManager.getCustoms();
    }

    public Collection<Identifier> getDimensionIdsWithoutCustomSettings() {
        Set<Identifier> idsWithoutSettings = Sets.newHashSet(hierarchicalConfigManager.getAllowedModuleIds());
        idsWithoutSettings.removeAll(getDimensionIdsWithCustomSettings());

        return idsWithoutSettings;
    }

    public DimensionConfig getSettings(Identifier dimensionId) {
        return hierarchicalConfigManager.get(dimensionId);
    }

    public boolean isCustom(Identifier dimensionId) {
        return hierarchicalConfigManager.isCustom(dimensionId);
    }

    public void addCustom(Identifier dimensionId, DimensionConfig settings) {
        hierarchicalConfigManager.addCustom(dimensionId, settings);
    }

    public boolean deleteCustom(Identifier dimensionId) {
        return hierarchicalConfigManager.deleteCustom(dimensionId);
    }

    public void saveAll() {
        this.configStateManager.save(defaultSettings);
        this.hierarchicalConfigManager.saveAllCustoms();
    }
}
