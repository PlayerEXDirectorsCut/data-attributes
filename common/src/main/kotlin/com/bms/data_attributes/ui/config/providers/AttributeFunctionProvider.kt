package com.bms.data_attributes.ui.config.providers

import com.bms.data_attributes.api.DataAttributesAPI
import com.bms.data_attributes.config.functions.AttributeFunction
import com.bms.data_attributes.config.functions.AttributeFunctionConfig
import com.bms.data_attributes.ui.components.config.function.AttributeFunctionHeaderComponent
import com.bms.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class AttributeFunctionProvider(val option: Option<AttributeFunctionConfig>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    val backing = option.value().data

    init {
        child(
            Components.button(Component.translatable("text.config.data_attributes.buttons.add")) {
                val id = ResourceLocation.tryBuild("unknown", "resource")!!
                val entry = mutableMapOf<ResourceLocation, AttributeFunction>()
                backing[id] = entry
                child(1, AttributeFunctionHeaderComponent(id, entry,this))
            }
                .renderer(ButtonRenderers.STANDARD)
                .horizontalSizing(Sizing.content())
                .verticalSizing(Sizing.fixed(20))
        )

        for ((id, entry) in backing) {
            child(AttributeFunctionHeaderComponent(id, entry, this).id(id.toString()))
        }
        for ((id, entry) in DataAttributesAPI.serverManager.defaults.functions.entries) {
            val component = childById(AttributeFunctionHeaderComponent::class.java, id.toString())
            if (component != null) {
                component.addFunctions(entry)
            }
            else {
                child(AttributeFunctionHeaderComponent(id, entry, this).id(id.toString()))
            }
        }
    }

    override fun isValid() = !this.option.detached()
    override fun parsedValue() = AttributeFunctionConfig(backing)
}