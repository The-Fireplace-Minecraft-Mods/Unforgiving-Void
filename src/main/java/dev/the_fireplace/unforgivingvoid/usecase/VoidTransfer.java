package dev.the_fireplace.unforgivingvoid.usecase;

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
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public final class VoidTransfer {

    private final DimensionConfigManager dimensionConfigManager;
    private final SwitchDimensions switchDimensions;
    private final SpawnPositionLocator spawnPositionLocator;

    @Inject
    public VoidTransfer(DimensionConfigManager dimensionConfigManager, SwitchDimensions switchDimensions, SpawnPositionLocator spawnPositionLocator) {
        this.dimensionConfigManager = dimensionConfigManager;
        this.switchDimensions = switchDimensions;
        this.spawnPositionLocator = spawnPositionLocator;
    }

    public void initiateVoidTransfer(ServerPlayerEntity serverPlayerEntity, MinecraftServer server) {
        ServerWorld world = serverPlayerEntity.getServerWorld();
        DimensionConfig dimensionConfig = dimensionConfigManager.getSettings(world.getRegistryKey().getValue());

        ServerWorld targetWorld = getTargetWorld(server, dimensionConfig);
        if (targetWorld == null) {
            UnforgivingVoidConstants.getLogger().error("Target world not found: " + dimensionConfig.getTargetDimension());
            return;
        }
        spawnPositionLocator.setHorizontalOffsetRange(dimensionConfig.getHorizontalDistanceOffset());
        BlockPos spawnPos;
        switch (dimensionConfig.getTransferPositionMode()) {
            case SIMILAR:
                spawnPos = spawnPositionLocator.findSimilarPosition(serverPlayerEntity.getType(), world, targetWorld, serverPlayerEntity.getBlockPos());
                break;
            case SURFACE:
                spawnPos = spawnPositionLocator.findSurfacePosition(serverPlayerEntity.getType(), world, targetWorld, serverPlayerEntity.getBlockPos());
                break;
            case FALL_FROM_SKY:
                spawnPos = spawnPositionLocator.findSkyPosition(serverPlayerEntity.getType(), world, targetWorld, serverPlayerEntity.getBlockPos());
                break;
            case SPAWNPOINT:
            default:
                spawnPos = spawnPositionLocator.findSpawnPosition(serverPlayerEntity.getType(), targetWorld);
        }

        applyStatusEffects(serverPlayerEntity, dimensionConfig);
        switchDimensions.switchDimensions(serverPlayerEntity, targetWorld, spawnPos);
        createAssistanceMaterials(dimensionConfig, targetWorld, spawnPos);
    }

    @Nullable
    private ServerWorld getTargetWorld(MinecraftServer server, DimensionConfig dimensionConfig) {
        RegistryKey<World> targetWorldRegistryKey = createTargetWorldRegistryKey(dimensionConfig);

        return server.getWorld(targetWorldRegistryKey);
    }

    private RegistryKey<World> createTargetWorldRegistryKey(DimensionConfig dimensionConfig) {
        return RegistryKey.of(Registry.WORLD_KEY, new Identifier(dimensionConfig.getTargetDimension()));
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