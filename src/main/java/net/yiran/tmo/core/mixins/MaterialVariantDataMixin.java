package net.yiran.tmo.core.mixins;

import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(value = MaterialVariantData.class, remap = false)
public class MaterialVariantDataMixin extends VariantData {
    @Shadow
    public MaterialMultiplier extract;
    @Unique
    private List<String> contexts = List.of("default");

    @Inject(method = "combine", at = @At("HEAD"), cancellable = true)
    public void combine(MaterialData material, CallbackInfoReturnable<VariantData> cir) {
        cir.setReturnValue(combineWrap(material));
    }

    private VariantData combineWrap(MaterialData material) {
        UniqueVariantData result = new UniqueVariantData();

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

        result.aspects = AspectData.merge(aspects, material.aspects);

        if (material.rarity != null && (rarity == null || material.rarity.ordinal() > rarity.ordinal())) {
            result.rarity = material.rarity;
        } else {
            result.rarity = rarity;
        }

        result.glyph = Optional.ofNullable(extract.glyph)
                .map(glyph -> new GlyphData(glyph.textureLocation, glyph.textureX, glyph.textureY, material.tints.glyph))
                .orElse(glyph);

        List<String> availableTextures = Arrays.asList(extract.availableTextures);
        // note that map is run on one of the sub-streams
        result.models = Stream.concat(
                        Arrays.stream(models),
                        Arrays.stream(extract.models).map(model -> MaterialData.kneadModel(model, material, availableTextures)))
                .toArray(ModuleModel[]::new);

        if (tags == null) {
            result.tags = material.tags;
        } else if (material.tags == null) {
            result.tags = tags;
        } else {
            result.tags = Stream.concat(tags.stream(), material.tags.stream()).collect(Collectors.toSet());
        }

        return result;
    }
}
