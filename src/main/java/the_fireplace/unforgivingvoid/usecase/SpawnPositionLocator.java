package the_fireplace.unforgivingvoid.usecase;

import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.Random;

public final class SpawnPositionLocator {

    private boolean avoidSkySpawning = true;
    private int horizontalOffsetRange = 128;
    private int preferredYCoordinate = 64;

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
        do {
            int targetX = currentPos.getX() / 8 - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            int targetY = rand.nextInt(100) + 16;
            int targetZ = currentPos.getZ() / 8 - horizontalOffsetRange + rand.nextInt(horizontalOffsetRange * 2);
            spawnVec = RespawnAnchorBlock.findRespawnPosition(entityType, targetWorld, new BlockPos(targetX, targetY, targetZ));
        } while (!spawnVec.isPresent());

        return new BlockPos(spawnVec.get());
    }
}
