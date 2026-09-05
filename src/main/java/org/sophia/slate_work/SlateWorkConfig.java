package org.sophia.slate_work;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import static org.sophia.slate_work.Slate_work.MOD_ID;

@Config(name = MOD_ID)
public class SlateWorkConfig implements ConfigData {
        @Comment("A half joke feature where the Replicated Allay becomes edible and gives 1 point of food and saturation. Changes only on restart")
    public boolean imAGummyBear = false;
}
