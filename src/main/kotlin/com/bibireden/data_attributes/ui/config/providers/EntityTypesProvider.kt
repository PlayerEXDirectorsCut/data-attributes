package com.bibireden.data_attributes.ui.config.providers

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.config.Validators
import com.bibireden.data_attributes.data.EntityTypeData
import com.bibireden.data_attributes.ui.colors.ColorCodes
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.components.RemoveButtonComponent
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.ParentComponent
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EntityTypesProvider(val option: Option<Map<Identifier, EntityTypeData>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    private val backing = HashMap(option.value())

    private val headerComponents: MutableMap<Identifier, Component> = mutableMapOf()
    private val entryComponents: MutableMap<Identifier, Component> = mutableMapOf()

    private fun createEntries(id: Identifier, types: Map<Identifier, Double>, isDefault: Boolean = false): CollapsibleFoldableContainer {
        val container = childById(CollapsibleFoldableContainer::class.java, id.toString())
            ?: CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), registryEntryToText(id, Registries.ENTITY_TYPE, { it.translationKey }, isDefault), true)
                .also { ct ->
                    ct.gap(4)
                    ct.id(id.toString())

                    if (!isDefault) {
                        ct.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                            .apply {
                                verticalAlignment(VerticalAlignment.CENTER)
                                gap(10)
                                id("dock")
                            }
                            .also { fl ->
                                fl.child(RemoveButtonComponent { backing.remove(id); refreshAndDisplayEntries(true) }
                                    .renderer(ButtonRenderers.STANDARD))

                                fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                                    if (ct.childById(FlowLayout::class.java, "edit-field") == null) {
                                        val field = FieldComponents.identifier(
                                            { newId, field ->
                                                if (backing.containsKey(newId) || !Registries.ENTITY_TYPE.containsId(newId)) return@identifier
                                                // ensured that this exists and is possible to swap
                                                backing.remove(id)?.let { backing[newId] = it }
                                                refreshAndDisplayEntries(true)
                                            }
                                        )

                                        field.textBox
                                            .addColorCondition(ColorCodes.RED, backing::containsKey)
                                            .addColorCondition(ColorCodes.GREEN) { Registries.ENTITY_TYPE.containsId(it) }

                                        ct.child(0, field)
                                    }
                                }
                                    .renderer(ButtonRenderers.STANDARD)
                                )

                                fl.child(
                                    Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                                        backing[id]?.let {
                                            val map = it.data.toMutableMap()
                                            map[Identifier("unknown")] = 0.0
                                            backing[id] = EntityTypeData(map)
                                        }
                                        refreshAndDisplayEntries(true)
                                    }
                                        .renderer(ButtonRenderers.STANDARD)
                                )
                            }
                        )
                    }

                    headerComponents[id] = ct

                    child(SearchAnchorComponent(ct.titleLayout(), Option.Key.ROOT, id::toString, { Text.translatable(id.toTranslationKey()).toString() }))

                    child(ct)
                }

        for ((entryId, value) in types) {
            createEntry(entryId, value, id, container, backing[id]?.data?.get(entryId) == null)
        }

        return container
    }

    private fun createEntry(id: Identifier, value: Double, parentId: Identifier, parent: CollapsibleContainer, isDefault: Boolean) {
        if (parent.childById(CollapsibleContainer::class.java, id.toString()) != null) return

        Containers.collapsible(Sizing.content(), Sizing.content(), registryEntryToText(id, Registries.ATTRIBUTE, { it.translationKey }, isDefault), true).also { ct ->
            ct.gap(4)
            ct.id(id.toString())

            if (!isDefault) {
                ct.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                    .apply {
                        verticalAlignment(VerticalAlignment.BOTTOM)
                        gap(10)
                    }
                    .also { fl ->
                        fl.child(RemoveButtonComponent {
                            val entry = backing[parentId]?.data?.toMutableMap() ?: return@RemoveButtonComponent
                            entry.remove(id)
                            backing[parentId] = EntityTypeData(entry)
                            refreshAndDisplayEntries(true)
                        }
                            .renderer(ButtonRenderers.STANDARD))

                        fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                            if (ct.childById(FlowLayout::class.java, "edit-field") == null) {
                                val field = FieldComponents.identifier(
                                    { newId, _ ->
                                        val entry = backing[parentId]?.data?.toMutableMap() ?: return@identifier
                                        if (entry.containsKey(newId) || !Registries.ATTRIBUTE.containsId(newId)) return@identifier
                                        // ensured that this exists and is possible to swap
                                        entry[newId] = entry.remove(id) ?: return@identifier
                                        backing[parentId] = EntityTypeData(entry)
                                        refreshAndDisplayEntries(true)
                                    }
                                )

                                field.textBox
                                    .addColorCondition(ColorCodes.RED) { backing[parentId]?.data?.get(it) != null }
                                    .addColorCondition(ColorCodes.GREEN) { Registries.ATTRIBUTE.containsId(it) }

                                ct.child(0, field)
                            }
                        }
                            .renderer(ButtonRenderers.STANDARD)
                        )
                    }
                )
            }

            ct.child(textBoxComponent(
                Text.translatable("text.config.data_attributes.data_entry.entity_types.value"),
                value,
                Validators::isNumeric,
                onChange = {
                    it.toDoubleOrNull()?.let { value ->
                        val data = this.backing.remove(parentId) ?: EntityTypeData()
                        val mapping = data.data.toMutableMap()
                        mapping[id] = value
                        this.backing[parentId] = data.copy(data = mapping)

                        if (isDefault) refreshAndDisplayEntries(true)
                    }
                }
            ).apply {
                val attribute = Registries.ATTRIBUTE[id] as? ClampedEntityAttribute ?: return@apply
                tooltip(Text.translatable("text.config.data_attributes.data_entry.entity_type_value", id, attribute.minValue, attribute.maxValue))
            })

            if (isDefault) {
                ct.titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
            }

            ct.child(SearchAnchorComponent(ct.titleLayout(), Option.Key.ROOT, id::toString, { Text.translatable(id.toTranslationKey()).toString() }))

            entryComponents[id] = ct

            parent.child(ct)
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
            Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                backing[Identifier("unknown")] = EntityTypeData()
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