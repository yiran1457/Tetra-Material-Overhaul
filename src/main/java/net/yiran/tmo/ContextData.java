package net.yiran.tmo;

import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.yiran.tmo.core.IMaterialData;
import net.yiran.tmo.core.IMaterialMultiplier;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.data.MaterialMultiplier;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetra.properties.AttributeHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContextData {
    public static Type TYPE = new TypeToken<Map<String, ContextData>>() {
    }.getType();
    public Multimap<Attribute, AttributeModifier> attributes;
    public EffectData effects = new EffectData();

    public static ContextData buildDefaultContextData(MaterialData data) {
        var defaultContext = new ContextData();
        defaultContext.attributes = data.attributes;
        defaultContext.effects = data.effects;
        return defaultContext;
    }

    public static void combineVariantData(VariantData THIS, VariantData result, List<String> contexts, MaterialMultiplier extract, MaterialData material) {
        List<Multimap<Attribute, AttributeModifier>> attributeList = new ArrayList<>();
        attributeList.add(THIS.attributes);
        attributeList.add(AttributeHelper.multiplyModifiers(extract.primaryAttributes, material.primary));
        attributeList.add(AttributeHelper.multiplyModifiers(extract.secondaryAttributes, material.secondary));
        attributeList.add(AttributeHelper.multiplyModifiers(extract.tertiaryAttributes, material.tertiary));
        attributeList.add(AttributeHelper.multiplyModifiers(((IMaterialMultiplier) extract).getMagicAttributes(), material.magicCapacity));

        List<EffectData> effectList = new ArrayList<>();
        effectList.add(THIS.effects);
        effectList.add(EffectData.multiply(extract.primaryEffects, material.primary, material.primary));
        effectList.add(EffectData.multiply(extract.secondaryEffects, material.secondary, material.secondary));
        effectList.add(EffectData.multiply(extract.tertiaryEffects, material.tertiary, material.tertiary));
        effectList.add(EffectData.multiply(((IMaterialMultiplier) extract).getMagicEffects(), material.magicCapacity, material.magicCapacity));

        if (((IMaterialData) material).getContextData() != null) {
            Map<String, ContextData> contextMap = ((IMaterialData) material).getContextData();
            for (String context : contexts) {
                if (contextMap.containsKey(context)) {
                    ContextData contextData = contextMap.get(context);
                    attributeList.add(contextData.attributes);
                    effectList.add(contextData.effects);
                }
            }
        }
        result.attributes = AttributeHelper.collapseRound(AttributeHelper.merge(attributeList));
        result.effects = EffectData.merge(effectList);

    }
}
