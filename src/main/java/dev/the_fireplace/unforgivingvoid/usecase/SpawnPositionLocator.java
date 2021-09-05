package dev.the_fireplace.unforgivingvoid.usecase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;
import java.util.Random;

public final class SpawnPositionLocator {

    private final ImmutableList<Vec3i> xzOffsets = ImmutableList.of(
        new Vec3i(0, 0, -1),
        new Vec3i(-1, 0, 0),
        new Vec3i(0, 0, 1),
        new Vec3i(1, 0, 0),
        new Vec3i(-1, 0, -1),
        new Vec3i(1, 0, -1),
        new Vec3i(-1, 0, 1),
        new Vec3i(1, 0, 1)
    );
    private final ImmutableList<Vec3i> spawnAreaOffsets = new ImmutableList.Builder<Vec3i>()
        .addAll(xzOffsets)
        .addAll(xzOffsets.stream().map(Vec3i::down).iterator())
        .addAll(xzOffsets.stream().map(v3i -> v3i.down(-1)).iterator())
        .add(new Vec3i(0, 1, 0))
        .build();

    private int horizontalOffsetRange = 128;
    private int maxScanIterations = 2048;

    public void setHorizontalOffsetRange(int horizontalOffsetRange) {
        this.horizontalOffsetRange = horizontalOffsetRange;
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
            if (iteration++ >= maxScanIterations) {
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
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getDimension().getType(), targetWorld.getDimension().getType(), currentPos);
        int iteration = 0;
        do {
            if (iteration++ >= maxScanIterations) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding surface position in {}, falling back to finding the spawn position instead.",
                    Registry.DIMENSION_TYPE.getId(targetWorld.getDimension().getType())
                );
                return findSpawnPosition(entityType, targetWorld);
            }
            int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
            int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());
            int targetY = targetWorld.getTopY(Heightmap.Type.WORLD_SURFACE, targetX, targetZ);
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            spawnVec = findSafePlatform(entityType, targetWorld, attemptPos);
        } while (!spawnVec.isPresent());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSkyPosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getDimension().getType(), targetWorld.getDimension().getType(), currentPos);
        int iteration = 0;
        do {
            int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
            int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());
            int targetY = targetWorld.getEffectiveHeight() - (int) Math.ceil(entityType.getHeight());
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            if (isSafeSky(entityType, targetWorld, attemptPos)) {
                return attemptPos;
            }
        } while (iteration++ < maxScanIterations);
        UnforgivingVoidConstants.getLogger().warn(
            "Max attempts exceeded for finding sky position in {}, falling back to finding the spawn position instead.",
            Registry.DIMENSION_TYPE.getId(targetWorld.getDimension().getType())
        );

        return findSpawnPosition(entityType, targetWorld);
    }

    public BlockPos findSpawnPosition(EntityType<?> entityType, ServerWorld targetWorld) {
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = targetWorld.getSpawnPos();
        Optional<Vec3d> spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        int iteration = 0;
        while (!spawnVec.isPresent()) {
            if (iteration++ >= maxScanIterations) {
                UnforgivingVoidConstants.getLogger().warn(
                    "Max attempts exceeded for finding spawn position in {}, falling back to the built in spawn position even though it may be unsafe.",
                    Registry.DIMENSION_TYPE.getId(targetWorld.getDimension().getType())
                );
                return targetFocalPosition;
            }

            int targetX = applyHorizontalOffset(rand, targetFocalPosition.getX());
            int targetZ = applyHorizontalOffset(rand, targetFocalPosition.getZ());
            int targetY = targetWorld.getTopY(Heightmap.Type.WORLD_SURFACE, targetX, targetZ);
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            spawnVec = findSafePlatform(entityType, targetWorld, attemptPos);
        }

        return new BlockPos(spawnVec.get());
    }

    private Optional<Vec3d> findSafePlatform(EntityType<?> entityType, ServerWorld targetWorld, BlockPos blockPos) {
        return findRespawnPosition(entityType, targetWorld, blockPos);
    }

    /**
     * Backported replacement for 1.16.5 method BlockRespawnAnchor.findRespawnPosition
     */
    private Optional<Vec3d> findRespawnPosition(EntityType<?> entity, ServerWorld world, BlockPos pos) {
        Optional<Vec3d> optional = findSafeNearbyPosition(entity, world, pos, true);
        return optional.isPresent() ? optional : findSafeNearbyPosition(entity, world, pos, false);
    }

    private Optional<Vec3d> findSafeNearbyPosition(EntityType<?> entityType, ServerWorld world, BlockPos blockPos, boolean bl) {
        BlockPos.Mutable mutableTargetPos = new BlockPos.Mutable();
        UnmodifiableIterator<Vec3i> offsetIterator = spawnAreaOffsets.iterator();

        Vec3d vec3d;
        do {
            if (!offsetIterator.hasNext()) {
                return Optional.empty();
            }

            Vec3i checkOffset = offsetIterator.next();
            mutableTargetPos.set(blockPos).add(checkOffset.getX(), checkOffset.getY(), checkOffset.getZ());
            vec3d = Dismounting.method_30769(entityType, world, mutableTargetPos, bl);
        } while (vec3d == null);

        return Optional.of(vec3d);
    }

    private boolean isSafeSky(EntityType<?> entityType, ServerWorld targetWorld, BlockPos blockPos) {
        return !isInvalidSpawn(entityType, targetWorld.getBlockState(blockPos));
    }

    /**
     * Backported replacement for 1.16.5 method EntityType#isInvalidSpawn
     */
    private boolean isInvalidSpawn(EntityType<?> entityType, BlockState state) {
        //This first if statement isn't quite equivalent to the one from 1.16+ EntityType, but should be good enough for sky spawning purposes
        if (state.getBlock() instanceof AirBlock) {
            return false;
        } else if (!entityType.isFireImmune() && (state.getBlock().equals(Blocks.FIRE) || state.getBlock().equals(Blocks.MAGMA_BLOCK) || isLitCampfire(state) || state.getBlock().equals(Blocks.LAVA))) {
            return true;
        } else {
            return state.getBlock().equals(Blocks.WITHER_ROSE) || state.getBlock().equals(Blocks.SWEET_BERRY_BUSH) || state.getBlock().equals(Blocks.CACTUS);
        }
    }

    /**
     * {@link CampfireBlock}'s isLitCampfire is private in this version, use an equivalent instead
     */
    private boolean isLitCampfire(BlockState state) {
        return state.getBlock() == Blocks.CAMPFIRE && state.get(CampfireBlock.LIT);
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
}
