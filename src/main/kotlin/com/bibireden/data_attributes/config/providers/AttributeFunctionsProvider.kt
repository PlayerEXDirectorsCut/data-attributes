package com.bibireden.data_attributes.config.providers

import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.attributeIdentifierToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.isAttributeUnregistered
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.config.Validators
import com.bibireden.data_attributes.config.functions.AttributeFunctionConfig
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.text.Text

class AttributeFunctionsProvider(val option: Option<AttributeFunctionConfig>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    val backing = option.value().data.toMutableMap()

    init {
        backing.forEach { (topID, functions) ->
            val isFunctionParentUnregistered = isAttributeUnregistered(topID)
            CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), attributeIdentifierToText(topID), true).also { ct ->
                ct.gap(15)
                if (isFunctionParentUnregistered) {
                    ct.titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
                }
                functions.forEachIndexed { index,  function ->
                    val isFunctionChildUnregistered = isAttributeUnregistered(function.id)

                    Containers.collapsible(Sizing.content(), Sizing.content(), attributeIdentifierToText(function.id), true).also {
                        it.gap(8)
                        if (isFunctionChildUnregistered) {
                            it.titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
                        }
                        else {
                            val attribute = Registries.ATTRIBUTE[function.id]
                            if (attribute is ClampedEntityAttribute) {
                                it.tooltip(
                                    Text.translatable(
                                        "text.config.data_attributes.data_entry.function_child",
                                        function.id,
                                        attribute.minValue,
                                        attribute.maxValue
                                    )
                                )
                            }
                        }
                        it.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.functions.value"),
                            function.value,
                            Validators::isNumeric,
                            onChange = {
                                it.toDoubleOrNull()?.let { v ->
                                    val popped = this.backing.remove(topID)?.toMutableList() ?: return@textBoxComponent
                                    popped[index] = function.copy(value = v)
                                    this.backing.put(topID, popped)
                                }
                            }
                        ))

                        it.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
                            hf.verticalAlignment(VerticalAlignment.CENTER)
                            hf.child(
                                Components.label(Text.translatable("text.config.data_attributes.data_entry.functions.behavior"))
                                    .sizing(Sizing.content(), Sizing.fixed(20))
                            )
                            hf.child(
                                Components.button(Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}"), {
                                    function.behavior = when (function.behavior) {
                                        StackingBehavior.Add -> StackingBehavior.Multiply
                                        StackingBehavior.Multiply -> StackingBehavior.Add
                                    }
                                    it.message = Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")
                                    val popped = this.backing.remove(topID)?.toMutableList() ?: return@button
                                    popped[index] = function.copy(behavior = function.behavior)
                                    this.backing.put(topID, popped)
                                })
                                    .renderer(ButtonRenderers.STANDARD)
                                    .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
                            )
                        })
                        ct.child(it)
                    }
                }
            }
                .also(this::child)
        }
    }

    override fun isValid() = !this.option.detached()
    override fun parsedValue() = AttributeFunctionConfig(backing)
}
