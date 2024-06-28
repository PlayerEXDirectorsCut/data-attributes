package com.bibireden.data_attributes.config.models

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.data.EntityTypeData
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Hook
import io.wispforest.owo.config.annotation.SectionHeader
import io.wispforest.owo.config.annotation.Sync
import net.minecraft.util.Identifier

@Suppress("UNUSED")
@Config(name = "${DataAttributes.MOD_ID}/entity_types", wrapperName = "DataAttributesEntityTypesConfig")
@Sync(SyncMode.NONE)
class EntityTypesConfigModel {
    @SectionHeader("entity_types")

    @JvmField
    @Hook
    var entity_types: Map<Identifier, EntityTypeData> = mapOf()
}