package dev.the_fireplace.unforgivingvoid.mixin;

import com.google.inject.Injector;
import com.mojang.authlib.GameProfile;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfig;
import dev.the_fireplace.unforgivingvoid.config.DimensionConfigManager;
import dev.the_fireplace.unforgivingvoid.usecase.QueueVoidTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AbstractClassNeverImplemented")
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player
{
    @Shadow
    private boolean isChangingDimension;

    protected ServerPlayerMixin(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(at = @At("TAIL"), method = "doTick")
    private void tick(CallbackInfo callbackInfo) {
        Injector injector = UnforgivingVoidConstants.getInjector();
        DimensionConfig dimensionConfig = injector.getInstance(DimensionConfigManager.class).getSettings(this.level.dimension().location());
        if (!this.level.isClientSide()
            && dimensionConfig.isEnabled()
            && this.blockPosition().getY() <= getBottomY(level) - dimensionConfig.getTriggerDistance()
            && !isChangingDimension()
        ) {
            MinecraftServer server = getServer();
            if (server != null) {
                UnforgivingVoidConstants.getLogger().debug(
                    "Player is below the minimum height. Teleporting to new dimension. Current position is {}, and current world is {}",
                    blockPosition().toShortString(),
                    getServerLevel().dimension().location()
                );

                isChangingDimension = true;

                injector.getInstance(QueueVoidTransfer.class).queueTransfer((ServerPlayer) (Object) this, server);
            }
        }
    }

    private int getBottomY(Level world) {
        return 0;
    }

    @Invoker("getLevel")
    public abstract ServerLevel getServerLevel();

    @Shadow
    public abstract boolean isChangingDimension();
}
