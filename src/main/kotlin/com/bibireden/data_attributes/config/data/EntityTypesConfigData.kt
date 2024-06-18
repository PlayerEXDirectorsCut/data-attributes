package com.bibireden.data_attributes.config.data

import com.bibireden.data_attributes.data.EntityTypeData
import net.minecraft.util.Identifier

data class EntityTypesConfigData(val data: Map<Identifier, EntityTypeData> = mapOf())