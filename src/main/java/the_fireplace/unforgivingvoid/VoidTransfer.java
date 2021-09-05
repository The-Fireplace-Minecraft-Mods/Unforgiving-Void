package the_fireplace.unforgivingvoid;

import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import the_fireplace.unforgivingvoid.config.DimensionConfig;
import the_fireplace.unforgivingvoid.config.DimensionConfigManager;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Random;

public final class VoidTransfer {

    private final DimensionConfigManager dimensionConfigManager;

    @Inject
    public VoidTransfer(DimensionConfigManager dimensionConfigManager) {
        this.dimensionConfigManager = dimensionConfigManager;
    }

    public void initiateVoidTransfer(ServerPlayerEntity serverPlayerEntity, MinecraftServer server) {
        ServerWorld world = serverPlayerEntity.getServerWorld();
        DimensionConfig dimensionConfig = dimensionConfigManager.getSettings(world.getRegistryKey().getValue());

        RegistryKey<World> targetWorldRegistryKey = createTargetWorldRegistryKey(dimensionConfig);
        ServerWorld targetWorld = server.getWorld(targetWorldRegistryKey);
        if (targetWorld == null) {
            UnforgivingVoidConstants.getLogger().error("Target world not found: " + dimensionConfig.getTargetDimension());
            return;
        }
        BlockPos playerPos = serverPlayerEntity.getBlockPos();
        BlockPos spawnPos = findSpawnPosition(serverPlayerEntity, dimensionConfig, targetWorld, playerPos);

        preloadTargetChunk(targetWorld, spawnPos);
        applyStatusEffects(serverPlayerEntity, dimensionConfig);
        serverPlayerEntity.teleport(targetWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), serverPlayerEntity.yaw, serverPlayerEntity.pitch);
        createAssistanceMaterials(dimensionConfig, targetWorld, spawnPos);
    }

    private RegistryKey<World> createTargetWorldRegistryKey(DimensionConfig dimensionConfig) {
        return RegistryKey.of(Registry.WORLD_KEY, new Identifier(dimensionConfig.getTargetDimension()));
    }

    private BlockPos findSpawnPosition(ServerPlayerEntity serverPlayerEntity, DimensionConfig dimensionConfig, ServerWorld targetWorld, BlockPos playerPos) {
        Optional<Vec3d> spawnVec;
        Random rand = targetWorld.getRandom();
        do {
            int targetX = playerPos.getX() / 8 - dimensionConfig.getHorizontalDistanceOffset() + rand.nextInt(dimensionConfig.getHorizontalDistanceOffset() * 2);
            int targetY = rand.nextInt(100) + 16;
            int targetZ = playerPos.getZ() / 8 - dimensionConfig.getHorizontalDistanceOffset() + rand.nextInt(dimensionConfig.getHorizontalDistanceOffset() * 2);
            spawnVec = RespawnAnchorBlock.findRespawnPosition(serverPlayerEntity.getType(), targetWorld, new BlockPos(targetX, targetY, targetZ));
        } while (!spawnVec.isPresent());

        return new BlockPos(spawnVec.get());
    }

    private void preloadTargetChunk(ServerWorld targetWorld, BlockPos spawnPos) {
        //Make sure the chunk is created BEFORE teleporting the player, or else we end up with the infinite derp loop
        targetWorld.getChunk(spawnPos);
    }

    private void applyStatusEffects(ServerPlayerEntity serverPlayerEntity, DimensionConfig dimensionConfig) {
        serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 3));
        if (dimensionConfig.getFireResistanceSeconds() > 0) {
            serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, dimensionConfig.getFireResistanceSeconds() * 20));
        }
    }

    private void createAssistanceMaterials(DimensionConfig dimensionConfig, ServerWorld targetWorld, BlockPos spawnPos) {
        if (dimensionConfig.isDropObsidian()) {
            targetWorld.spawnEntity(new ItemEntity(targetWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), new ItemStack(Blocks.OBSIDIAN, 14)));
        }
    }
}
