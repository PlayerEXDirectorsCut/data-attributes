@file:UseSerializers(ResourceLocationSerializer::class)

package com.bms.data_attributes.config

import com.bms.data_attributes.config.entities.EntityTypeEntry
import com.bms.data_attributes.config.functions.AttributeFunction
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bms.data_attributes.serde.ResourceLocationSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.resources.ResourceLocation

@Serializable
data class Cache(val overrides: Overrides = Overrides(), val functions: Functions = Functions(), val types: EntityTypes = EntityTypes()) {
    @Serializable
    data class Overrides(var entries: LinkedHashMap<ResourceLocation, AttributeOverride> = LinkedHashMap())
    @Serializable
    data class Functions(var entries: LinkedHashMap<ResourceLocation, LinkedHashMap<ResourceLocation, AttributeFunction>> = LinkedHashMap())
    @Serializable
    data class EntityTypes(var entries: LinkedHashMap<ResourceLocation, LinkedHashMap<ResourceLocation, EntityTypeEntry>> = LinkedHashMap())
}