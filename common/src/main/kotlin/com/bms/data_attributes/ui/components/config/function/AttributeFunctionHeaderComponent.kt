package com.bms.data_attributes.ui.components.config.function

import com.bms.data_attributes.DataAttributesClient
import com.bms.data_attributes.api.DataAttributesAPI
import com.bms.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bms.data_attributes.config.functions.AttributeFunction
import com.bms.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bms.data_attributes.ui.components.buttons.ButtonComponents
import com.bms.data_attributes.ui.components.config.AttributeConfigComponent
import com.bms.data_attributes.ui.components.config.ConfigDockComponent
import com.bms.data_attributes.ui.components.fields.FieldComponents
import com.bms.data_attributes.ui.config.providers.AttributeFunctionProvider
import com.bms.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class AttributeFunctionHeaderComponent(override var identifier: ResourceLocation, private val functions: MutableMap<ResourceLocation, AttributeFunction>, private val provider: AttributeFunctionProvider) : CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), Component.literal("<n/a>"), DataAttributesClient.UI_STATE.collapsible.functionParents[identifier.toString()] ?: true), AttributeConfigComponent<Attribute> {
    override val registry: Registry<Attribute>
        get() = BuiltInRegistries.ATTRIBUTE

    override val isDefault: Boolean
        get() = !provider.backing.containsKey(identifier)

    private fun createEntry(childId: ResourceLocation, function: AttributeFunction): AttributeFunctionComponent = childById(AttributeFunctionComponent::class.java, "child#$childId") ?: AttributeFunctionComponent(childId, function, identifier, provider).also { it.id("child#$childId") }.also(::child)

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Component.translatable(identifier.toLanguageKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, BuiltInRegistries.ATTRIBUTE, { it.descriptionId }, isDefault))
    }

    override fun update() {
        titleLayout().tooltip(null)
        when {
            !isRegistered -> {
                titleLayout().tooltip(Component.translatable("text.config.data_attributes.data_entry.invalid"))
            }
            isDefault -> {
                titleLayout().tooltip(Component.translatable("text.config.data_attributes_data_entry.default"))
            }
        }
        updateTextLabel()
        updateSearchAnchor()
    }

    fun addFunctions(functions: MutableMap<ResourceLocation, AttributeFunction>) {
        for ((id, function) in functions) createEntry(id, function)
    }

    init {
        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.functionParents[identifier.toString()] = it }

        update()

        child(
            ConfigDockComponent(ConfigDockComponent.ConfigDefaultProperties({ _, _ ->
                provider.backing.remove(identifier)

                val entries = DataAttributesAPI.serverManager.defaults.functions.entries[identifier]

                if (entries != null) {
                    forEachDescendant {
                        if (it is AttributeFunctionComponent) { if (entries.containsKey(it.identifier)) it.update() else it.remove() }
                    }
                    update()
                }
                else remove()
            })
            { _, _ ->
                if (childById(FlowLayout::class.java, "edit-field") == null) {
                    val field = FieldComponents.identifier(
                        { newId, _ ->
                            if (provider.backing.containsKey(newId) || !registry.containsKey(newId)) return@identifier

                            provider.backing.remove(identifier)?.let { provider.backing[newId] = it }

                            identifier = newId

                            for (afc in children().filterIsInstance<AttributeFunctionComponent>()) { afc.updateParent(identifier) }

                            update()
                        },
                        autocomplete = registry.keySet()
                    )

                    field.textBox.predicate = { provider.backing[identifier]?.get(it) == null && registry.containsKey(it) }

                    child(0, field)
                }
            }).child(
                Components.button(Component.translatable("text.config.data_attributes.buttons.add")) {
                    val entries = provider.backing.computeIfAbsent(identifier) { mutableMapOf() }

                    val childId = ResourceLocation.tryBuild("unknown", "resource")!!
                    val function = AttributeFunction()

                    entries[childId] = function

                    provider.backing[identifier] = entries

                    child(2, AttributeFunctionComponent(childId, function, identifier, provider))

                    update()
                }
                    .renderer(ButtonRenderers.STANDARD)
                    .horizontalSizing(Sizing.content())
                    .verticalSizing(Sizing.fixed(20))
            )
        )

        addFunctions(this.functions)
    }
}