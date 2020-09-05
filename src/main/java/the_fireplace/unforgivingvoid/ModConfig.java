package the_fireplace.unforgivingvoid;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

import java.util.List;

@Config(name = UnforgivingVoid.MODID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public int triggerAtY = -32;
    @ConfigEntry.Gui.Tooltip
    public boolean dropObsidian = false;
    @ConfigEntry.Gui.Tooltip(count=2)
    public int fireResistanceSeconds = 180;
    @ConfigEntry.Gui.Tooltip
    public int horizontalDistanceOffset = 128;
    @ConfigEntry.Gui.Tooltip(count=2)
    public List<String> dimensionFilter = Lists.newArrayList("*");

    @Override
    public void validatePostLoad() {
        if(fireResistanceSeconds < 0)
            fireResistanceSeconds = 0;
        if(horizontalDistanceOffset < 0)
            horizontalDistanceOffset = -horizontalDistanceOffset;
        //Convert to set then list to deduplicate
        dimensionFilter = Lists.newArrayList(Sets.newHashSet(dimensionFilter));
    }
}
