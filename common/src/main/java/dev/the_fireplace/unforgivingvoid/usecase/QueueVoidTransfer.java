package dev.the_fireplace.unforgivingvoid.usecase;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.inject.Inject;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public final class QueueVoidTransfer
{
    private static final Set<UUID> playerLock = new ConcurrentSkipListSet<>(UUID::compareTo);

    private final VoidTransfer voidTransfer;

    @Inject
    public QueueVoidTransfer(VoidTransfer voidTransfer) {
        this.voidTransfer = voidTransfer;
    }

    public void queueTransfer(ServerPlayer serverPlayerEntity, MinecraftServer server) {
        UUID playerId = serverPlayerEntity.getUUID();
        if (!playerLock.add(playerId)) {
            return;
        }
        voidTransfer.initiateVoidTransfer(serverPlayerEntity, server);
        playerLock.remove(playerId);
    }
}
