package dev.the_fireplace.unforgivingvoid.usecase;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.inject.Inject;
import java.util.Set;

public final class QueueVoidTransfer {

    private static final Set<ServerPlayerEntity> playerLock = new ConcurrentSet<>();

    private final VoidTransfer voidTransfer;

    @Inject
    public QueueVoidTransfer(VoidTransfer voidTransfer) {
        this.voidTransfer = voidTransfer;
    }

    public void queueTransfer(ServerPlayerEntity serverPlayerEntity, MinecraftServer server) {
        if (playerLock.contains(serverPlayerEntity)) {
            return;
        }
        playerLock.add(serverPlayerEntity);
        voidTransfer.initiateVoidTransfer(serverPlayerEntity, server);
        playerLock.remove(serverPlayerEntity);
    }
}
