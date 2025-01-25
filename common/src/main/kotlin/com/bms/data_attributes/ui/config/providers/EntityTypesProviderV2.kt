package com.bms.data_attributes.ui.config.providers

import com.bms.data_attributes.api.DataAttributesAPI
import com.bms.data_attributes.config.entities.EntityTypeData
import com.bms.data_attributes.ui.components.config.entities.EntityTypesHeaderComponent
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.resources.ResourceLocation

class EntityTypesProviderV2(val option: Option<Map<ResourceLocation, EntityTypeData>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    val backing = option.value().toMutableMap()

    init {
        for ((id, entry) in backing) {
            child(EntityTypesHeaderComponent(id, entry.data.toMutableMap(), this).id(id.toString()))
        }
        for ((id, entry) in DataAttributesAPI.serverManager.defaults.types.entries) {
            val component = childById(EntityTypesHeaderComponent::class.java, id.toString())
            if (component != null) {
                component.addEntityTypes(entry)
            }
            else {
                child(EntityTypesHeaderComponent(id, entry, this).id(id.toString()))
            }
        }
    }

    override fun isValid() = !this.option.detached()
    override fun parsedValue() = backing
}