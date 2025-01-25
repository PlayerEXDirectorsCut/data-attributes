package com.bms.data_attributes.ui.components.config.entities

import com.bms.data_attributes.DataAttributesClient
import com.bms.data_attributes.api.DataAttributesAPI
import com.bms.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bms.data_attributes.config.entities.EntityTypeData
import com.bms.data_attributes.config.entities.EntityTypeEntry
import com.bms.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bms.data_attributes.ui.components.buttons.ButtonComponents
import com.bms.data_attributes.ui.components.config.AttributeConfigComponent
import com.bms.data_attributes.ui.components.config.ConfigDockComponent
import com.bms.data_attributes.ui.components.config.function.AttributeFunctionComponent
import com.bms.data_attributes.ui.components.fields.FieldComponents
import com.bms.data_attributes.ui.config.providers.EntityTypesProviderV2
import com.bms.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class EntityTypesHeaderComponent(override var identifier: ResourceLocation, private val entityTypes: MutableMap<ResourceLocation, EntityTypeEntry>, private val provider: EntityTypesProviderV2)
    : CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), Component.literal("<n/a>"), DataAttributesClient.UI_STATE.collapsible.entityTypeHeaders[identifier.toString()] ?: true), AttributeConfigComponent<EntityType<*>> {

    override val registry: Registry<EntityType<*>> = BuiltInRegistries.ENTITY_TYPE

    override val isDefault: Boolean
        get() = !provider.backing.containsKey(identifier)

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Component.translatable(identifier.toLanguageKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, registry, { it.descriptionId }, isDefault))
    }

    private fun createEntry(entryId: ResourceLocation, entry: EntityTypeEntry): EntityTypesComponent = childById(EntityTypesComponent::class.java, "entry#$entryId") ?: EntityTypesComponent(entryId, identifier, entry, provider).also { it.id("entry#$entryId")}.also(::child)

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

    fun addEntityTypes(entityTypes: MutableMap<ResourceLocation, EntityTypeEntry>) {
        for ((id, types) in entityTypes) createEntry(id, types)
    }

    init {
        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.entityTypeHeaders[identifier.toString()] = it }

        child(
            ConfigDockComponent(ConfigDockComponent.ConfigDefaultProperties({ _, _ ->
                provider.backing.remove(identifier)

                val entries = DataAttributesAPI.serverManager.defaults.types.entries[identifier]

                if (entries != null) {
                    forEachDescendant { if (it is EntityTypesComponent) { if (entries.containsKey(it.identifier)) it.update() else it.remove() } }
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

                            children().filterIsInstance<EntityTypesComponent>().forEach { it.updateParent(identifier) }

                            update()
                        },
                        autocomplete = registry.keySet()
                    )

                    field.textBox.predicate = { provider.backing[identifier]?.data?.get(it) == null && registry.containsKey(it) }

                    child(0, field)
                }
            }).child(
                Components.button(Component.translatable("text.config.data_attributes.buttons.add")) {
                    val map = provider.backing[identifier]?.data?.toMutableMap() ?: mutableMapOf()
                    val childId = ResourceLocation.tryBuild("unknown", "resource")!!
                    val entry = EntityTypeEntry()
                    map[childId] = entry
                    provider.backing[identifier] = EntityTypeData(map)

                    child(1, EntityTypesComponent(childId, identifier, entry, provider))

                    update()
                }
                    .renderer(ButtonRenderers.STANDARD)
                    .horizontalSizing(Sizing.content())
                    .verticalSizing(Sizing.fixed(20))
            )
        )

        addEntityTypes(this.entityTypes)

        update()
    }
}