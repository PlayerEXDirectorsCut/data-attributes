package com.bibireden.data_attributes.ui.components.config.function

import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.components.buttons.ButtonComponents
import com.bibireden.data_attributes.ui.components.config.AttributeConfigComponent
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.config.providers.AttributeFunctionProviderV2
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeFunctionHeaderComponent(override var identifier: Identifier, private val provider: AttributeFunctionProviderV2) : CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), Text.of("<n/a>"), DataAttributesClient.UI_STATE.collapsible.functionParents[identifier.toString()] ?: true), AttributeConfigComponent<EntityAttribute> {
    override val registry: Registry<EntityAttribute>
        get() = Registries.ATTRIBUTE

    /** Current functions of the header obtained from the provider. */
    val functions: MutableMap<Identifier, AttributeFunction>?
        get() = provider.backing[identifier]

    override val isDefault: Boolean
        get() = !provider.backing.containsKey(identifier)

    private fun createEntry(childId: Identifier, function: AttributeFunction): AttributeFunctionComponent = AttributeFunctionComponent(childId, function, identifier, provider).also(::child)

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Text.translatable(identifier.toTranslationKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, Registries.ATTRIBUTE, { it.translationKey }, isDefault))
    }

    private fun updateComponent() {
        titleLayout().tooltip(null)
        if (!isRegistered) {
            titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
        } else if (isDefault) {
            titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
        }
        updateTextLabel()
        updateSearchAnchor()
    }

    init {
        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.functionParents[identifier.toString()] = it }

        updateComponent()

        child(
            Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
            .apply {
                verticalAlignment(VerticalAlignment.CENTER)
                gap(10)
                id("dock")
            }
            .also { fl ->
                if (!isDefault) {
                    fl.child(ButtonComponents.remove { provider.backing.remove(identifier); remove() }
                        .renderer(ButtonRenderers.STANDARD))

                    fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                        if (childById(FlowLayout::class.java, "edit-field") == null) {
                            val field = FieldComponents.identifier(
                                { newId, _ ->
                                    if (provider.backing.containsKey(newId) || !Registries.ATTRIBUTE.containsId(newId)) return@identifier

                                    provider.backing.remove(identifier)?.let { provider.backing[newId] = it }

                                    identifier = newId

                                    updateComponent()
                                },
                                autocomplete = Registries.ATTRIBUTE.ids
                            )

                            field.textBox.predicate = { provider.backing[identifier]?.get(it) == null && Registries.ATTRIBUTE.containsId(it) }

                            child(0, field)
                        }
                    }
                        .renderer(ButtonRenderers.STANDARD)
                    )
                }

                fl.child(
                    Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                        val entries = provider.backing[identifier] ?: return@button

                        val childId = Identifier("unknown")
                        val function = AttributeFunction()

                        entries[childId] = function

                        provider.backing.computeIfAbsent(identifier) { mutableMapOf() }[childId] = function

                        child(1, AttributeFunctionComponent(childId, function, identifier, provider))

                        updateComponent()
                    }
                        .renderer(ButtonRenderers.STANDARD)
                        .horizontalSizing(Sizing.content())
                        .verticalSizing(Sizing.fixed(20))
                )
            }
        )

        this.functions?.forEach(::createEntry)
    }
}