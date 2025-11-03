package net.yiran.tmo.core.mixins;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.yiran.tmo.ContextData;
import net.yiran.tmo.core.IMaterialData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import se.mickelus.tetra.module.data.MaterialData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Mixin(MaterialData.class)
public class MaterialDataMixin implements IMaterialData {
    private Map<String, ContextData> contextData;

    @Override
    public void setContextData(Map<String, ContextData> contextData) {
        this.contextData = contextData;
    }

    @Override
    public Map<String, ContextData> getContextData() {
        return contextData;
    }

    @Inject(method = "copyFields", at = @At("RETURN"),remap = false)
    private static void copyFields(MaterialData from, MaterialData to, CallbackInfo ci) {
        if (((IMaterialData) from).getContextData() != null) {
            if (((IMaterialData) to).getContextData() != null) {
                ((IMaterialData) to).getContextData().putAll(((IMaterialData) from).getContextData());
            }
        }
    }

    @Mixin(MaterialData.Deserializer.class)
    public abstract static class DeserializerMixin implements JsonDeserializer<MaterialData> {
        @Inject(
                method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lse/mickelus/tetra/module/data/MaterialData;",
                at = @At(
                        value = "RETURN"
                ),
                locals = LocalCapture.CAPTURE_FAILHARD,
                remap = false
        )
        private void deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context, CallbackInfoReturnable<MaterialData> cir, JsonObject jsonObject, MaterialData data) {
            if (json.getAsJsonObject().has("contexts")) {
                Map<String, ContextData> contexts = context.deserialize(json.getAsJsonObject().get("contexts"), ContextData.TYPE);
                if (!contexts.containsKey("default")) {
                    var defaultContext = new ContextData();
                    defaultContext.attributes=data.attributes;
                    defaultContext.effects=data.effects;
                    contexts.put("default", defaultContext);
                }
                ((IMaterialData) data).setContextData(contexts);
            } else {
                Map<String, ContextData> contextData = new HashMap<>();
                var defaultContext = new ContextData();
                defaultContext.attributes=data.attributes;
                defaultContext.effects=data.effects;
                contextData.put("default", defaultContext);
                ((IMaterialData) data).setContextData(contextData);
            }
        }
    }
}
