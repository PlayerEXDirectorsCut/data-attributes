package com.bibireden.data_attributes.ui.config.providers

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.AttributeFormat
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.attributeIdToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.config.Validators
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.ext.round
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import kotlin.math.max

class AttributeOverrideProviderV2(val option: Option<Map<Identifier, AttributeOverride>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    private val backing = option.value().toMutableMap()

    private fun replaceEntry(id: Identifier, override: AttributeOverride) {
        val noEntry = id !in backing
        this.backing[id] = override
        if (noEntry) refreshAndDisplayAttributes()
    }

    /** Construct single override entry that discerns defaults from config-based ones. */
    private fun createOverrideEntry(id: Identifier, override: AttributeOverride, isDefault: Boolean) {
        val attribute = Registries.ATTRIBUTE[id] as? MutableEntityAttribute
        val isRegistered = attribute != null

        var override = override

        if (attribute != null) {
            // extract min & max fallbacks from internals
            override = override.copy(
                min_fallback = attribute.`data_attributes$min_fallback`(),
                max_fallback = attribute.`data_attributes$max_fallback`()
            )
        }

        Containers.collapsible(Sizing.content(), Sizing.content(), attributeIdToText(id, isDefault), true).also { container ->
            if (!isRegistered) {
                container.titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
            }

            container.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                .also { content ->
                    content.verticalAlignment(VerticalAlignment.BOTTOM)
                    content.gap(10)

                    content.child(ConfigToggleButton().also { button ->
                        button.enabled(override.enabled)
                        button.onPress { backing[id] = override.copy(enabled = !override.enabled) }
                        button.renderer(ButtonRenderers.STANDARD)
                    })

                    content.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.reset"))
                        {
                            this.backing[id] = override.copy(
                                min = attribute?.`data_attributes$min_fallback`() ?: override.min_fallback,
                                max = attribute?.`data_attributes$max_fallback`() ?: override.max_fallback,
                                smoothness = 1.0,
                                formula = StackingFormula.Flat,
                                format = AttributeFormat.Whole
                            )
                            refreshAndDisplayAttributes()
                        }
                        .renderer(ButtonRenderers.STANDARD)
                    )
                }
            )

            container.child(textBoxComponent(
                Text.translatable("text.config.data_attributes.data_entry.overrides.min"),
                override.min,
                Validators::isNumeric,
                onChange = {
                    it.toDoubleOrNull()?.let { v ->
                        this.backing[id] = override.copy(min = v)
                    }
                },
                "inputs.min"
            ))

            container.child(
                textBoxComponent(
                    Text.translatable("text.config.data_attributes.data_entry.overrides.max"),
                    override.max,
                    Validators::isNumeric,
                    onChange = {
                        it.toDoubleOrNull()?.let { v ->
                            this.backing[id] = override.copy(max = v)
                        }
                    },
                    "inputs.max"
                )
            )

            container.child(
                textBoxComponent(
                    Text.translatable("text.config.data_attributes.data_entry.overrides.min_fallback"),
                    override.min_fallback,
                    Validators::isNumeric,
                )
            )

            container.child(
                textBoxComponent(
                    Text.translatable("text.config.data_attributes.data_entry.overrides.max_fallback"),
                    override.max_fallback,
                    Validators::isNumeric,
                )
            )

            container.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30)).also { hf ->
                hf.verticalAlignment(VerticalAlignment.CENTER)
                hf.gap(8)
                hf.child(
                    Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.smoothness"))
                        .sizing(Sizing.content(), Sizing.fixed(20)))

                hf.child(
                    Components.discreteSlider(Sizing.fill(30), 0.01, 100.0).value(override.smoothness).also { slider ->
                        slider.onChanged().subscribe {
                            replaceEntry(id, override.copy(smoothness = max(slider.value().round(2), 0.01)))
                        }
                    }
                        .positioning(Positioning.relative(100, 0))
                )
            })

            container.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
                hf.verticalAlignment(VerticalAlignment.CENTER)
                hf.gap(8)
                hf.child(
                    Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.formula"))
                        .sizing(Sizing.content(), Sizing.fixed(20))
                )
                hf.child(
                    Components.button(Text.translatable("text.config.data_attributes.enum.stackingFormula.${override.formula.name.lowercase()}")) {
                        override.formula = when (override.formula) {
                            StackingFormula.Flat -> StackingFormula.Diminished
                            StackingFormula.Diminished -> StackingFormula.Flat
                        }
                        it.message = Text.translatable("text.config.data_attributes.enum.stackingFormula.${override.formula.name.lowercase()}")
                        replaceEntry(id, override.copy(formula = override.formula))
                    }
                        .renderer(ButtonRenderers.STANDARD)
                        .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
                )
            })

            container.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
                hf.verticalAlignment(VerticalAlignment.CENTER)
                hf.gap(8)
                hf.child(
                    Components.label(Text.translatable("text.config.data_attributes.data_entry.overrides.format"))
                        .sizing(Sizing.content(), Sizing.fixed(20))
                )
                hf.child(
                    Components.button(Text.translatable("text.config.data_attributes.enum.format.${override.format.name.lowercase()}")) {
                        override.format = when (override.format) {
                            AttributeFormat.Percentage -> AttributeFormat.Whole
                            AttributeFormat.Whole -> AttributeFormat.Percentage
                        }
                        it.message = Text.translatable("text.config.data_attributes.enum.format.${override.format.name.lowercase()}")
                        replaceEntry(id, override.copy(formula = override.formula))
                    }
                        .renderer(ButtonRenderers.STANDARD)
                        .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
                )
            })
        }.also(::child)
    }

    init {
        refreshAndDisplayAttributes()
    }

    fun refreshAndDisplayAttributes() {
        for (child in children) {
            if (child is CollapsibleContainer) child.remove()
        }

        val manager = DataAttributesAPI.serverManager

        // config first
        for ((id, override) in backing) { // DataAttributes.OVERRIDES_CONFIG.overrides
            createOverrideEntry(id, override, false)
        }

        // defaults
        for ((id, override) in manager.defaults.overrides.entries) {
            if (id !in backing) createOverrideEntry(id, override, true)
        }
    }

    override fun isValid() = !this.option.detached()

    override fun parsedValue() = backing
}