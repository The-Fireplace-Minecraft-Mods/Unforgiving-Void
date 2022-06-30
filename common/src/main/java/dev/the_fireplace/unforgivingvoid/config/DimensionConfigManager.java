package dev.the_fireplace.unforgivingvoid.config;

import com.google.common.collect.Sets;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.lib.api.lazyio.injectables.HierarchicalConfigManagerFactory;
import dev.the_fireplace.lib.api.lazyio.interfaces.NamespacedHierarchicalConfigManager;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

@Singleton
public final class DimensionConfigManager
{
    public static final String DOMAIN = UnforgivingVoidConstants.MODID + "_customDimensionConfigs";
    public static final Set<ResourceLocation> DEFAULT_DIMENSIONS = Sets.newHashSet(
        Registry.DIMENSION_TYPE.getKey(DimensionType.OVERWORLD),
        Registry.DIMENSION_TYPE.getKey(DimensionType.NETHER),
        Registry.DIMENSION_TYPE.getKey(DimensionType.THE_END)
    );
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
        this.hierarchicalConfigManager = hierarchicalConfigManagerFactory.createDynamicNamespaced(
            DOMAIN,
            defaultSettings,
            DEFAULT_DIMENSIONS,
            Registry.DIMENSION_TYPE::keySet
        );
    }

    public Iterable<ResourceLocation> getDimensionIds() {
        return hierarchicalConfigManager.getAllowedModuleIds();
    }

    public Collection<ResourceLocation> getDimensionIdsWithCustomSettings() {
        return hierarchicalConfigManager.getCustoms();
    }

    public Collection<ResourceLocation> getDimensionIdsWithoutCustomSettings() {
        Set<ResourceLocation> idsWithoutSettings = Sets.newHashSet(hierarchicalConfigManager.getAllowedModuleIds());
        idsWithoutSettings.removeAll(getDimensionIdsWithCustomSettings());

        return idsWithoutSettings;
    }

    public DimensionConfig getSettings(ResourceLocation dimensionId) {
        return hierarchicalConfigManager.get(dimensionId);
    }

    public boolean isCustom(ResourceLocation dimensionId) {
        return hierarchicalConfigManager.isCustom(dimensionId);
    }

    public void addCustom(ResourceLocation dimensionId, DimensionConfig settings) {
        hierarchicalConfigManager.addCustom(dimensionId, settings);
    }

    public boolean deleteCustom(ResourceLocation dimensionId) {
        return hierarchicalConfigManager.deleteCustom(dimensionId);
    }

    public void saveAll() {
        this.configStateManager.save(defaultSettings);
        this.hierarchicalConfigManager.saveAllCustoms();
    }
}
