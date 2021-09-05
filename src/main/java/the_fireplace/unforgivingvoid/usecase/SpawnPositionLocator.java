package the_fireplace.unforgivingvoid.usecase;

import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Random;

public final class SpawnPositionLocator {

    private boolean avoidSkySpawning = true;
    private int horizontalOffsetRange = 128;
    private int preferredYCoordinate = 64;
    private int maxScanIterations = 64;
    private boolean fallbackToSpawn = true;

    public void setAvoidSkySpawning(boolean allowSkySpawning) {
        this.avoidSkySpawning = allowSkySpawning;
    }

    public void setHorizontalOffsetRange(int horizontalOffsetRange) {
        this.horizontalOffsetRange = horizontalOffsetRange;
    }

    public void setPreferredYCoordinate(int yCoordinate) {
        this.preferredYCoordinate = yCoordinate;
    }

    public BlockPos findSpawnPosition(EntityType<?> entityType, ServerWorld currentWorld, ServerWorld targetWorld, BlockPos currentPos) {
        Optional<Vec3d> spawnVec;
        Random rand = targetWorld.getRandom();
        BlockPos targetFocalPosition = getDimensionScaledPosition(currentWorld.getRegistryKey(), targetWorld.getRegistryKey(), currentPos);
        do {
            int targetX = targetFocalPosition.getX() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetY = preferredYCoordinate - 5 + rand.nextInt(10);//TODO option to find top block instead
            int targetZ = targetFocalPosition.getZ() - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            BlockPos attemptPos = new BlockPos(targetX, targetY, targetZ);

            spawnVec = findSafePlatform(entityType, targetWorld, attemptPos);
        } while (!spawnVec.isPresent());

        return new BlockPos(spawnVec.get());
    }

    private Optional<Vec3d> findSafePlatform(EntityType<?> entityType, ServerWorld targetWorld, BlockPos blockPos) {
        return RespawnAnchorBlock.findRespawnPosition(entityType, targetWorld, blockPos);
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
