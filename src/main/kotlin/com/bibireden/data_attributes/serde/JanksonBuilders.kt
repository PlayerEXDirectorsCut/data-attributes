package com.bibireden.data_attributes.serde

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonObject
import blue.endless.jankson.JsonPrimitive
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.functions.AttributeFunctionConfig
import com.bibireden.data_attributes.config.entities.EntityTypeData
import com.bibireden.data_attributes.config.entities.EntityTypeEntry
import net.minecraft.util.Identifier
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object JanksonBuilders {
    fun applyEntityTypes(builder: Jankson.Builder) {
        builder.registerSerializer(EntityTypeData::class.java) { cfg, marshaller -> marshaller.serialize(cfg.data) }
        builder.registerDeserializer(JsonObject::class.java, EntityTypeData::class.java) { json, marshaller ->
            EntityTypeData(marshaller.marshall<Map<String, JsonObject>>(Map::class.java, json).entries
                .associate { (k, v) ->
                    Identifier.tryParse(k)!! to marshaller.marshallCarefully(EntityTypeEntry::class.java, v)
                }
            )
        }
    }

    fun applyFunctions(builder: Jankson.Builder) {
        builder.registerSerializer(AttributeFunctionConfig::class.java) { cfg, marshaller ->
            marshaller.serialize(cfg.data)
        }
        builder.registerDeserializer(JsonObject::class.java, AttributeFunctionConfig::class.java) { obj, marshaller ->
            AttributeFunctionConfig(marshaller.marshall<Map<String, Map<String, JsonObject>>>(Map::class.java, obj).entries
                .associate { (a, b) -> Identifier.tryParse(a)!! to b.entries
                    .associate { (k, v) -> Identifier.tryParse(k)!! to marshaller.marshallCarefully(AttributeFunction::class.java, v) }.toMutableMap()
                }.toMutableMap()
            )
        }
    }
}