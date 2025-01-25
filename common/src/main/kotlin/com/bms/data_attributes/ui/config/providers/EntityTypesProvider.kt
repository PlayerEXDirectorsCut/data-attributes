package com.bms.data_attributes.ui.config.providers

import com.bms.data_attributes.api.DataAttributesAPI
import com.bms.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bms.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bms.data_attributes.config.Validators
import com.bms.data_attributes.config.entities.EntityTypeData
import com.bms.data_attributes.config.entities.EntityTypeEntry
import com.bms.data_attributes.ext.round
import com.bms.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bms.data_attributes.ui.components.buttons.ButtonComponents
import com.bms.data_attributes.ui.components.fields.FieldComponents
import com.bms.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Component as OwoComponent
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.RangedAttribute

class EntityTypesProvider(val option: Option<Map<ResourceLocation, EntityTypeData>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    private val backing = HashMap(option.value())

    private val headerComponents: MutableMap<ResourceLocation, OwoComponent> = mutableMapOf()
    private val entryComponents: MutableMap<ResourceLocation, OwoComponent> = mutableMapOf()

    private fun createEntries(id: ResourceLocation, types: Map<ResourceLocation, EntityTypeEntry>, isDefault: Boolean = false): CollapsibleFoldableContainer {
        val container = childById(CollapsibleFoldableContainer::class.java, id.toString())
            ?: CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), registryEntryToText(id, BuiltInRegistries.ENTITY_TYPE, { it.descriptionId }, isDefault), true)
                .also { ct ->
                    ct.gap(4)
                    ct.id(id.toString())

                    ct.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                        .apply {
                            verticalAlignment(VerticalAlignment.CENTER)
                            gap(10)
                            id("dock")
                        }
                        .also { fl ->
                            if (!isDefault) {
                                fl.child(ButtonComponents.remove { backing.remove(id); refreshAndDisplayEntries(true) }
                                    .renderer(ButtonRenderers.STANDARD))

                                fl.child(Components.button(Component.translatable("text.config.data_attributes.data_entry.edit")) {
                                    if (ct.childById(FlowLayout::class.java, "edit-field") == null) {
                                        val field = FieldComponents.identifier(
                                            { newId, _ ->
                                                if (backing.containsKey(newId) || !BuiltInRegistries.ENTITY_TYPE.containsKey(newId)) return@identifier

                                                backing.remove(id)?.let { backing[newId] = it }
                                                refreshAndDisplayEntries(true)
                                            },
                                            autocomplete = BuiltInRegistries.ENTITY_TYPE.keySet()
                                        )

                                        field.textBox.predicate = { it !in backing && BuiltInRegistries.ENTITY_TYPE.containsKey(it) }

                                        ct.child(0, field)
                                    }
                                }
                                    .renderer(ButtonRenderers.STANDARD)
                                )
                            }

                            fl.child(
                                Components.button(Component.translatable("text.config.data_attributes.buttons.add")) {
                                    val map = backing[id]?.data?.toMutableMap() ?: mutableMapOf()
                                    map[ResourceLocation.tryBuild("unknown", "resource")!!] = EntityTypeEntry()
                                    backing[id] = EntityTypeData(map)
                                    refreshAndDisplayEntries(true)
                                }
                                    .renderer(ButtonRenderers.STANDARD)
                            )
                        }
                    )

                    headerComponents[id] = ct

                    child(SearchAnchorComponent(ct.titleLayout(), Option.Key.ROOT, id::toString, { Component.translatable(id.toLanguageKey()).toString() }))


                    if (id.toString() == "unknown:resource" && children.size > 1) child(1, ct)
                    else child(ct)
                }

        for ((entryId, entry) in types) {
            createEntry(entryId, entry, id, container, backing[id]?.data?.get(entryId) == null)
        }

        return container
    }

    private fun createEntry(id: ResourceLocation, entityTypeEntry: EntityTypeEntry, parentId: ResourceLocation, parent: CollapsibleContainer, isDefault: Boolean) {
        if (parent.childById(CollapsibleContainer::class.java, id.toString()) != null) return

        Containers.collapsible(Sizing.content(), Sizing.content(), registryEntryToText(id, BuiltInRegistries.ATTRIBUTE, { it.descriptionId }, isDefault), true).also { ct ->
            ct.gap(4)
            ct.id(id.toString())

            // find fallback
            try {
                entityTypeEntry.fallback = BuiltInRegistries.ENTITY_TYPE.get(parentId).let { entityType ->
                    BuiltInRegistries.ATTRIBUTE.getHolder(id).map(DefaultAttributes.getSupplier(entityType as EntityType<LivingEntity>)::getBaseValue).orElse(null)
                }?.round(2)
            }
            catch (_: Exception) {
                entityTypeEntry.fallback = 0.0
            }

            if (!isDefault) {
                ct.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                    .apply {
                        verticalAlignment(VerticalAlignment.BOTTOM)
                        gap(10)
                    }
                    .also { fl ->
                        fl.child(ButtonComponents.remove {
                            val entry = backing[parentId]?.data?.toMutableMap() ?: return@remove
                            entry.remove(id)
                            backing[parentId] = EntityTypeData(entry)
                            refreshAndDisplayEntries(true)
                        }
                            .renderer(ButtonRenderers.STANDARD))

                        fl.child(Components.button(Component.translatable("text.config.data_attributes.data_entry.edit")) {
                            if (ct.childById(FlowLayout::class.java, "edit-field") == null) {
                                val field = FieldComponents.identifier(
                                    { newId, _ ->
                                        val entry = backing[parentId]?.data?.toMutableMap() ?: return@identifier
                                        if (entry.containsKey(newId) || !BuiltInRegistries.ATTRIBUTE.containsKey(newId)) return@identifier

                                        entry[newId] = entry.remove(id) ?: return@identifier
                                        backing[parentId] = EntityTypeData(entry)

                                        refreshAndDisplayEntries(true)
                                    },
                                    autocomplete = BuiltInRegistries.ATTRIBUTE.keySet()
                                )

                                field.textBox.predicate = { backing[parentId]?.data?.get(it) == null && BuiltInRegistries.ATTRIBUTE.containsKey(it) }

                                ct.child(0, field)
                            }
                        }
                            .renderer(ButtonRenderers.STANDARD)
                        )
                    }
                )
            }

            ct.child(textBoxComponent(
                Component.translatable("text.config.data_attributes.data_entry.entity_types.value"),
                entityTypeEntry.value,
                Validators::isNumeric,
                onChange = {
                    it.toDoubleOrNull()?.let { value ->
                        val data = this.backing.remove(parentId) ?: EntityTypeData()
                        val mapping = data.data.toMutableMap()
                        mapping[id] = entityTypeEntry.copy(value = value)
                        this.backing[parentId] = data.copy(data = mapping)

                        if (isDefault) refreshAndDisplayEntries(true)
                    }
                }
            ).apply {
                val attribute = BuiltInRegistries.ATTRIBUTE[id] as? RangedAttribute ?: return@apply
                tooltip(Component.translatable("text.config.data_attributes.data_entry.entity_type_value", id, attribute.minValue, attribute.maxValue))
            })

            val fallback = entityTypeEntry.fallback
            if (fallback != null) {
                ct.child(textBoxComponent(
                    Component.translatable("text.config.data_attributes.data_entry.fallback"),
                    fallback,
                    Validators::isNumeric,
                ))
            }

            if (isDefault) {
                ct.titleLayout().tooltip(Component.translatable("text.config.data_attributes_data_entry.default"))
            }

            ct.child(SearchAnchorComponent(ct.titleLayout(), Option.Key.ROOT, id::toString, { Component.translatable(id.toLanguageKey()).toString() }))

            entryComponents[id] = ct

            if (id.toString() == "unknown:resource" && parent.children().size > 1) parent.child(1, ct)
            else parent.child(ct)
        }

        // force a rearrangement to bring the dock up top~
        parent.childById(FlowLayout::class.java, "dock")?.let {
            parent.removeChild(it)
            parent.child(0, it)
        }
    }

    private fun refreshAndDisplayEntries(clearHeaders: Boolean = false) {
        if (clearHeaders) {
            headerComponents.values.forEach { component ->
                component.id(null)
                component.remove()
            }
            headerComponents.clear()
        }
        entryComponents.values.forEach { component ->
            component.id(null)
            component.remove()
        }
        entryComponents.clear()

        for ((id, types) in backing) {
            createEntries(id, types.data, false)
        }

        for ((id, types) in DataAttributesAPI.serverManager.defaults.types.entries) {
            createEntries(id, types, id !in backing)
        }
    }

    init {
        child(
            Components.button(Component.translatable("text.config.data_attributes.buttons.add")) {
                backing[ResourceLocation.tryBuild("unknown", "resource")!!] = EntityTypeData()
                refreshAndDisplayEntries(true)
            }
                .renderer(ButtonRenderers.STANDARD)
                .horizontalSizing(Sizing.content())
                .verticalSizing(Sizing.fixed(20))
        )

        refreshAndDisplayEntries()
    }

    override fun isValid() = !this.option.detached()
    override fun parsedValue() = backing
}