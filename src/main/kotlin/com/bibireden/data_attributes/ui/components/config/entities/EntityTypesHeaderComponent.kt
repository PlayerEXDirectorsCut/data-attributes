package com.bibireden.data_attributes.ui.components.config.entities

import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.entities.EntityTypeData
import com.bibireden.data_attributes.config.entities.EntityTypeEntry
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.components.buttons.ButtonComponents
import com.bibireden.data_attributes.ui.components.config.AttributeConfigComponent
import com.bibireden.data_attributes.ui.components.config.ConfigDockComponent
import com.bibireden.data_attributes.ui.components.config.function.AttributeFunctionComponent
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.config.providers.EntityTypesProviderV2
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntityTypesHeaderComponent(override var identifier: Identifier, private val entityTypes: MutableMap<Identifier, EntityTypeEntry>, private val provider: EntityTypesProviderV2)
    : CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), Text.of("<n/a>"), DataAttributesClient.UI_STATE.collapsible.entityTypeHeaders[identifier.toString()] ?: true), AttributeConfigComponent<EntityType<*>> {

    override val registry: Registry<EntityType<*>> = Registries.ENTITY_TYPE

    override val isDefault: Boolean
        get() = !provider.backing.containsKey(identifier)

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Text.translatable(identifier.toTranslationKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, registry, { it.translationKey }, isDefault))
    }

    private fun createEntry(entryId: Identifier, entry: EntityTypeEntry): EntityTypesComponent = childById(EntityTypesComponent::class.java, "entry#$entryId") ?: EntityTypesComponent(entryId, identifier, entry, provider).also { it.id("entry#$entryId")}.also(::child)

    override fun update() {
        titleLayout().tooltip(null)
        when {
            !isRegistered -> {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
            }
            isDefault -> {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
            }
        }
        updateTextLabel()
        updateSearchAnchor()
    }

    fun addEntityTypes(entityTypes: MutableMap<Identifier, EntityTypeEntry>) {
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
                            if (provider.backing.containsKey(newId) || !registry.containsId(newId)) return@identifier

                            provider.backing.remove(identifier)?.let { provider.backing[newId] = it }

                            identifier = newId

                            children().filterIsInstance<EntityTypesComponent>().forEach { it.updateParent(identifier) }

                            update()
                        },
                        autocomplete = registry.ids
                    )

                    field.textBox.predicate = { provider.backing[identifier]?.data?.get(it) == null && registry.containsId(it) }

                    child(0, field)
                }
            }).child(
                Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                    val map = provider.backing[identifier]?.data?.toMutableMap() ?: mutableMapOf()
                    val childId = Identifier("unknown")
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