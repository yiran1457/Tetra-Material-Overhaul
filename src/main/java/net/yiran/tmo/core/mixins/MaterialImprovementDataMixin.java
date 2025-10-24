package net.yiran.tmo.core.mixins;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.yiran.tmo.ContextData;
import net.yiran.tmo.core.IMaterialData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.*;
import java.util.stream.Stream;

@Mixin(value = MaterialImprovementData.class, remap = false)
public class MaterialImprovementDataMixin extends ImprovementData {
    @Shadow
    public MaterialMultiplier extract;
    @Unique
    private List<String> contexts = List.of("default");

    @Inject(method = "combine", at = @At("HEAD"), cancellable = true)
    private void combine(MaterialData material, CallbackInfoReturnable<ImprovementData> cir) {
        cir.setReturnValue(combineWrap(material));
    }

    private ImprovementData combineWrap(MaterialData material) {
        UniqueImprovementData result = new UniqueImprovementData();

        List<Multimap<Attribute, AttributeModifier>> attributeList = new ArrayList<>();
        attributeList.add(attributes);
        //attributeList.add(material.attributes);
        attributeList.add(AttributeHelper.multiplyModifiers(extract.primaryAttributes, material.primary));
        attributeList.add(AttributeHelper.multiplyModifiers(extract.secondaryAttributes, material.secondary));
        attributeList.add(AttributeHelper.multiplyModifiers(extract.tertiaryAttributes, material.tertiary));

        List<EffectData> effectList = new ArrayList<>();
        effectList.add(effects);
        //effectList.add(material.effects);
        effectList.add(EffectData.multiply(extract.primaryEffects, material.primary, material.primary));
        effectList.add(EffectData.multiply(extract.secondaryEffects, material.secondary, material.secondary));
        effectList.add(EffectData.multiply(extract.tertiaryEffects, material.tertiary, material.tertiary));

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

        result.key = key + material.key;
        result.level = level;
        result.group = group;
        result.enchantment = enchantment;
        result.aspects = AspectData.merge(aspects, material.aspects);

        if (material.category != null) {
            result.category = material.category;
        }

        result.attributes = AttributeHelper.collapseRound(AttributeHelper.merge(attributeList));

        result.durability = Math.round(durability + Optional.ofNullable(extract.durability)
                .map(extracted -> extracted * material.durability)
                .orElse(0f));

        result.durabilityMultiplier = durabilityMultiplier + Optional.ofNullable(extract.durabilityMultiplier)
                .map(extracted -> extracted * material.durability)
                .orElse(0f);

        result.integrity = integrity + Optional.ofNullable(extract.integrity)
                .map(extracted -> extracted * (extracted > 0 ? material.integrityGain : material.integrityCost))
                .map(Math::round)
                .orElse(0);

        result.magicCapacity = Math.round(magicCapacity + Optional.ofNullable(extract.magicCapacity)
                .map(extracted -> extracted * material.magicCapacity)
                .orElse(0f));

        result.effects = EffectData.merge(effectList);

        result.tools = ToolData.merge(Arrays.asList(
                tools,
                ToolData.multiply(extract.tools, material.toolLevel, material.toolEfficiency)
        ));

        result.glyph = Optional.ofNullable(extract.glyph)
                .map(glyph -> new GlyphData(glyph.textureLocation, glyph.textureX, glyph.textureY, material.tints.glyph))
                .orElse(glyph);

        List<String> availableTextures = Arrays.asList(extract.availableTextures);
        // note that map is run on one of the sub-streams
        result.models = Stream.concat(
                        Arrays.stream(models),
                        Arrays.stream(extract.models).map(model -> MaterialData.kneadModel(model, material, availableTextures)))
                .toArray(ModuleModel[]::new);

        return result;
    }

}
