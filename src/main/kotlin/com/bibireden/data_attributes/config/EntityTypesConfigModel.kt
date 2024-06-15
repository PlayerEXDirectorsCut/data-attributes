package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.data.merged.EntityTypes
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Hook
import io.wispforest.owo.config.annotation.Sync

@Suppress("UNUSED")
@Config(name = "${DataAttributes.MOD_ID}/entity_types", wrapperName = "DataAttributesEntityTypesConfig")
@Sync(SyncMode.NONE)
class EntityTypesConfigModel {
//    @JvmField
//    @Hook
//    val entityTypes: EntityTypes = EntityTypes(mapOf())
}