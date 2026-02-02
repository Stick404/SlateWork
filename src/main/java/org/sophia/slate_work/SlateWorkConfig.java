package org.sophia.slate_work;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import static org.sophia.slate_work.Slate_work.MOD_ID;

@Config(name = MOD_ID)
public class SlateWorkConfig implements ConfigData {
    @Comment("If set to true, Slate Works tries to aggressively optimize Spell Circle Looping, fixing a memory leak. This may break running Spell Circles, and may crash with addons that mixin to Spell Circles. Here be dragons!")
    public boolean aggressiveLooperOptimizations = true;
    @Comment("If set to true, Slate Works tries to aggressively optimize Spell Circle NBT sizes. This will break running Spell Circles, and may crash with addons that mixin to Spell Circles. Here be dragons!")
    public boolean aggressiveNBTOptimizations = false;
    @Comment("If set to true, Slate Works tries to aggressively optimize Spell Circle path scanning, may crash with other addons that mixin to Spell Circles. Here be dragons!")
    public boolean aggressiveScanningOptimizations = false;
}
