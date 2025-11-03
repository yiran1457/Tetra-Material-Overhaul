package net.yiran.tmo;

import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.module.data.EffectData;

import java.lang.reflect.Type;
import java.util.Map;

public class ContextData {
    public static Type TYPE = new TypeToken<Map<String, ContextData>>() {}.getType();
    public Multimap<Attribute, AttributeModifier> attributes;
    public EffectData effects = new EffectData();
}
