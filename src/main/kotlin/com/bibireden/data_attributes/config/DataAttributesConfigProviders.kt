package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.config.OverridesConfigModel.AttributeOverrideConfig
import com.bibireden.data_attributes.data.AttributeFunctionConfigData
import com.bibireden.data_attributes.data.EntityTypeData
import com.bibireden.data_attributes.utils.round
import com.google.common.base.Predicate
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.OptionComponentFactory
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

@Suppress("UnstableApiUsage")
object DataAttributesConfigProviders {
    fun isAttributeUnregistered(id: Identifier) = !Registries.ATTRIBUTE.containsId(id)
    fun isNumeric(str: String) = str.isEmpty() || str.matches("-?\\d+(\\.\\d+)?".toRegex())
    fun maybeInvalidText(text: String, invalid: Boolean) = Text.literal(text).apply { formatted(if (invalid) Formatting.RED else Formatting.WHITE) }

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
            backing.forEach { (id, override) ->
                val isOverrideInvalid = isAttributeUnregistered(id)
                Containers.collapsible(Sizing.content(), Sizing.content(), maybeInvalidText("$id", isOverrideInvalid), true)
                    .also {
                        if (isOverrideInvalid) it.tooltip(Text.translatable("text.config.data_attributes.data_entry.overrides.invalid"))
                        it.gap(15)
                        it.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
                            hf.verticalAlignment(VerticalAlignment.CENTER)
                            hf.gap(6)
                            hf.child(Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.enabled"))
                                .sizing(Sizing.content(), Sizing.fixed(20)))

                            hf.child(ConfigToggleButton().also { b ->
                                b.enabled(override.enabled).positioning(Positioning.relative(100, 0))
                                b.onPress {
                                    val enabled = !override.enabled
                                    this.backing.remove(id)
                                    this.backing.put(id, override.copy(enabled = enabled))
                                }
                            })
                        })

