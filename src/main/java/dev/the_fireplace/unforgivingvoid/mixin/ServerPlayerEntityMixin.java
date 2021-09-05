package dev.the_fireplace.unforgivingvoid.mixin;

import com.mojang.authlib.GameProfile;
import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfig;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfigManager;
import dev.the_fireplace.unforgivingvoid.usecase.QueueVoidTransfer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("TAIL"), method = "playerTick")
    private void tick(CallbackInfo callbackInfo) {
        DimensionConfig dimensionConfig = DIContainer.get().getInstance(DimensionConfigManager.class).getSettings(Registry.DIMENSION_TYPE.getId(world.getDimension().getType()));
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
