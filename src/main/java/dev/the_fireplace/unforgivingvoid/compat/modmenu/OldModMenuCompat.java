package dev.the_fireplace.unforgivingvoid.compat.modmenu;

import com.terraformersmc.modmenu.gui.ModsScreen;
import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.unforgivingvoid.UnforgivingVoidConstants;
import dev.the_fireplace.unforgivingvoid.config.UVConfigScreenFactory;
import dev.the_fireplace.unforgivingvoid.mixin.clothconfig.AbstractConfigScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * Counteract Mod Menu's old caching mechanism (MM 2.0.2 and earlier), which shouldn't be used with Cloth Config GUIs and causes problems for Hierarchical Configs
 * See also: https://github.com/TerraformersMC/ModMenu/issues/254
 */
@Environment(EnvType.CLIENT)
public final class OldModMenuCompat implements ModMenuCompat {
    @Override
    public void forceReloadConfigGui() {
        UVConfigScreenFactory configScreenFactory = DIContainer.get().getInstance(UVConfigScreenFactory.class);
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof AbstractConfigScreenAccessor) {
            Screen parent = ((AbstractConfigScreenAccessor) screen).getParent();
            if (parent instanceof ModsScreen) {
                ((ModsScreen) parent).getConfigScreenCache().put(UnforgivingVoidConstants.MODID, configScreenFactory.getConfigScreen(parent));
            }
        }
    }
}
