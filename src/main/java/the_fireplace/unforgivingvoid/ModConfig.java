package the_fireplace.unforgivingvoid;

import com.google.common.collect.Lists;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;

import java.util.List;

@Config(name = UnforgivingVoid.MODID)
public class ModConfig implements ConfigData {
    public int triggerAtY = -32;
    public int damageOnImpact = 19;
    public boolean preventImpactDeath = true;
    public boolean dropObsidian = true;
    public boolean lavaPlatform = false;
    public int horizontalDistanceOffset = 512;
    public List<String> dimensionFilter = Lists.newArrayList("*");
}
