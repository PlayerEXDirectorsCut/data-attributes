package com.bibireden.data_attributes.config.providers

import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.attributeIdentifierToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.isAttributeUnregistered
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.config.Validators
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import com.bibireden.data_attributes.ext.round
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeOverrideProvider(val option: Option<Map<Identifier, AttributeOverride>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    val backing = option.value().toMutableMap()

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

                    topContainer.child(
                        textBoxComponent(
                        Text.translatable("text.config.data_attributes.data_entry.overrides.min"),
                        override.min,
                        Validators::isNumeric,
                        onChange = {
                            it.toDoubleOrNull()?.let { v ->
                                this.backing.replace(id, override.copy(min = v))
                            }
                        },
                        "inputs.min"
                    )
                    )

                    topContainer.child(
                        textBoxComponent(
                        Text.translatable("text.config.data_attributes.data_entry.overrides.max"),
                        override.max,
                        Validators::isNumeric,
                        onChange = {
                            it.toDoubleOrNull()?.let { v ->
                                this.backing.replace(id, override.copy(max = v))
                            }
                        },
                        "inputs.max"
                    )
                    )

                    topContainer.child(
                        textBoxComponent(
                        Text.translatable("text.config.data_attributes.data_entry.overrides.min_fallback"),
                        override.min_fallback,
                        Validators::isNumeric,
                    )
                    )

                    topContainer.child(
                        textBoxComponent(
                        Text.translatable("text.config.data_attributes.data_entry.overrides.max_fallback"),
                        override.max_fallback,
                        Validators::isNumeric,
                    )
                    )

                    topContainer.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30)).also { hf ->
                        hf.verticalAlignment(VerticalAlignment.CENTER)
                        hf.gap(8)
                        hf.child(
                            Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.smoothness"))
                            .sizing(Sizing.content(), Sizing.fixed(20)))

                        hf.child(
                            Components.discreteSlider(Sizing.fill(30), 0.01, 100.0).value(override.smoothness).also { slider ->
                                slider.onChanged().subscribe {
                                    var smoothness = slider.value().round(2)
                                    if (smoothness <= 0.0) {smoothness = 0.01}
                                    this.backing.remove(id)
                                    this.backing.put(id, override.copy(smoothness = smoothness))
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

    override fun isValid() = !this.option.detached()

    override fun parsedValue() = backing
}