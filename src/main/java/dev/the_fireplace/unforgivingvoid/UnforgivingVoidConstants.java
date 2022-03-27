package dev.the_fireplace.unforgivingvoid;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public final class UnforgivingVoidConstants
{
    public static final String MODID = "unforgivingvoid";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    @Nullable
    private static MinecraftServer server = null;

    public static Logger getLogger() {
        return LOGGER;
    }

    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }

    public static void setServer(@Nullable MinecraftServer server) {
        UnforgivingVoidConstants.server = server;
    }
}
