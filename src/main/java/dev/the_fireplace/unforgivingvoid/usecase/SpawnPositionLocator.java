package dev.the_fireplace.unforgivingvoid.usecase;


import dev.the_fireplace.lib.api.teleport.injectables.SafePosition;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.dimension.DimensionType;

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
        BlockPos targetFocalPosition = getDimensionScaledPosition(
            currentWorld.getDimension().getType(),
            targetWorld.getDimension().getType(),
            currentPos
        );
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to similar position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding similar position in {}, falling back to finding the spawn position instead.",
                    Registry.DIMENSION_TYPE.getId(targetWorld.getDimension().getType())
                );
                return findSpawnPosition(entityType, targetWorld);
            }
            int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
            int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());
            int targetY = rand.nextInt(targetWorld.getEffectiveHeight() - 20) + 10;
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            spawnVec = findSafePlatform(entityType, targetWorld, attemptPos);
        } while (!spawnVec.isPresent());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSurfacePosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Optional<Vec3d> spawnVec;
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getDimension().getType(), targetWorld.getDimension().getType(), currentPos);
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to surface position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding surface position in {}, falling back to finding the spawn position instead.",
                    Registry.DIMENSION_TYPE.getId(targetWorld.getDimension().getType())
                );
                return findSpawnPosition(entityType, targetWorld);
            }

            spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        } while (!spawnVec.isPresent());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSkyPosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getDimension().getType(), targetWorld.getDimension().getType(), currentPos);
        int iteration = 0;
        do {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to sky position, iteration {}", iteration);

            int spawnHeight = targetWorld.getChunkManager().getChunkGenerator().getSpawnHeight();

            int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
            int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());
            int targetY = rand.nextInt(targetWorld.getEffectiveHeight() - spawnHeight) + spawnHeight - 6 /* minus 6 because bedrock ceiling */;

            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            if (isSafeSky(entityType, targetWorld, attemptPos)) {
                return attemptPos;
            }
        } while (iteration++ < MAX_SCAN_ITERATIONS);

        UnforgivingVoidConstants.getLogger().warn(
            "Max attempts exceeded for finding sky position in {}, falling back to finding the spawn position instead.",
            Registry.DIMENSION_TYPE.getId(targetWorld.getDimension().getType())
        );

        return findSpawnPosition(entityType, targetWorld);
    }

    public BlockPos findSpawnPosition(EntityType<?> entityType, ServerWorld targetWorld) {
        BlockPos targetFocalPosition = targetWorld.getSpawnPos();
        Optional<Vec3d> spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        int iteration = 0;
        while (!spawnVec.isPresent()) {
            UnforgivingVoidConstants.getLogger().debug("Attempting to teleport to spawn position, iteration {}", iteration);

            if (iteration++ >= MAX_SCAN_ITERATIONS) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding spawn position in {}, falling back to the built in spawn position even though it may be unsafe.",
                    Registry.DIMENSION_TYPE.getId(targetWorld.getDimension().getType())
                );
                return targetFocalPosition;
            }

            spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        }

        return new BlockPos(spawnVec.get());
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
        Box skySpawnBoundingBox = entityType.createSimpleBoundingBox(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);

        // minus 16, to guarantee there's always (at least) 16 blocks of space below the player (so they don't spawn on the ground)
        skySpawnBoundingBox = new Box(skySpawnBoundingBox.x1, skySpawnBoundingBox.y1 - 16, skySpawnBoundingBox.z1, skySpawnBoundingBox.x2, skySpawnBoundingBox.y2, skySpawnBoundingBox.z2);

        return targetWorld.doesNotCollide(skySpawnBoundingBox);
    }

    private BlockPos getDimensionScaledPosition(DimensionType originDimension, DimensionType targetDimension, BlockPos inputPos) {
        if (originDimension.equals(DimensionType.OVERWORLD) && targetDimension.equals(DimensionType.THE_NETHER)) {
            return new BlockPos(inputPos.getX() / 8, inputPos.getY(), inputPos.getZ() / 8);
        } else if (originDimension.equals(DimensionType.THE_NETHER) && targetDimension.equals(DimensionType.OVERWORLD)) {
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
