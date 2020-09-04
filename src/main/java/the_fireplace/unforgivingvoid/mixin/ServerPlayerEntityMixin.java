package the_fireplace.unforgivingvoid.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import the_fireplace.unforgivingvoid.UnforgivingVoid;

import java.util.Random;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow
    public abstract Entity moveToWorld(ServerWorld destination);

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(at = @At(value="TAIL"), method = "tick")
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
                    //Random rand = world.random;
                    //BlockPos spawnPos = new BlockPos(getBlockPos().getX() / 8 - UnforgivingVoid.config.horizontalDistanceOffset + rand.nextInt(UnforgivingVoid.config.horizontalDistanceOffset * 2), rand.nextInt(100) + 16, getBlockPos().getZ() / 8 - UnforgivingVoid.config.horizontalDistanceOffset + rand.nextInt(UnforgivingVoid.config.horizontalDistanceOffset * 2));
                    moveToWorld(server.getWorld(World.NETHER));
                    //teleport(server.getWorld(World.NETHER), spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), yaw, pitch);
                }
            }
        }
    }
}
