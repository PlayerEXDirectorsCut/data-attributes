package com.bibireden.data_attributes.ui.components.config.function

import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.ui.components.buttons.ButtonComponents
import com.bibireden.data_attributes.ui.components.entries.DataEntryComponent
import com.bibireden.data_attributes.ui.components.entries.EntryComponents
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.config.providers.AttributeFunctionProviderV2
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeFunctionComponent(private var identifier: Identifier, private var function: AttributeFunction, private var parentId: Identifier, private val provider: AttributeFunctionProviderV2)
    : CollapsibleContainer(Sizing.content(), Sizing.content(), Text.of("<n/a>"), DataAttributesClient.UI_STATE.collapsible.functionChildren[parentId.toString()]?.get(identifier.toString()) ?: true)
{
    private val backing = provider.backing

    /** Current registered attribute with this function. */
    private val attribute: MutableEntityAttribute?
        get() = Registries.ATTRIBUTE[identifier]

    private val valueEntry: DataEntryComponent<Double>

    private fun isRegistered() = attribute != null

    private fun isDefault() = provider.backing[parentId]?.containsKey(identifier) != true

    private fun updateEntryToBacking(id: Identifier, function: AttributeFunction) {
        backing[parentId]?.set(id, function)
    }

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Text.translatable(identifier.toTranslationKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, Registries.ATTRIBUTE, { it.translationKey }, isDefault()))
    }

    private fun updateComponent() {
        titleLayout().tooltip(null)
        if (!isRegistered()) {
            titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
        } else if (isDefault()) {
            titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
        }
        updateTextLabel()
        updateSearchAnchor()
    }

    init {
        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.functionChildren.computeIfAbsent(parentId.toString()) { mutableMapOf() }[identifier.toString()] = it }

        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
            .apply {
                verticalAlignment(VerticalAlignment.BOTTOM)
                gap(10)
            }
            .also { fl ->
                fl.child(ConfigToggleButton().enabled(function.enabled).onPress { updateEntryToBacking(identifier, function.copy(enabled = !function.enabled)) }
                    .renderer(ButtonRenderers.STANDARD))

                if (!isDefault()) {
                    fl.child(ButtonComponents.remove { backing[parentId]?.remove(identifier); remove() }.renderer(ButtonRenderers.STANDARD))

                    fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                        if (fl.childById(FlowLayout::class.java, "edit-field") == null) {
                            val field = FieldComponents.identifier(
                                { newId, _ ->
                                    val entry = backing[parentId] ?: return@identifier
                                    if (!Registries.ATTRIBUTE.containsId(newId) || entry[newId] != null) return@identifier

                                    entry[newId] = entry.remove(identifier) ?: return@identifier
                                    backing[parentId] = entry

                                    identifier = newId

                                    updateComponent()
                                },
                                autocomplete = Registries.ATTRIBUTE.ids
                            )

                            field.textBox.predicate = { backing[parentId]?.get(it) == null && Registries.ATTRIBUTE.containsId(it) }

                            child(0, field)
                        }
                    }
                        .renderer(ButtonRenderers.STANDARD)
                    )
                }
            }
        )

       valueEntry = EntryComponents.double(Text.translatable("text.config.data_attributes.data_entry.functions.value"), DataEntryComponent.Properties({ updateEntryToBacking(identifier, function.copy(value = it)) }), function.value.toString()).also {
           if (attribute is ClampedEntityAttribute) {
               val clamped = attribute as ClampedEntityAttribute
               it.tooltip(Text.translatable("text.config.data_attributes.data_entry.function_child", identifier, clamped.minValue, clamped.maxValue))
           }
       }.also(::child)

        child(
            Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).apply {
                verticalAlignment(VerticalAlignment.CENTER)

                child(Components.label(Text.translatable("text.config.data_attributes.data_entry.functions.behavior")).sizing(Sizing.content(), Sizing.fixed(20)))
                child(
                    Components.button(Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")) {
                        function.behavior = StackingBehavior.entries[function.behavior.ordinal xor 1]
                        it.message = Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")
                        updateEntryToBacking(identifier, function.copy(behavior = function.behavior))
                    }
                        .renderer(ButtonRenderers.STANDARD)
                        .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
                )
            }
        )

        updateComponent()
    }
}