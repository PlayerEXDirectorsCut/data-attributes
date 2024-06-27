package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.config.OverridesConfigModel.AttributeOverrideConfig
import com.bibireden.data_attributes.data.AttributeFunctionConfigData
import com.bibireden.data_attributes.data.EntityTypeData
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.ui.colors.ColorCodes
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import com.bibireden.data_attributes.utils.round
import com.google.common.base.Predicate
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.OptionComponentFactory
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Suppress("UnstableApiUsage")
object DataAttributesConfigProviders {
    fun entityTypeIdentifierToText(id: Identifier): MutableText {
        val type = Registries.ENTITY_TYPE[id]
        return Text.empty().apply {
            append(Text.translatable(type.translationKey).append(" ").setStyle(Style.EMPTY.withColor(ColorCodes.BEE_YELLOW)))
            append(Text.literal("($id)").setStyle(Style.EMPTY.withColor(ColorCodes.BEE_BLACK)))
        }
    }
    fun attributeIdentifierToText(id: Identifier): MutableText {
        val attribute = Registries.ATTRIBUTE[id]
        return Text.empty().apply {
            if (attribute != null) {
                append(Text.translatable(attribute.translationKey).append(" ")).setStyle(Style.EMPTY.withColor(0xE7C14B))
            }
            append(Text.literal("($id)").also { t ->
                t.setStyle(Style.EMPTY.withColor(if (attribute != null) ColorCodes.BEE_BLACK else ColorCodes.UNEDITABLE))
            })
        }
    }
    fun isAttributeUnregistered(id: Identifier) = !Registries.ATTRIBUTE.containsId(id)
    fun isNumeric(str: String) = str.isEmpty() || str.matches("-?\\d+(\\.\\d+)?".toRegex())

    val ATTRIBUTE_OVERRIDE_FACTORY = OptionComponentFactory { model, option ->
        val provider = AttributeOverrideProvider(option)
        return@OptionComponentFactory OptionComponentFactory.Result(provider, provider)
    }

    val ATTRIBUTE_FUNCTIONS_FACTORY = OptionComponentFactory { model, option ->
        val provider = AttributeFunctionsProvider(option)
        return@OptionComponentFactory OptionComponentFactory.Result(provider, provider)
    }

    val ENTITY_TYPES_FACTORY = OptionComponentFactory { model, option ->
        val provider = EntityTypesProvider(option)
        return@OptionComponentFactory OptionComponentFactory.Result(provider, provider)
    }

