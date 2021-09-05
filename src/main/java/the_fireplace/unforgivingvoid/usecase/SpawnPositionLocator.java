package the_fireplace.unforgivingvoid.usecase;

import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Random;

public final class SpawnPositionLocator {

    private int horizontalOffsetRange = 128;
    private int maxScanIterations = 512;

    public void setHorizontalOffsetRange(int horizontalOffsetRange) {
        this.horizontalOffsetRange = horizontalOffsetRange;
    }

    public BlockPos findSimilarPosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Optional<Vec3d> spawnVec;
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getRegistryKey(), targetWorld.getRegistryKey(), currentPos);
        int iteration = 0;
        do {
            int targetX = targetFocalPosition.getX() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetY = targetFocalPosition.getY() - 64 + rand.nextInt(128);//TODO smarter scan for this one, bound with dimension bounds
            int targetZ = targetFocalPosition.getZ() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            spawnVec = findSafePlatform(entityType, targetWorld, attemptPos);
            if (iteration++ >= maxScanIterations) {
                return findSpawnPosition(entityType, targetWorld);
            }
        } while (!spawnVec.isPresent());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSurfacePosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Optional<Vec3d> spawnVec;
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getRegistryKey(), targetWorld.getRegistryKey(), currentPos);
        int iteration = 0;
        do {
            if (iteration++ >= maxScanIterations) {
                return findSpawnPosition(entityType, targetWorld);
            }
            int targetX = targetFocalPosition.getX() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetZ = targetFocalPosition.getZ() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetY = targetWorld.getTopY(Heightmap.Type.WORLD_SURFACE, targetX, targetZ);
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            spawnVec = findSafePlatform(entityType, targetWorld, attemptPos);
        } while (!spawnVec.isPresent());

        return new BlockPos(spawnVec.get());
    }

    public BlockPos findSkyPosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getRegistryKey(), targetWorld.getRegistryKey(), currentPos);
        int iteration = 0;
        do {
            int targetX = targetFocalPosition.getX() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetZ = targetFocalPosition.getZ() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetY = targetWorld.getDimensionHeight() - (int) Math.ceil(entityType.getHeight());
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            if (isSafeSky(entityType, targetWorld, attemptPos)) {
                return attemptPos;
            }
        } while (iteration++ < maxScanIterations);

        return findSpawnPosition(entityType, targetWorld);
    }

    public BlockPos findSpawnPosition(EntityType<?> entityType, ServerWorld targetWorld) {
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = targetWorld.getSpawnPos();
        Optional<Vec3d> spawnVec = findSafePlatform(entityType, targetWorld, targetFocalPosition);
        int iteration = 0;
        while (!spawnVec.isPresent()) {
            if (iteration++ >= maxScanIterations) {
                return targetFocalPosition;
            }

            int targetX = targetFocalPosition.getX() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetZ = targetFocalPosition.getZ() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetY = targetWorld.getTopY(Heightmap.Type.WORLD_SURFACE, targetX, targetZ);
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            spawnVec = findSafePlatform(entityType, targetWorld, attemptPos);
        }

        return new BlockPos(spawnVec.get());
    }

    private Optional<Vec3d> findSafePlatform(EntityType<?> entityType, ServerWorld targetWorld, BlockPos blockPos) {
        return RespawnAnchorBlock.findRespawnPosition(entityType, targetWorld, blockPos);
    }

    private boolean isSafeSky(EntityType<?> entityType, ServerWorld targetWorld, BlockPos blockPos) {
        return !entityType.isInvalidSpawn(targetWorld.getBlockState(blockPos));
    }

    private BlockPos getDimensionScaledPosition(RegistryKey<World> originDimension, RegistryKey<World> targetDimension, BlockPos inputPos) {
        if (originDimension.equals(World.OVERWORLD) && targetDimension.equals(World.NETHER)) {
            return new BlockPos(inputPos.getX() / 8, inputPos.getY(), inputPos.getZ() / 8);
        } else if (originDimension.equals(World.NETHER) && targetDimension.equals(World.OVERWORLD)) {
            return new BlockPos(inputPos.getX() * 8, inputPos.getY(), inputPos.getZ() * 8);
        }

        return inputPos;
    }
}
