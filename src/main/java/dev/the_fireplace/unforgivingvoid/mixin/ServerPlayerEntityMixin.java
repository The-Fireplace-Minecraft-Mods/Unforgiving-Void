package dev.the_fireplace.unforgivingvoid.mixin;

import com.mojang.authlib.GameProfile;
import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfig;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfigManager;
import dev.the_fireplace.unforgivingvoid.usecase.QueueVoidTransfer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    protected ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(at = @At("TAIL"), method = "playerTick")
    private void tick(CallbackInfo callbackInfo) {
        DimensionConfig dimensionConfig = DIContainer.get().getInstance(DimensionConfigManager.class).getSettings(this.world.getRegistryKey().getValue());
        if (!this.world.isClient()
            && dimensionConfig.isEnabled()
            && this.getBlockPos().getY() <= getBottomY(world) - dimensionConfig.getTriggerDistance()
        ) {
            MinecraftServer server = getServer();
            if (server != null) {
                DIContainer.get().getInstance(QueueVoidTransfer.class).queueTransfer((ServerPlayerEntity) (Object) this, server);
            }
        }
    }

    private int getBottomY(World world) {
        return 0;
    }
}