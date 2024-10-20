package com.bibireden.data_attributes.ui.components.config

import com.bibireden.data_attributes.api.attribute.AttributeFormat
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeOverrideComponent(var identifier: Identifier, private var override: AttributeOverride, private val backing: MutableMap<Identifier, AttributeOverride>, horizontalSizing: Sizing, verticalSizing: Sizing, title: Text, expanded: Boolean) : CollapsibleContainer(horizontalSizing, verticalSizing, title, expanded) {
    private var attribute: MutableEntityAttribute? = null

    private fun isRegistered() = attribute !== null

    private fun isDefault() = !backing.containsKey(identifier)

    private fun replaceEntry(id: Identifier, override: AttributeOverride) {
        this.backing[id] = override
    }

    private fun updateOverride(override: AttributeOverride) {
        val attribute = attribute
        if (attribute !== null) {
            // extract min & max fallbacks from internals
            this.override = override.copy(
                min_fallback = attribute.`data_attributes$min_fallback`(),
                max_fallback = attribute.`data_attributes$max_fallback`()
            )
        }

        this.titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, Registries.ATTRIBUTE, { it.translationKey }, isDefault()))

        if (!isRegistered()) {
            titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
        }
        else if (isDefault()) {
            titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
        }
    }

    init {
        updateOverride(override)

        child(
            Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
            .also { content ->
                content.verticalAlignment(VerticalAlignment.BOTTOM)
                content.gap(10)

                content.child(ConfigToggleButton().also { button ->
                    button.enabled(override.enabled)
                    button.onPress { replaceEntry(identifier, override.copy(enabled = !override.enabled)) }
                    button.renderer(ButtonRenderers.STANDARD)
                })

                if (!isDefault()) {
                    content.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.reset"))
                    {
                        override = override.copy(
                            min = attribute?.`data_attributes$min_fallback`() ?: override.min_fallback,
                            max = attribute?.`data_attributes$max_fallback`() ?: override.max_fallback,
                            smoothness = 1.0,
                            formula = StackingFormula.Flat,
                            format = AttributeFormat.Whole
                        )
                        this.backing[identifier] = override
                        updateOverride(override)
                    }
                        .renderer(ButtonRenderers.STANDARD)
                    )

                    content.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                        if (this.childById(FlowLayout::class.java, "edit-field") == null) {
                            val field = FieldComponents.identifier(
                                { newId, _ ->
                                    val attribute = Registries.ATTRIBUTE[newId] as? MutableEntityAttribute ?: return@identifier

                                    if (backing.containsKey(newId)) return@identifier

                                    this.backing.remove(identifier)
                                    this.backing[newId] = override.copy(
                                        min = attribute.`data_attributes$min_fallback`(),
                                        max = attribute.`data_attributes$max_fallback`(),
                                        smoothness = 1.0,
                                        formula = StackingFormula.Flat,
                                        format = AttributeFormat.Whole
                                    )

                                    this.attribute = attribute
                                },
                                autocomplete = Registries.ATTRIBUTE.ids
                            )

                            field.textBox.predicate = { it !in backing && Registries.ATTRIBUTE.containsId(it) }

                            this.child(0, field)
                        }
                    }
                        .renderer(ButtonRenderers.STANDARD)
                    )

                    content.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.remove"))
                    {
                        this.backing.remove(identifier)
                    }
                        .renderer(ButtonRenderers.STANDARD)
                    )
                }
            }
        )

        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Text.translatable(identifier.toTranslationKey()).toString() }))
    }
}