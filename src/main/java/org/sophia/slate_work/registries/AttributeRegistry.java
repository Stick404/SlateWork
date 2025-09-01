package org.sophia.slate_work.registries;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class AttributeRegistry {
    private static final Map<Identifier, EntityAttribute> ATTRIBUTES = new LinkedHashMap<>();

    public static void init(){
        for (var e : ATTRIBUTES.entrySet()){
            Registry.register(Registries.ATTRIBUTE, e.getKey(),e.getValue());
        }
    }

    public static final EntityAttribute WHISPERING = make("whispering", new ClampedEntityAttribute(
            MOD_ID + ".attributes.whispering", 0, 0, 1).setTracked(true));

    private static <T extends EntityAttribute> T make(String id, T attr) {
        var old = ATTRIBUTES.put(new Identifier(MOD_ID, id), attr);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return attr;
    }
}
