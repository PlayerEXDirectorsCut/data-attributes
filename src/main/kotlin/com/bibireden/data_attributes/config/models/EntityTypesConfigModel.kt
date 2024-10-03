package com.bibireden.data_attributes.config.models

import blue.endless.jankson.Comment
import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.entities.EntityTypeData
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Hook
import io.wispforest.owo.config.annotation.SectionHeader
import io.wispforest.owo.config.annotation.Sync
import net.minecraft.util.Identifier

@Suppress("UNUSED")
@Config(name = "${DataAttributes.MOD_ID}/entity_types", wrapperName = "EntityTypesConfig")
@Sync(SyncMode.NONE)
class EntityTypesConfigModel {
    @SectionHeader("entity_types")

    @JvmField
    @Hook
    @Comment("entity types are able to target specific entities in-game to attach certain attributes to them.")
    var entries: Map<Identifier, EntityTypeData> = mapOf()
}