package the_fireplace.unforgivingvoid.config;

import com.google.common.collect.Sets;
import dev.the_fireplace.lib.api.lazyio.injectables.ConfigStateManager;
import dev.the_fireplace.lib.api.lazyio.injectables.HierarchicalConfigManagerFactory;
import dev.the_fireplace.lib.api.lazyio.interfaces.NamespacedHierarchicalConfigManager;
import net.minecraft.util.Identifier;
import the_fireplace.unforgivingvoid.UnforgivingVoid;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

@Singleton
public final class DimensionConfigManager {
    private final NamespacedHierarchicalConfigManager<DimensionConfig> hierarchicalConfigManager;
    private final ConfigStateManager configStateManager;
    private final DefaultDimensionConfig defaultSettings;

    private final Set<Identifier> allowedDimensionIdentifiers;

    @Inject
    public DimensionConfigManager(
        HierarchicalConfigManagerFactory hierarchicalConfigManagerFactory,
        ConfigStateManager configStateManager,
        DefaultDimensionConfig defaultSettings
    ) {
        this.defaultSettings = defaultSettings;
        this.configStateManager = configStateManager;
        this.allowedDimensionIdentifiers = Sets.newHashSet();//TODO Correct this
        this.hierarchicalConfigManager = hierarchicalConfigManagerFactory.createNamespaced(
            UnforgivingVoid.MODID + "_customDimensionConfigs",
            defaultSettings,
            allowedDimensionIdentifiers
        );
    }

    public Collection<Identifier> getAllowedDimensionIds() {
        return allowedDimensionIdentifiers;
    }

    public Collection<Identifier> getDimensionIdsWithCustomSettings() {
        return hierarchicalConfigManager.getCustoms();
    }

    public Collection<Identifier> getDimensionIdsWithoutCustomSettings() {
        Set<Identifier> idsWithoutSettings = Sets.newHashSet(allowedDimensionIdentifiers);
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
