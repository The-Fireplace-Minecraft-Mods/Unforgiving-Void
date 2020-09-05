package the_fireplace.unforgivingvoid.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import the_fireplace.unforgivingvoid.UnforgivingVoid;

import java.util.Optional;
import java.util.Random;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(at = @At(value="TAIL"), method = "playerTick")
    private void tick(CallbackInfo callbackInfo) {
        if(!this.world.isClient() && this.getBlockPos().getY() <= UnforgivingVoid.config.triggerAtY) {
            MinecraftServer server = getServer();
            if(server != null) {
                boolean doTeleport = UnforgivingVoid.config.dimensionFilter.contains("*");
                for (String dim : UnforgivingVoid.config.dimensionFilter)
                    if (!dim.equals("*") && world.getRegistryKey().getValue().toString().toLowerCase().equals(dim.toLowerCase())) {
                        doTeleport = !doTeleport;
                        break;
                    }
                if (doTeleport) {
                    addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 3));
                    Random rand = world.random;
                    ServerWorld nether = server.getWorld(World.NETHER);
                    assert nether != null;
                    Optional<Vec3d> spawnVec;
                    do {
                        spawnVec = RespawnAnchorBlock.findRespawnPosition(this.getType(), nether, new BlockPos(getBlockPos().getX() / 8 - UnforgivingVoid.config.horizontalDistanceOffset + rand.nextInt(UnforgivingVoid.config.horizontalDistanceOffset * 2), rand.nextInt(100) + 16, getBlockPos().getZ() / 8 - UnforgivingVoid.config.horizontalDistanceOffset + rand.nextInt(UnforgivingVoid.config.horizontalDistanceOffset * 2)));
                    } while(!spawnVec.isPresent());
                    BlockPos spawnPos = new BlockPos(spawnVec.get());
                    //Make sure the chunk is created BEFORE teleporting the player, or else we end up with the infinite derp loop
                    nether.getChunk(spawnPos);
                    if(UnforgivingVoid.config.fireResistanceSeconds > 0)
                        addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, UnforgivingVoid.config.fireResistanceSeconds*20));
                    ((ServerPlayerEntity)(Object)this).teleport(nether, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), yaw, pitch);
                    if(UnforgivingVoid.config.dropObsidian)
                        nether.spawnEntity(new ItemEntity(nether, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), new ItemStack(Blocks.OBSIDIAN, 14)));
                }
            }
        }
    }
}
