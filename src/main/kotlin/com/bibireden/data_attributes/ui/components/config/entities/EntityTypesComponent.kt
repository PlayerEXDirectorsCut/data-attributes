package com.bibireden.data_attributes.ui.components.config.entities

import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.config.Validators
import com.bibireden.data_attributes.config.entities.EntityTypeData
import com.bibireden.data_attributes.config.entities.EntityTypeEntry
import com.bibireden.data_attributes.ext.round
import com.bibireden.data_attributes.ui.components.buttons.ButtonComponents
import com.bibireden.data_attributes.ui.components.config.AttributeConfigComponent
import com.bibireden.data_attributes.ui.components.config.ConfigDockComponent
import com.bibireden.data_attributes.ui.components.entries.DataEntryComponent
import com.bibireden.data_attributes.ui.components.entries.EntryComponents
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.config.providers.EntityTypesProviderV2
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntityTypesComponent(override var identifier: Identifier, private var parentId: Identifier, private var entry: EntityTypeEntry, private val provider: EntityTypesProviderV2)
    : CollapsibleContainer(Sizing.content(), Sizing.content(), Text.of("<n/a>"), DataAttributesClient.UI_STATE.collapsible.entityTypeEntries[parentId.toString()]?.get(identifier.toString()) ?: true), AttributeConfigComponent<EntityAttribute> {

    override val registry: Registry<EntityAttribute> = Registries.ATTRIBUTE

    override val isDefault: Boolean
        get() = provider.backing[parentId]?.data?.get(identifier) == null

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Text.translatable(identifier.toTranslationKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, registry, { it.translationKey }, isDefault))
    }

    override fun update() {
        titleLayout().tooltip(null)
        when {
            !isRegistered -> titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
            isDefault -> titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
        }

        // find fallback
        try {
            entry.fallback = Registries.ENTITY_TYPE.get(parentId)
                ?.let { entityType -> registry.get(identifier)?.let(DefaultAttributeRegistry.get(entityType as EntityType<LivingEntity>)::getBaseValue) }
                ?.round(2)
        }
        catch (_: IllegalArgumentException) {
            entry.fallback = 0.0
        }

        val fallback = entry.fallback
        if (fallback != null) {
            childById(FlowLayout::class.java, "fallback")?.remove()
            child(textBoxComponent(Text.translatable("text.config.data_attributes.data_entry.fallback"), fallback, Validators::isNumeric).id("fallback"))
        }

        updateTextLabel()
        updateSearchAnchor()
    }


    private val valueEntry: DataEntryComponent<Double> = EntryComponents.double(Text.translatable("text.config.data_attributes.data_entry.entity_types.value"), DataEntryComponent.Properties({ updateEntryToBacking(entry.copy(value = it)) }), entry.value.toString()).also {
        val attribute = registryEntry
        if (attribute is ClampedEntityAttribute) {
            it.tooltip(Text.translatable("text.config.data_attributes.data_entry.entity_type_value", identifier, attribute.minValue, attribute.maxValue))
        }
    }

    private fun updateEntryToBacking(entry: EntityTypeEntry) {
        val etd = provider.backing[parentId] ?: return
        val mapping = etd.data.toMutableMap()
        mapping[identifier] = entry
        provider.backing[parentId] = EntityTypeData(mapping)

        update()
    }

    fun updateParent(parentId: Identifier) { this.parentId = parentId }


    init {
        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.entityTypeEntries.computeIfAbsent(parentId.toString()) { mutableMapOf() } [identifier.toString()] = it }

        child(ConfigDockComponent(ConfigDockComponent.ConfigDefaultProperties({ _, _ ->
            val entry = provider.backing[parentId]?.data?.toMutableMap() ?: return@ConfigDefaultProperties
            if (entry.remove(identifier) != null) {
                val defaultEntry = DataAttributesAPI.serverManager.defaults.types.entries[parentId]?.get(identifier)
                if (defaultEntry != null) {
                    valueEntry.textbox.text = defaultEntry.value.toString()
                }
                else remove()

                provider.backing[parentId] = EntityTypeData(entry)

                update()
            }
            }, {_, _ ->
                if (childById(FlowLayout::class.java, "edit-field") == null) {
                    val field = FieldComponents.identifier(
                        { newId, _ ->
                            val entry = provider.backing[parentId]?.data?.toMutableMap() ?: return@identifier
                            if (entry.containsKey(newId) || !registry.containsId(newId)) return@identifier

                            entry[newId] = entry.remove(identifier) ?: return@identifier
                            provider.backing[parentId] = EntityTypeData(entry)

                            identifier = newId

                            update()
                        },
                        autocomplete = Registries.ATTRIBUTE.ids
                    )

                    field.textBox.predicate = { provider.backing[parentId]?.data?.get(it) == null && registry.containsId(it) }

                    child(0, field)
                }
            }
        )))

        valueEntry.also(::child)

        update()
    }
}