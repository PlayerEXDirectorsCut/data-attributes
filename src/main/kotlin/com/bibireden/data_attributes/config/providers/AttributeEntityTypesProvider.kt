package com.bibireden.data_attributes.config.providers

import com.bibireden.data_attributes.config.DataAttributesConfigProviders
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.attributeIdentifierToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.entityTypeIdentifierToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.data.EntityTypeData
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeEntityTypesProvider(val option: Option<Map<Identifier, EntityTypeData>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    val backing = HashMap(option.value())

    init {
        backing.forEach { (topID, types) ->
            CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), entityTypeIdentifierToText(topID), true).also { ct ->
                ct.gap(15)
                types.data.forEach { id,  value ->
                    Containers.collapsible(Sizing.content(), Sizing.content(), attributeIdentifierToText(id), true).also {
                        it.gap(8)

                        it.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.entity_types.value"),
                            value,
                            DataAttributesConfigProviders::isNumeric,
                            onChange = {
                                it.toDoubleOrNull()?.let { value ->
                                    val data = this.backing.remove(topID)?: return@let
                                    val mapping = data.data
                                    mapping[id] = value
                                    this.backing.put(topID, data.copy(data = mapping))
                                }
                            }
                        ))

                        val attribute = Registries.ATTRIBUTE[id]
                        if (attribute is ClampedEntityAttribute) {
                            it.tooltip(
                                Text.translatable(
                                    "text.config.data_attributes.data_entry.entity_type_value",
                                    id,
                                    attribute.minValue,
                                    attribute.maxValue
                                )
                            )
                        }

                        ct.child(it)
                    }
                }
            }
                .also(this::child)
        }
    }

    override fun isValid() = !this.option.detached()

    override fun parsedValue() = backing
}
