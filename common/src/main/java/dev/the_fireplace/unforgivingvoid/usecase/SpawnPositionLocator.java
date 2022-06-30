package dev.the_fireplace.unforgivingvoid.usecase;


import dev.the_fireplace.lib.api.teleport.injectables.SafePosition;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

    public BlockPos findSimilarPosition(EntityType<?> entityType, ServerLevel currentWorld, ServerLevel targetWorld, BlockPos currentPos) {
        Optional<Vec3> spawnVec;
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.dimension(), targetWorld.dimension(), currentPos);
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to similar position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding similar position in {}, falling back to finding the spawn position instead.",
                    targetWorld.dimension().location().toString()
                );
                return findSpawnPosition(entityType, targetWorld);
            }
            int targetY = rand.nextInt(targetWorld.getLogicalHeight() - 20) + 10 + targetWorld.getMinBuildHeight();

            spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition, targetY);
        } while (spawnVec.isEmpty());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSurfacePosition(EntityType<?> entityType, ServerLevel currentWorld, ServerLevel targetWorld, BlockPos currentPos) {
        Optional<Vec3> spawnVec;
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.dimension(), targetWorld.dimension(), currentPos);
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to surface position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding surface position in {}, falling back to finding the spawn position instead.",
                    targetWorld.dimension().location().toString()
                );
                return findSpawnPosition(entityType, targetWorld);
            }

            spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        } while (spawnVec.isEmpty());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSkyPosition(EntityType<?> entityType, ServerLevel currentWorld, ServerLevel targetWorld, BlockPos currentPos) {
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.dimension(), targetWorld.dimension(), currentPos);
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to sky position, iteration {}", iteration);

            int spawnHeight = targetWorld.getChunkSource().getGenerator().getSpawnHeight(targetWorld);

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
            targetWorld.dimension().location().toString()
        );

        return findSpawnPosition(entityType, targetWorld);
    }

    public BlockPos findSpawnPosition(EntityType<?> entityType, ServerLevel targetWorld) {
        BlockPos targetFocalPosition = targetWorld.getSharedSpawnPos();
        Optional<Vec3> spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        int iteration = 0;
        while (spawnVec.isEmpty()) {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to spawn position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding spawn position in {}, falling back to the built in spawn position even though it may be unsafe.",
                    targetWorld.dimension().location().toString()
                );
                return targetFocalPosition;
            }

            spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        }

        return new BlockPos(spawnVec.get());
    }

    private Optional<Vec3> findSafePlatform(EntityType<?> entityType, ServerLevel targetWorld, BlockPos targetFocalPosition, int targetY) {
        Random rand = targetWorld.getRandom();

        int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
        int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());

        BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

        return safePosition.findBy(entityType, targetWorld, attemptPos);
    }


    private Optional<Vec3> findSafePlatform(EntityType<?> entityType, ServerLevel targetWorld, BlockPos targetFocalPosition) {
        Random rand = targetWorld.getRandom();

        int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
        int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());
        int targetY = targetWorld.getHeight(Heightmap.Types.WORLD_SURFACE, targetX, targetZ);
        BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

        return safePosition.findBy(entityType, targetWorld, attemptPos);
    }

    private boolean isSafeSky(EntityType<?> entityType, ServerLevel targetWorld, BlockPos blockPos) {
        AABB skySpawnBoundingBox = entityType.getAABB(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5)
            .setMinY(blockPos.getY() - 16); // minus 16, to guarantee there's always (at least) 16 blocks of space below the player (so they don't spawn on the ground)
        return targetWorld.noCollision(skySpawnBoundingBox);
    }

    private BlockPos getDimensionScaledPosition(ResourceKey<Level> originDimension, ResourceKey<Level> targetDimension, BlockPos inputPos) {
        if (originDimension.equals(Level.OVERWORLD) && targetDimension.equals(Level.NETHER)) {
            return new BlockPos(inputPos.getX() / 8, inputPos.getY(), inputPos.getZ() / 8);
        } else if (originDimension.equals(Level.NETHER) && targetDimension.equals(Level.OVERWORLD)) {
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
