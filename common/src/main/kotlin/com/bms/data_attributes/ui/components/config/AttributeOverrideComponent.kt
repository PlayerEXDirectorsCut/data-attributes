package com.bms.data_attributes.ui.components.config

import com.bms.data_attributes.DataAttributesClient
import com.bms.data_attributes.api.DataAttributesAPI
import com.bms.data_attributes.api.attribute.AttributeFormat
import com.bms.data_attributes.api.attribute.StackingFormula
import com.bms.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bms.data_attributes.ext.round
import com.bms.data_attributes.mutable.MutableAttribute
import com.bms.data_attributes.ui.components.entries.DataEntryComponent
import com.bms.data_attributes.ui.components.entries.EntryComponents
import com.bms.data_attributes.ui.components.fields.EditFieldComponent
import com.bms.data_attributes.ui.components.fields.FieldComponents
import com.bms.data_attributes.ui.config.providers.AttributeOverrideProvider
import com.bms.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import kotlin.math.max

class AttributeOverrideComponent(
    var identifier: ResourceLocation,
    private var override: AttributeOverride,
    private val backing: MutableMap<ResourceLocation, AttributeOverride>,
    private val provider: AttributeOverrideProvider
) : CollapsibleContainer(Sizing.content(), Sizing.content(), Component.literal("<n/a>"), DataAttributesClient.UI_STATE.collapsible.overrides[identifier.toString()] ?: true) {
    private val attribute: MutableAttribute?
        get() = BuiltInRegistries.ATTRIBUTE[identifier] as? MutableAttribute

    private fun isRegistered() = attribute != null

    private fun isDefault() = !backing.containsKey(identifier)

    private val dataEntryMin: DataEntryComponent<Double>?
    private val dataEntryMax: DataEntryComponent<Double>?
    private val dataEntryMinFallback: DataEntryComponent<Double>?
    private val dataEntryMaxFallback: DataEntryComponent<Double>?

    private fun replaceEntry(id: ResourceLocation, override: AttributeOverride) {
        identifier = id
        this.backing[id] = override
    }

    private fun update() {
        val attribute = attribute
        if (attribute != null) {
            // extract min & max fallbacks from internals
            this.override = override.copy(
                min_fallback = attribute.`data_attributes$min_fallback`(),
                max_fallback = attribute.`data_attributes$max_fallback`()
            )

            if (override.min.isNaN()) override.min = override.min_fallback
            if (override.max.isNaN()) override.max = override.max_fallback

            dataEntryMin?.textbox?.text(override.min.toString())
            dataEntryMax?.textbox?.text(override.max.toString())

            dataEntryMinFallback?.textbox?.setHint(Component.literal(override.min_fallback.toString()))
            dataEntryMaxFallback?.textbox?.setHint(Component.literal(override.max_fallback.toString()))
        }

        this.titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, BuiltInRegistries.ATTRIBUTE, { it.descriptionId }, isDefault()))

        titleLayout().tooltip(null)

        when {
            !isRegistered() -> {
                titleLayout().tooltip(Component.translatable("text.config.data_attributes.data_entry.invalid"))
            }
            isDefault() -> {
                titleLayout().tooltip(Component.translatable("text.config.data_attributes_data_entry.default"))
            }
        }
    }

    private fun changeAttributeOverride(changed: AttributeOverride) {
        val wasDefault = isDefault()
        replaceEntry(identifier, changed)
        if (wasDefault) {
            val index = provider.children().indexOf(this)
            this.provider.child(index, AttributeOverrideComponent(identifier, changed, backing, provider))
            this.remove()
            update()
        }
    }

    init {
        val stub = "text.config.data_attributes.data_entry.overrides"

        update()

        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15)).also { content ->
            content.verticalAlignment(VerticalAlignment.BOTTOM)
            content.gap(10)

            content.child(ConfigToggleButton().also { button ->
                button.enabled(override.enabled)
                button.onPress { changeAttributeOverride(override.copy(enabled = !override.enabled)) }
                button.renderer(ButtonRenderers.STANDARD)
            })

            if (!isDefault()) {
                content.child(Components.button(Component.translatable("text.config.data_attributes.data_entry.reset"))
                {
                   changeAttributeOverride(override.copy(
                       min = attribute?.`data_attributes$min_fallback`() ?: override.min_fallback,
                       max = attribute?.`data_attributes$max_fallback`() ?: override.max_fallback,
                       smoothness = 1.0,
                       formula = StackingFormula.Flat,
                       format = AttributeFormat.Whole
                   ))
                }
                    .renderer(ButtonRenderers.STANDARD)
                )

                content.child(Components.button(Component.translatable("text.config.data_attributes.data_entry.edit")) {
                    if (this.childById(EditFieldComponent::class.java, "edit-field") != null) return@button

                    val field = FieldComponents.identifier(
                        { newId, _ ->
                            if (newId in backing || !BuiltInRegistries.ATTRIBUTE.containsKey(newId)) return@identifier

                            this.backing.remove(identifier)
                            replaceEntry(newId, override)
                            update()
                        },
                        autocomplete = BuiltInRegistries.ATTRIBUTE.keySet()
                    ).apply { id("edit-field") }

                    field.textBox.predicate = { id -> id !in backing && BuiltInRegistries.ATTRIBUTE.containsKey(id) }

                    this.child(0, field)
                }
                    .renderer(ButtonRenderers.STANDARD)
                )

                content.child(Components.button(Component.translatable("text.config.data_attributes.data_entry.remove")) {
                    this.backing.remove(identifier)

                    val default = DataAttributesAPI.serverManager.defaults.overrides.entries[identifier]
                    if (default != null) {
                        val index = provider.children().indexOf(this)
                        this.provider.child(index, AttributeOverrideComponent(identifier, default, backing, provider))
                    }

                    remove()
                }
                    .renderer(ButtonRenderers.STANDARD)
                )
            }
        })

        dataEntryMin = EntryComponents.double(Component.translatable("$stub.min"), DataEntryComponent.Properties({ changeAttributeOverride(override.copy(min = it)) }), override.min.toString())
            .also(::child)
        dataEntryMax = EntryComponents.double(Component.translatable("$stub.max"), DataEntryComponent.Properties({ changeAttributeOverride(override.copy(max = it)) }), override.max.toString())
            .also(::child)

        dataEntryMinFallback = EntryComponents.double(Component.translatable("$stub.min_fallback"), suggestion = override.min_fallback.toString()).also(::child)
        dataEntryMaxFallback = EntryComponents.double(Component.translatable("$stub.max_fallback"), suggestion = override.max_fallback.toString()).also(::child)

        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30)).apply {
            verticalAlignment(VerticalAlignment.CENTER)
            gap(8)
            child(Components.label(Component.translatable("text.config.data_attributes.data_entry.overrides.smoothness"))
                .sizing(Sizing.content(), Sizing.fixed(20))
            )
            child(AttributeConfigComponents.smoothnessSlider(override) { changeAttributeOverride(override.copy(smoothness = max(it.value().round(2), 0.001))) }
                .positioning(Positioning.relative(100, 0))
            )
        }.id("smoothness"))

        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
            hf.verticalAlignment(VerticalAlignment.CENTER)
            hf.gap(8)
            hf.child(
                Components.label(Component.translatable("text.config.data_attributes.data_entry.overrides.formula"))
                    .sizing(Sizing.content(), Sizing.fixed(20))
            )
            hf.child(
                Components.button(Component.translatable("text.config.data_attributes.enum.stackingFormula.${override.formula.name.lowercase()}")) {
                    override.formula = StackingFormula.entries[override.formula.ordinal xor 1]
                    it.message = Component.translatable("text.config.data_attributes.enum.stackingFormula.${override.formula.name.lowercase()}")
                    changeAttributeOverride(override.copy(formula = override.formula))
                }
                    .renderer(ButtonRenderers.STANDARD)
                    .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
            )
        }.id("formula"))

        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
            hf.verticalAlignment(VerticalAlignment.CENTER)
            hf.gap(8)
            hf.child(
                Components.label(Component.translatable("text.config.data_attributes.data_entry.overrides.format"))
                    .sizing(Sizing.content(), Sizing.fixed(20))
            )
            hf.child(
                Components.button(Component.translatable("text.config.data_attributes.enum.format.${override.format.name.lowercase()}")) {
                    override.format = AttributeFormat.entries[override.format.ordinal xor 1]
                    it.message = Component.translatable("text.config.data_attributes.enum.format.${override.format.name.lowercase()}")
                    changeAttributeOverride(override.copy(formula = override.formula))
                }
                    .renderer(ButtonRenderers.STANDARD)
                    .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
            )
        }.id("format"))

        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Component.translatable(identifier.toLanguageKey()).toString() }))

        id(identifier.toString())

        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.overrides[identifier.toString()] = it }
    }
}