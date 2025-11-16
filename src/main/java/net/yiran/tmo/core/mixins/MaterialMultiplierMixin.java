package net.yiran.tmo.core.mixins;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.yiran.tmo.core.IMaterialMultiplier;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.MaterialMultiplier;

@Mixin(MaterialMultiplier.class)
public class MaterialMultiplierMixin implements IMaterialMultiplier {
    public EffectData magicEffects;
    public Multimap<Attribute, AttributeModifier> magicAttributes;

    @Override
    public Multimap<Attribute, AttributeModifier> getMagicAttributes() {
        return magicAttributes;
    }

    @Override
    public EffectData getMagicEffects() {
        return magicEffects;
    }
}
