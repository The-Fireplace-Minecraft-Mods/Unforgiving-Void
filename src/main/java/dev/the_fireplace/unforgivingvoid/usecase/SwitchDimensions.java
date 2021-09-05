package dev.the_fireplace.unforgivingvoid.usecase;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class SwitchDimensions {

    public void switchDimensions(ServerPlayerEntity serverPlayerEntity, ServerWorld targetWorld, BlockPos spawnPos) {
        preloadTargetChunk(targetWorld, spawnPos);
        serverPlayerEntity.teleport(targetWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), serverPlayerEntity.getYaw(), serverPlayerEntity.getPitch());
    }

    private void preloadTargetChunk(ServerWorld targetWorld, BlockPos spawnPos) {
        targetWorld.getChunk(spawnPos);
    }
}
