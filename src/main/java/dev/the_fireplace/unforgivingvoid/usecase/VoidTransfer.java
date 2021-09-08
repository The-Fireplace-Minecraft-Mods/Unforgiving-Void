package dev.the_fireplace.unforgivingvoid.usecase;

import dev.the_fireplace.lib.api.teleport.injectables.Teleporter;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfig;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfigManager;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public final class VoidTransfer {

    private final DimensionConfigManager dimensionConfigManager;
    private final Teleporter teleporter;
    private final SpawnPositionLocator spawnPositionLocator;

    @Inject
    public VoidTransfer(DimensionConfigManager dimensionConfigManager, Teleporter teleporter, SpawnPositionLocator spawnPositionLocator) {
        this.dimensionConfigManager = dimensionConfigManager;
        this.teleporter = teleporter;
        this.spawnPositionLocator = spawnPositionLocator;
    }

    public void initiateVoidTransfer(ServerPlayerEntity serverPlayerEntity, MinecraftServer server) {
        ServerWorld currentWorld = serverPlayerEntity.getServerWorld();
        DimensionConfig dimensionConfig = dimensionConfigManager.getSettings(Registry.DIMENSION.getId(currentWorld.getDimension().getType()));

        ServerWorld targetWorld = getTargetWorld(server, dimensionConfig);
        if (targetWorld == null) {
            UnforgivingVoidConstants.getLogger().error("Target world not found: " + dimensionConfig.getTargetDimension());
            return;
        }
        spawnPositionLocator.setHorizontalOffsetRange(dimensionConfig.getHorizontalDistanceOffset());
        BlockPos spawnPos = getSpawnPos(serverPlayerEntity, currentWorld, dimensionConfig, targetWorld);

        applyStatusEffects(serverPlayerEntity, dimensionConfig);
        teleporter.teleport(serverPlayerEntity, targetWorld, spawnPos);
        createAssistanceMaterials(dimensionConfig, targetWorld, spawnPos);
    }

    private BlockPos getSpawnPos(ServerPlayerEntity serverPlayerEntity, ServerWorld currentWorld, DimensionConfig dimensionConfig, ServerWorld targetWorld) {
        BlockPos spawnPos;
        switch (dimensionConfig.getTransferPositionMode()) {
            case SIMILAR:
                spawnPos = spawnPositionLocator.findSimilarPosition(serverPlayerEntity.getType(), currentWorld, targetWorld, serverPlayerEntity.getBlockPos());
                break;
            case SURFACE:
                spawnPos = spawnPositionLocator.findSurfacePosition(serverPlayerEntity.getType(), currentWorld, targetWorld, serverPlayerEntity.getBlockPos());
                break;
            case FALL_FROM_SKY:
                spawnPos = spawnPositionLocator.findSkyPosition(serverPlayerEntity.getType(), currentWorld, targetWorld, serverPlayerEntity.getBlockPos());
                break;
            case SPAWNPOINT:
            default:
                spawnPos = spawnPositionLocator.findSpawnPosition(serverPlayerEntity.getType(), targetWorld);
        }
        return spawnPos;
    }

    @Nullable
    private ServerWorld getTargetWorld(MinecraftServer server, DimensionConfig dimensionConfig) {
        return server.getWorld(Registry.DIMENSION.get(new Identifier(dimensionConfig.getTargetDimension())));
    }

    private void applyStatusEffects(ServerPlayerEntity serverPlayerEntity, DimensionConfig dimensionConfig) {
        serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 3));
        if (dimensionConfig.getFireResistanceSeconds() > 0) {
            serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, dimensionConfig.getFireResistanceSeconds() * 20));
        }
        if (dimensionConfig.getSlowFallingSeconds() > 0) {
            serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, dimensionConfig.getSlowFallingSeconds() * 20));
        }
    }

    private void createAssistanceMaterials(DimensionConfig dimensionConfig, ServerWorld targetWorld, BlockPos spawnPos) {
        if (dimensionConfig.isDropObsidian()) {
            targetWorld.spawnEntity(new ItemEntity(targetWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), new ItemStack(Blocks.OBSIDIAN, 14)));
        }
    }
}
