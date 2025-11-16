package net.yiran.tmo.core;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.module.data.EffectData;

public interface IMaterialMultiplier {
    Multimap<Attribute, AttributeModifier> getMagicAttributes();

    EffectData getMagicEffects();
}
