package dev.the_fireplace.unforgivingvoid.usecase;

import dev.the_fireplace.lib.api.teleport.injectables.Teleporter;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfig;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfigManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;


public final class VoidTransfer
{
    private final DimensionConfigManager dimensionConfigManager;

    private final Teleporter teleporter;

    private final SpawnPositionLocator spawnPositionLocator;

    @Inject
    public VoidTransfer(DimensionConfigManager dimensionConfigManager, Teleporter teleporter, SpawnPositionLocator spawnPositionLocator) {
        this.dimensionConfigManager = dimensionConfigManager;
        this.teleporter = teleporter;
        this.spawnPositionLocator = spawnPositionLocator;
    }

    public void initiateVoidTransfer(ServerPlayer serverPlayerEntity, MinecraftServer server) {
        ServerLevel currentWorld = serverPlayerEntity.level();
        DimensionConfig dimensionConfig = dimensionConfigManager.getSettings(currentWorld.dimension().location());

        ServerLevel targetWorld = getTargetWorld(server, dimensionConfig);

        if (targetWorld == null) {
            UnforgivingVoidConstants.getLogger().error("Target world not found: " + dimensionConfig.getTargetDimension());
            return;
        }

        spawnPositionLocator.setHorizontalOffsetRange(dimensionConfig.getHorizontalDistanceOffset());
        Vec3 spawnPos = getSpawnPos(serverPlayerEntity, currentWorld, dimensionConfig, targetWorld);

        Entity teleportedEntity = teleporter.teleport(serverPlayerEntity, targetWorld, spawnPos.x, spawnPos.y, spawnPos.z);

        applyStatusEffects((ServerPlayer) teleportedEntity, dimensionConfig);
        createAssistanceMaterials(dimensionConfig, targetWorld, spawnPos);
        UnforgivingVoidConstants.getLogger().debug(
            "Player teleport complete. New position is {}, and new world is {}",
            teleportedEntity.blockPosition().toShortString(),
            teleportedEntity.level().dimension().location()
        );
    }

    private Vec3 getSpawnPos(ServerPlayer serverPlayerEntity, ServerLevel currentWorld, DimensionConfig dimensionConfig, ServerLevel targetWorld) {
        return switch (dimensionConfig.getTransferPositionMode()) {
            case SIMILAR ->
                spawnPositionLocator.findSimilarPosition(serverPlayerEntity.getType(), currentWorld, targetWorld, serverPlayerEntity.blockPosition());
            case SURFACE ->
                spawnPositionLocator.findSurfacePosition(serverPlayerEntity.getType(), currentWorld, targetWorld, serverPlayerEntity.blockPosition());
            case FALL_FROM_SKY ->
                spawnPositionLocator.findSkyPosition(serverPlayerEntity.getType(), currentWorld, targetWorld, serverPlayerEntity.blockPosition());
            case SPAWNPOINT -> spawnPositionLocator.findSpawnPosition(serverPlayerEntity.getType(), targetWorld);
        };
    }

    @Nullable
    private ServerLevel getTargetWorld(MinecraftServer server, DimensionConfig dimensionConfig) {
        ResourceKey<Level> targetWorldRegistryKey = createTargetWorldRegistryKey(dimensionConfig);
        if (targetWorldRegistryKey == null) {
            return null;
        }

        return server.getLevel(targetWorldRegistryKey);
    }

    @Nullable
    private ResourceKey<Level> createTargetWorldRegistryKey(DimensionConfig dimensionConfig) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(dimensionConfig.getTargetDimension());
        if (resourceLocation == null) {
            UnforgivingVoidConstants.getLogger().error("Target dimension not a valid resource location: {}", dimensionConfig.getTargetDimension());
            return null;
        }
        return ResourceKey.create(Registries.DIMENSION, resourceLocation);
    }

    private void applyStatusEffects(ServerPlayer serverPlayerEntity, DimensionConfig dimensionConfig) {
        serverPlayerEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 3));
        if (dimensionConfig.getFireResistanceSeconds() > 0) {
            serverPlayerEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dimensionConfig.getFireResistanceSeconds() * 20));
        }
        if (dimensionConfig.getSlowFallingSeconds() > 0) {
            serverPlayerEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dimensionConfig.getSlowFallingSeconds() * 20));
        }
    }

    private void createAssistanceMaterials(DimensionConfig dimensionConfig, ServerLevel targetWorld, Vec3 spawnPos) {
        if (dimensionConfig.isDropObsidian()) {
            targetWorld.addFreshEntity(new ItemEntity(targetWorld, spawnPos.x, spawnPos.y, spawnPos.z, new ItemStack(Blocks.OBSIDIAN, 14)));
        }
    }
}
