package dev.the_fireplace.unforgivingvoid.usecase;

import dev.the_fireplace.lib.api.teleport.injectables.Teleporter;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfig;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;

import javax.annotation.Nullable;
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
        ServerLevel currentWorld = serverPlayerEntity.getLevel();
        DimensionConfig dimensionConfig = dimensionConfigManager.getSettings(Registry.DIMENSION_TYPE.getKey(currentWorld.getDimension().getType()));

        ServerLevel targetWorld = getTargetWorld(server, dimensionConfig);

        if (targetWorld == null) {
            UnforgivingVoidConstants.getLogger().error("Target world not found: " + dimensionConfig.getTargetDimension());
            return;
        }

        spawnPositionLocator.setHorizontalOffsetRange(dimensionConfig.getHorizontalDistanceOffset());
        BlockPos spawnPos = getSpawnPos(serverPlayerEntity, currentWorld, dimensionConfig, targetWorld);

        Entity teleportedEntity = teleporter.teleport(serverPlayerEntity, targetWorld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        applyStatusEffects((ServerPlayer) teleportedEntity, dimensionConfig);
        createAssistanceMaterials(dimensionConfig, targetWorld, spawnPos);
        UnforgivingVoidConstants.getLogger().debug(
            "Player teleport complete. New position is {}, and new world is {}",
            teleportedEntity.position().toString(),
            ((ServerPlayer) teleportedEntity).getLevel().dimension.getType().toString()
        );
    }

    private BlockPos getSpawnPos(ServerPlayer serverPlayerEntity, ServerLevel currentWorld, DimensionConfig dimensionConfig, ServerLevel targetWorld) {
        switch (dimensionConfig.getTransferPositionMode()) {
            case SIMILAR:
                return spawnPositionLocator.findSimilarPosition(serverPlayerEntity.getType(), currentWorld, targetWorld, new BlockPos(serverPlayerEntity.position()));
            case SURFACE:
                return spawnPositionLocator.findSurfacePosition(serverPlayerEntity.getType(), currentWorld, targetWorld, new BlockPos(serverPlayerEntity.position()));
            case FALL_FROM_SKY:
                return spawnPositionLocator.findSkyPosition(serverPlayerEntity.getType(), currentWorld, targetWorld, new BlockPos(serverPlayerEntity.position()));
            case SPAWNPOINT:
                return spawnPositionLocator.findSpawnPosition(serverPlayerEntity.getType(), targetWorld);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Nullable
    private ServerLevel getTargetWorld(MinecraftServer server, DimensionConfig dimensionConfig) {
        DimensionType targetWorldRegistryKey = createTargetWorldRegistryKey(dimensionConfig);

        return server.getLevel(targetWorldRegistryKey);
    }

    private DimensionType createTargetWorldRegistryKey(DimensionConfig dimensionConfig) {
        return Registry.DIMENSION_TYPE.get(new ResourceLocation(dimensionConfig.getTargetDimension()));
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

    private void createAssistanceMaterials(DimensionConfig dimensionConfig, ServerLevel targetWorld, BlockPos spawnPos) {
        if (dimensionConfig.isDropObsidian()) {
            targetWorld.addFreshEntity(new ItemEntity(targetWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), new ItemStack(Blocks.OBSIDIAN, 14)));
        }
    }
}