                        it.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.overrides.min"),
                            override.min,
                            ::isNumeric,
                            onChange = {
                                this.backing.remove(id)
                                this.backing.put(id, override.copy(min = it.toDouble()))
                            }
                        ))

                        it.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.overrides.max"),
                            override.max,
                            ::isNumeric,
                            onChange = {
                                this.backing.remove(id)
                                this.backing.put(id, override.copy(max = it.toDouble()))
                            }
                        ))

                        it.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.overrides.min_fallback"),
                            override.min_fallback,
                            ::isNumeric
                        ))

                        it.child(textBoxComponent(
                            Text.translatable("text.config.data_attributes.data_entry.overrides.max_fallback"),
                            override.max_fallback,
                            ::isNumeric
                        ))

                        it.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30)).also { hf ->
                            hf.verticalAlignment(VerticalAlignment.CENTER)
                            hf.gap(6)
                            hf.child(Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.smoothness"))
                                .sizing(Sizing.content(), Sizing.fixed(20)))

                            hf.child(
                                Components.discreteSlider(Sizing.fill(25), 0.01, 100.0).value(override.smoothness).also { slider ->
                                    slider.onChanged().subscribe {
                                        this.backing.remove(id)
                                        this.backing.put(id, override.copy(smoothness = slider.value().round(2)))
                                    } //THIS IS CURSED, WHY

                                }
                                    .positioning(Positioning.relative(100, 0))
                            )
                        })

                        it.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
                            hf.verticalAlignment(VerticalAlignment.CENTER)
                            hf.gap(6)
                            hf.child(
                                Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.formula"))
                                    .sizing(Sizing.content(), Sizing.fixed(20))
                            )
                            hf.child(
                                Components.button(Text.literal(override.formula.name), {
                                    override.formula = when (override.formula) {
                                        StackingFormula.Flat -> StackingFormula.Diminished
                                        StackingFormula.Diminished -> StackingFormula.Flat
                                    }
                                    it.message = Text.translatable("text.config.data_attributes.enum.stackingFormula.${override.formula.name.lowercase()}")
                                    this.backing.remove(id)
                                    this.backing.put(id, override.copy(formula = override.formula))
                                }).positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
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
                Containers.collapsible(Sizing.content(), Sizing.content(), maybeInvalidText("$topID", isFunctionParentUnregistered), true).also { ct ->
                    ct.gap(15)
                    if (isFunctionParentUnregistered) {
                        ct.tooltip(Text.translatable("text.config.data_attributes.data_entry.function_parent.invalid"))
                    }
                    functions.forEachIndexed { index,  function ->
                        val isFunctionChildUnregistered = isAttributeUnregistered(function.id)
                        Containers.collapsible(Sizing.content(), Sizing.content(), maybeInvalidText("${function.id}", isFunctionChildUnregistered), true).also {
                            it.gap(10)
                            if (isFunctionChildUnregistered) {
                                it.tooltip(Text.translatable("text.config.data_attributes.data_entry.function_child.invalid"))
                            }
                            it.child(textBoxComponent(
                                Text.translatable("text.config.data_attributes.data_entry.functions.value"),
                                function.value,
                                ::isNumeric,
                                onChange = {
                                    val popped = this.backing.remove(topID)?.toMutableList() ?: return@textBoxComponent
                                    popped[index] = function.copy(value = it.toDouble())
                                    this.backing.put(topID, popped)
                                }
                            ))

                            it.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
                                hf.verticalAlignment(VerticalAlignment.CENTER)
                                hf.gap(6)
                                hf.child(
                                    Components.label(Text.translatable("text.config.data_attributes.data_entry.functions.behavior"))
                                        .sizing(Sizing.content(), Sizing.fixed(20))
                                )
                                hf.child(
                                    Components.button(Text.literal(function.behavior.name), {
                                        function.behavior = when (function.behavior) {
                                            StackingBehavior.Add -> StackingBehavior.Multiply
                                            StackingBehavior.Multiply -> StackingBehavior.Add
                                        }
                                        it.message = Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")
                                        val popped = this.backing.remove(topID)?.toMutableList() ?: return@button
                                        popped[index] = function.copy(behavior = function.behavior)
                                        this.backing.put(topID, popped)
                                    }).positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
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
        val backing = option.value().toMutableMap()

        init {
            backing.forEach { (topID, types) ->
                Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal("$topID"), true).also { ct ->
                    ct.gap(15)
                    types.data.forEach { id,  value ->
                        Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal("[$id]"), true).also {
                            it.gap(10)

                            it.child(textBoxComponent(
                                Text.translatable("text.config.data_attributes.data_entry.entity_types.value"),
                                value,
                                ::isNumeric,
                                onChange = {
                                    val popped = this.backing.remove(topID)?.data ?: return@textBoxComponent
                                    popped[id] = it.toDouble()
                                    this.backing.put(topID, EntityTypeData(popped))
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

    fun textBoxComponent(txt: Text, obj: Any, predicate: Predicate<String>? = null, onChange: ((String) -> Unit)? = null): FlowLayout {
        val isUnchangeable = onChange != null
        return Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
            hf.verticalAlignment(VerticalAlignment.CENTER)
            hf.gap(6)
            hf.child(Components.label(txt).sizing(Sizing.content(), Sizing.fixed(20)))
            hf.child(
                Components.textBox(Sizing.fill(30)).also { tb ->
                    tb.text = obj.toString()
                    tb.verticalSizing(Sizing.fixed(15))
                    tb.setPlaceholder(Text.literal(obj.toString()))
                    tb.setUneditableColor(0xBBBBBB)
                    tb.setEditableColor(0x91D8AF)
                    tb.setEditable(isUnchangeable)
                    if (isUnchangeable) {
                        tb.tooltip(Text.translatable("text.config.data_attributes.data_entry.unchangeable"))
                    }
                    if (onChange != null) {
                        tb.setTextPredicate {
                            if (predicate == null || predicate.apply(it)) {
                                onChange.invoke(it)
                            }
                            true
                        }
                    }
                }.positioning(Positioning.relative(100, 50))
            )
        }
    }
}