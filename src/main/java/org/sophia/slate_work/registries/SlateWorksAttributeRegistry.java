package org.sophia.slate_work.registries;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class SlateWorksAttributeRegistry {
    private static final Map<Identifier, EntityAttribute> ATTRIBUTES = new LinkedHashMap<>();

    public static void init(){
        for (var e : ATTRIBUTES.entrySet()){
            Registry.register(Registries.ATTRIBUTE, e.getKey(),e.getValue());
        }
    }

    private static final IXplatRegister<EntityAttribute> REGISTER = IXplatAbstractions.INSTANCE
            .createRegistar(RegistryKeys.ATTRIBUTE);

    public static final RegistryEntry<EntityAttribute> WHISPERING = make("whispering", new ClampedEntityAttribute(
            MOD_ID + ".attributes.whispering", 0, 0, 1).setTracked(true));

    private static RegistryEntry<EntityAttribute> make(String id, EntityAttribute attr) {
        var old = ATTRIBUTES.put(Identifier.of(MOD_ID, id), attr);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return REGISTER.registerHolder(id, () -> attr);
    }
}