    private class AttributeOverrideProvider(option: Option<Map<Identifier, AttributeOverrideConfig>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
        val backing = HashMap(option.value())

        init {
            backing.forEach { (id, config) ->
                var override = config
                // extract min & max fallbacks.
                val attribute = Registries.ATTRIBUTE[id] as MutableEntityAttribute?
                if (attribute != null) {
                    override = override.copy(
                        min_fallback = attribute.`data_attributes$min_fallback`(),
                        max_fallback = attribute.`data_attributes$max_fallback`()
                    )
                    this.backing.replace(id, override)
                }

                val isOverrideInvalid = isAttributeUnregistered(id)
                Containers.collapsible(Sizing.content(), Sizing.content(), attributeIdentifierToText(id), true)
                    .also { topContainer ->
                        if (isOverrideInvalid) {
                            topContainer.tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
                        }

                        topContainer.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15)).also { hf ->
                            hf.verticalAlignment(VerticalAlignment.BOTTOM)

                            hf.gap(10)

                            hf.child(ConfigToggleButton().apply {
                                enabled(override.enabled)
                                onPress { backing.replace(id, override.copy(enabled = !override.enabled)) }
                                renderer(ButtonRenderers.STANDARD)
                            })

//                            hf.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.reset"))
//                                {
//                                    override = override.copy(min = override.min_fallback, max = override.max_fallback)
//
//                                    val inputMinBox = this.childById(TextBoxComponent::class.java, "inputs.min")!!
//                                    inputMinBox.text = override.min.toString()
//
//                                    val inputMaxBox = this.childById(TextBoxComponent::class.java, "inputs.max")!!
//                                    inputMaxBox.text = override.max.toString()
//
//                                    this.backing.replace(id, override)
//                                }
//                                .renderer(ButtonRenderers.STANDARD)
//                            )
                        })

                        topContainer.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.overrides.min"),
                            override.min,
                            ::isNumeric,
                            onChange = {
                                it.toDoubleOrNull()?.let { v ->
                                    this.backing.replace(id, override.copy(min = v))
                                }
                            },
                            "inputs.min"
                        ))

                        topContainer.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.overrides.max"),
                            override.max,
                            ::isNumeric,
                            onChange = {
                                it.toDoubleOrNull()?.let { v ->
                                    this.backing.replace(id, override.copy(max = v))
                                }
                            },
                            "inputs.max"
                        ))

                        topContainer.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.overrides.min_fallback"),
                            override.min_fallback,
                            ::isNumeric
                        ))

                        topContainer.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.overrides.max_fallback"),
                            override.max_fallback,
                            ::isNumeric
                        ))

                        topContainer.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30)).also { hf ->
                            hf.verticalAlignment(VerticalAlignment.CENTER)
                            hf.gap(8)
                            hf.child(Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.smoothness"))
                                .sizing(Sizing.content(), Sizing.fixed(20)))

                            hf.child(
                                Components.discreteSlider(Sizing.fill(25), 0.01, 100.0).value(override.smoothness).also { slider ->
                                    slider.onChanged().subscribe {
                                        this.backing.remove(id)
                                        this.backing.put(id, override.copy(smoothness = slider.value().round(2)))
                                    }
                                }
                                    .positioning(Positioning.relative(100, 0))
                            )
                        })

                        topContainer.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
                            hf.verticalAlignment(VerticalAlignment.CENTER)
                            hf.gap(8)
                            hf.child(
                                Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.formula"))
                                    .sizing(Sizing.content(), Sizing.fixed(20))
                            )
                            hf.child(
                                Components.button(Text.translatable("text.config.data_attributes.enum.stackingFormula.${override.formula.name.lowercase()}"), {
                                    override.formula = when (override.formula) {
                                        StackingFormula.Flat -> StackingFormula.Diminished
                                        StackingFormula.Diminished -> StackingFormula.Flat
                                    }
                                    it.message = Text.translatable("text.config.data_attributes.enum.stackingFormula.${override.formula.name.lowercase()}")
                                    this.backing.replace(id, override.copy(formula = override.formula))
                                })
                                    .renderer(ButtonRenderers.STANDARD)
                                    .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
                            )
                        })
                    }
                    .also(this::child)
            }
        }

        override fun isValid() = true

        override fun parsedValue() = backing
    }

    private class AttributeFunctionsProvider(option: Option<AttributeFunctionConfigData>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
        val backing = option.value().data.toMutableMap()

        init {
            backing.forEach { (topID, functions) ->
                val isFunctionParentUnregistered = isAttributeUnregistered(topID)
                CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), attributeIdentifierToText(topID), true).also { ct ->
                    ct.gap(15)
                    if (isFunctionParentUnregistered) {
                        ct.tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
                    }
                    functions.forEachIndexed { index,  function ->
                        val isFunctionChildUnregistered = isAttributeUnregistered(function.id)

                        Containers.collapsible(Sizing.content(), Sizing.content(), attributeIdentifierToText(function.id), true).also {
                            it.gap(8)
                            if (isFunctionChildUnregistered) {
                                it.tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
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
                                ::isNumeric,
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

        override fun isValid() = true
        override fun parsedValue() = AttributeFunctionConfigData(backing)
    }

    private class EntityTypesProvider(option: Option<Map<Identifier, EntityTypeData>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
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
                                ::isNumeric,
                                onChange = {
                                    it.toDoubleOrNull()?.let { value ->
                                        val data = this.backing.remove(topID)?: return@let
                                        val mapping = data.data
                                        mapping[id] = value
                                        this.backing.put(topID, data.copy(data = mapping))
                                    }
                                }
                            ))

                            ct.child(it)
                        }
                    }
                }
                    .also(this::child)
            }
        }

        override fun isValid() = true

        override fun parsedValue() = backing
    }

    fun textBoxComponent(txt: Text, obj: Any, predicate: Predicate<String>? = null, onChange: ((String) -> Unit)? = null, textBoxID: String? = null): FlowLayout {
        val isUnchangeable = onChange == null
        return Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
            hf.verticalAlignment(VerticalAlignment.CENTER)
            hf.gap(6)
            hf.child(Components.label(txt).sizing(Sizing.content(), Sizing.fixed(20)))
            hf.child(
                Components.textBox(Sizing.fill(30)).also { tb ->
                    tb.verticalSizing(Sizing.fixed(16))
                    tb.setEditableColor(ColorCodes.BEE_YELLOW)
                    tb.setUneditableColor(ColorCodes.BEE_BLACK)
                    tb.setEditable(!isUnchangeable)
                    if (isUnchangeable) {
                        tb.active = false
                        tb.cursorStyle(CursorStyle.POINTER)
                        tb.setSuggestion(obj.toString())
                        tb.tooltip(Text.translatable("text.config.data_attributes.data_entry.unchangeable"))
                    }
                    else {
                        tb.setPlaceholder(Text.literal(obj.toString()))
                        tb.text = obj.toString()
                    }
                    if (onChange != null) {
                        tb.onChanged().subscribe { v ->
                            if (predicate == null || predicate.apply(v)) {
                                onChange.invoke(v)
                            }
                        }
                    }
                }.positioning(Positioning.relative(100, 50)).id(textBoxID)
            )
        }
    }
}