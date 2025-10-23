package net.yiran.tmo;

import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.module.data.EffectData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ContextData {
    public Multimap<Attribute, AttributeModifier> attributes;
    public EffectData effects = new EffectData();

    public ContextData(Multimap<Attribute, AttributeModifier> attributes, EffectData effects) {
        this.attributes = attributes;
        this.effects = effects;
    }

    public static Map<String, ContextData> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var jsonObject = json.getAsJsonObject();
        Map<String, ContextData> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.asMap().entrySet()) {
            map.put(entry.getKey(), context.deserialize(entry.getValue(), ContextData.class));
        }
        return map;
    }
}
