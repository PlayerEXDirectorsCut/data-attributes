package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.config.DataAttributesConfigModel.AttributeOverrideConfig
import com.bibireden.data_attributes.data.*
import com.bibireden.data_attributes.data.merged.EntityTypes
import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.impl.MutableRegistryImpl
import io.wispforest.endec.CodecUtils
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class AttributeConfigManager(val data: AttributeData = AttributeData(), val handler: AttributeContainerHandler = AttributeContainerHandler()) {
    companion object Factory
    {
        fun getOrCreate(identifier: Identifier, attribute: EntityAttribute): EntityAttribute
        {
            return Registries.ATTRIBUTE[identifier] ?: MutableRegistryImpl.register(Registries.ATTRIBUTE, identifier, attribute)
        }
    }

    data class AttributeData(
        var overrides: Map<Identifier, AttributeOverrideConfig> = emptyMap(),
        var functions: Map<String, Map<String, AttributeFunction>> = emptyMap(),
        var entity_types: EntityTypes = EntityTypes(emptyMap())
    )
    {
        companion object {
            val ENDEC = StructEndecBuilder.of(
                Endec.map(Endecs.IDENTIFIER, AttributeOverrideConfig.ENDEC).fieldOf("overrides") { it.overrides },
                AttributeFunction.ENDEC.mapOf().mapOf().fieldOf("functions") { it.functions },
                EntityTypes.ENDEC.fieldOf("entity_types") { it.entity_types },
                ::AttributeData
            )
        }
    }


    /** Posts overrides and calls the [onDataUpdate] method for sync. */
    fun updateOverrides(data: Map<Identifier, AttributeOverrideConfig>)
    {
        this.data.overrides = data
        onDataUpdate()
    }

    /** Posts functions and calls the [onDataUpdate] method for sync. */
    fun updateFunctions(data: Map<String, Map<String, AttributeFunction>>)
    {
        this.data.functions = data
        onDataUpdate()
    }

    /** Whenever new [AttributeData] is applied. */
    fun onDataUpdate() {
        val lock = mutableMapOf<Identifier, EntityAttributeData>()
        val typesLock = mutableMapOf<Identifier, EntityTypeData>()

//        data.overrides.forEach { (k, v) -> lock[k] = EntityAttributeData(v) }

//        val key = Identifier("values")
//        val dat = lock.getOrDefault(key, EntityAttributeData())
//        dat.putFunctions(formatFunctions(data.functions))
//        lock[key] = dat
//
//        // entity types
//        data.entity_types.values.forEach { (key, value) ->
//            val id = Identifier(key)
//            val dat2 = EntityTypeData(value.map { (k, v) -> Identifier(k) to v }.toMap().toMutableMap())
//            typesLock[id] = dat2
//        }

//        return
//
//        MutableRegistryImpl.unregister(Registries.ATTRIBUTE)
//
//        for (id in Registries.ATTRIBUTE.ids) {
//            val attribute = Registries.ATTRIBUTE[id] ?: continue
//            (attribute as MutableEntityAttribute).`data_attributes$clear`()
//        }
//
//        for ((identifier, attributeData) in lock) {
//            attributeData.override(identifier, ::getOrCreate)
//        }
//
//        // todo: investigate if two are needed
//
//        for ((identifier, attributeData) in lock) {
//            attributeData.copy(Registries.ATTRIBUTE[identifier] ?: continue)
//        }
//
//        this.handler.buildContainers(typesLock, ENTITY_TYPE_INSTANCES)
    }
}