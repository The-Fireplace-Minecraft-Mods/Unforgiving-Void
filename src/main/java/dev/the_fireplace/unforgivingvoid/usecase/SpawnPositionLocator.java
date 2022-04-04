package dev.the_fireplace.unforgivingvoid.usecase;


import dev.the_fireplace.lib.api.teleport.injectables.SafePosition;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Random;

public final class SpawnPositionLocator
{
    private static final int MAX_SCAN_ITERATIONS = 2048;

    private final SafePosition safePosition;
    private int horizontalOffsetRange = 128;

    @Inject
    public SpawnPositionLocator(SafePosition safePosition) {
        this.safePosition = safePosition;
    }

    public BlockPos findSimilarPosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Optional<Vec3d> spawnVec;
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getRegistryKey(), targetWorld.getRegistryKey(), currentPos);
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to similar position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding similar position in {}, falling back to finding the spawn position instead.",
                    targetWorld.getRegistryKey().getValue().toString()
                );
                return findSpawnPosition(entityType, targetWorld);
            }
            int targetY = rand.nextInt(targetWorld.getLogicalHeight() - 20) + 10 + targetWorld.getBottomY();

            spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition, targetY);
        } while (spawnVec.isEmpty());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSurfacePosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Optional<Vec3d> spawnVec;
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getRegistryKey(), targetWorld.getRegistryKey(), currentPos);
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to surface position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding surface position in {}, falling back to finding the spawn position instead.",
                    targetWorld.getRegistryKey().getValue().toString()
                );
                return findSpawnPosition(entityType, targetWorld);
            }

            spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        } while (spawnVec.isEmpty());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSkyPosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getRegistryKey(), targetWorld.getRegistryKey(), currentPos);
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to sky position, iteration {}", iteration);

            int spawnHeight = targetWorld.getChunkManager().getChunkGenerator().getSpawnHeight(targetWorld);

            int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
            int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());
            int targetY = rand.nextInt(targetWorld.getLogicalHeight() - spawnHeight) + spawnHeight - 6 /* minus 6 because bedrock ceiling */;

            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            if (isSafeSky(entityType, targetWorld, attemptPos)) {
                return attemptPos;
            }
        } while (iteration++ < MAX_SCAN_ITERATIONS);

        UnforgivingVoidConstants.getLogger().warn(
            "Max attempts exceeded for finding sky position in {}, falling back to finding the spawn position instead.",
            targetWorld.getRegistryKey().getValue().toString()
        );

        return findSpawnPosition(entityType, targetWorld);
    }

    public BlockPos findSpawnPosition(EntityType<?> entityType, ServerWorld targetWorld) {
        BlockPos targetFocalPosition = targetWorld.getSpawnPos();
        Optional<Vec3d> spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        int iteration = 0;
        while (spawnVec.isEmpty()) {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to spawn position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding spawn position in {}, falling back to the built in spawn position even though it may be unsafe.",
                    targetWorld.getRegistryKey().getValue().toString()
                );
                return targetFocalPosition;
            }

            spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        }

        return new BlockPos(spawnVec.get());
    }

    private Optional<Vec3d> findSafePlatform(EntityType<?> entityType, ServerWorld targetWorld, BlockPos targetFocalPosition, int targetY) {
        Random rand = targetWorld.getRandom();

        int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
        int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());

        BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

        return safePosition.findBy(entityType, targetWorld, attemptPos);
    }


    private Optional<Vec3d> findSafePlatform(EntityType<?> entityType, ServerWorld targetWorld, BlockPos targetFocalPosition) {
        Random rand = targetWorld.getRandom();

        int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
        int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());
        int targetY = targetWorld.getTopY(Heightmap.Type.WORLD_SURFACE, targetX, targetZ);
        BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

        return safePosition.findBy(entityType, targetWorld, attemptPos);
    }

    private boolean isSafeSky(EntityType<?> entityType, ServerWorld targetWorld, BlockPos blockPos) {
        Box skySpawnBoundingBox = entityType.createSimpleBoundingBox(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5)
            .withMinY(blockPos.getY() - 16); // minus 16, to guarantee there's always (at least) 16 blocks of space below the player (so they don't spawn on the ground)
        return targetWorld.isSpaceEmpty(skySpawnBoundingBox);
    }

    private BlockPos getDimensionScaledPosition(RegistryKey<World> originDimension, RegistryKey<World> targetDimension, BlockPos inputPos) {
        if (originDimension.equals(World.OVERWORLD) && targetDimension.equals(World.NETHER)) {
            return new BlockPos(inputPos.getX() / 8, inputPos.getY(), inputPos.getZ() / 8);
        } else if (originDimension.equals(World.NETHER) && targetDimension.equals(World.OVERWORLD)) {
            return new BlockPos(inputPos.getX() * 8, inputPos.getY(), inputPos.getZ() * 8);
        }

        return inputPos;
    }

    private int applyHorizontalOffset(Random rand, int xzCoordinate) {
        return xzCoordinate - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
    }

    public void setHorizontalOffsetRange(int horizontalOffsetRange) {
        this.horizontalOffsetRange = horizontalOffsetRange;
    }
}
