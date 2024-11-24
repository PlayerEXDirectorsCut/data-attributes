package com.bibireden.data_attributes.ui.config.providers

import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.config.Validators
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.functions.AttributeFunctionConfig
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.components.buttons.ButtonComponents
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeFunctionProvider(val option: Option<AttributeFunctionConfig>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    private val backing = option.value().data

    private val headerComponents: MutableMap<Identifier, Component> = mutableMapOf()
    private val entryComponents: MutableMap<Identifier, Component> = mutableMapOf()

    private fun isFunctionHeaderDefault(id: Identifier) = !backing.containsKey(id)

    private fun isFunctionChildDefault(parentId: Identifier, childId: Identifier) = backing[parentId]?.containsKey(childId) != true

    private fun updateHeader(id: Identifier, component: CollapsibleContainer, isDefault: Boolean) {
        component.titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(id, Registries.ATTRIBUTE, { it.translationKey }, isDefault))
    }

    private fun updateChild(id: Identifier, parentId: Identifier, incomingFunction: AttributeFunction, component: CollapsibleContainer) {
        backing[parentId]?.set(id, incomingFunction)
    }

    private fun getOrCreateEntryContainer(id: Identifier, isDefault: Boolean): CollapsibleFoldableContainer {
        return childById(CollapsibleFoldableContainer::class.java, id.toString()) ?: CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), registryEntryToText(id, Registries.ATTRIBUTE, { it.translationKey }, isDefault), DataAttributesClient.UI_STATE.collapsible.functionParents[id.toString()] ?: true).also { cf ->
            headerComponents[id] = cf
            cf.id(id.toString())
            cf.onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.functionParents[id.toString()] = it }

            cf.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                .apply {
                    verticalAlignment(VerticalAlignment.CENTER)
                    gap(10)
                    id("dock")
                }
                .also { fl ->
                    if (!isDefault) {
                        fl.child(ButtonComponents.remove { backing.remove(id); cf.remove() }
                            .renderer(ButtonRenderers.STANDARD))

                        fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                            if (cf.childById(FlowLayout::class.java, "edit-field") == null) {
                                val field = FieldComponents.identifier(
                                    { newId, _ ->
                                        if (backing.containsKey(newId) || !Registries.ATTRIBUTE.containsId(newId)) return@identifier

                                        backing.remove(id)?.let { backing[newId] = it }

                                        updateHeader(newId, cf, isFunctionHeaderDefault(newId))
                                    },
                                    autocomplete = Registries.ATTRIBUTE.ids
                                )

                                field.textBox.predicate = { backing[id]?.get(it) == null && Registries.ATTRIBUTE.containsId(it) }

                                cf.child(0, field)
                            }
                        }
                            .renderer(ButtonRenderers.STANDARD)
                        )
                    }

                    fl.child(
                        Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                            val map = backing[id] ?: mutableMapOf()
                            val createdId = Identifier("unknown")
                            val function = AttributeFunction()
                            map[createdId] = function
                            backing[createdId] = map
                            createEntry(createdId, function, createdId, false, cf)
                        }
                            .renderer(ButtonRenderers.STANDARD)
                    )
                }
            )

            if (id.toString() == "minecraft:unknown" && children.size > 1) child(1, cf)
            else child(cf)
        }
    }

    private fun createEntry(incomingIdentifier: Identifier, function: AttributeFunction, parentId: Identifier, isDefault: Boolean, parent: CollapsibleFoldableContainer): CollapsibleContainer {
        var id = incomingIdentifier
        // find if it exists already to ignore the default
        val located = parent.childById(CollapsibleContainer::class.java, "${id}#child-fn")
        if (located != null) return located

        val container = Containers.collapsible(Sizing.content(), Sizing.content(), registryEntryToText(id, Registries.ATTRIBUTE, { it.translationKey }, isDefault), DataAttributesClient.UI_STATE.collapsible.functionChildren[parentId.toString()]?.get(id.toString()) == true).apply {
            gap(4)
            id("${id}#child-fn")
            onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.functionChildren.computeIfAbsent(parentId.toString()) { mutableMapOf() }[id.toString()] = true }

            val attribute = Registries.ATTRIBUTE[id]
            if (attribute !is EntityAttribute) {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
            } else if (isDefault) {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
            }

            child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                .apply {
                    verticalAlignment(VerticalAlignment.BOTTOM)
                    gap(10)
                }
                .also { fl ->
                    fl.child(
                        ConfigToggleButton()
                            .enabled(function.enabled)
                            .onPress { updateChild(id, parentId, function.copy(enabled = !function.enabled), this) }
                            .renderer(ButtonRenderers.STANDARD)
                    )

                    if (!isDefault) {
                        fl.child(
                            ButtonComponents.remove {
                                backing[parentId]?.remove(id)
                                remove()
                            }
                                .renderer(ButtonRenderers.STANDARD)
                        )
                        fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                            if (fl.childById(FlowLayout::class.java, "edit-field") == null) {
                                val field = FieldComponents.identifier(
                                    { newId, _ ->
                                        val entry = backing[parentId] ?: return@identifier
                                        if (!Registries.ATTRIBUTE.containsId(newId) || entry[newId] != null) return@identifier

                                        entry[newId] = entry.remove(id) ?: return@identifier
                                        backing[parentId] = entry

                                        id = newId

                                        updateHeader(id, this, isFunctionChildDefault(parentId, newId))
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
            child(textBoxComponent(
                Text.translatable("text.config.data_attributes.data_entry.functions.value"),
                function.value,
                Validators::isNumeric,
                onChange = {
                    it.toDoubleOrNull()?.let { z -> updateChild(id, parentId, function.copy(value = z), this) }
                }
            ).also {
                if (attribute is ClampedEntityAttribute) {
                    it.tooltip(Text.translatable("text.config.data_attributes.data_entry.function_child", id, attribute.minValue, attribute.maxValue))
                }
            })

            child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).apply {
                    verticalAlignment(VerticalAlignment.CENTER)

                    child(Components.label(Text.translatable("text.config.data_attributes.data_entry.functions.behavior")).sizing(Sizing.content(), Sizing.fixed(20)))
                    child(
                        Components.button(Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")) {
                            function.behavior = StackingBehavior.entries[function.behavior.ordinal xor 1]

                            it.message = Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")

                            backing[parentId]?.set(id, function.copy(behavior = function.behavior))
                        }
                            .renderer(ButtonRenderers.STANDARD)
                            .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
                    )
                }
            )

            child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { id.toString() }, { Text.translatable(id.toTranslationKey()).toString() }))
        }

        if (id.toString() == "minecraft:unknown" && parent.children().size > 1) parent.child(1, container)
        else parent.child(container)

        entryComponents[id] = container

        // force a rearrangement to bring the dock up top~
        parent.childById(FlowLayout::class.java, "dock")?.let {
            parent.removeChild(it)
            parent.child(0, it)
        }

        return container
    }

    private fun createFunctionEntries(functions: Map<Identifier, AttributeFunction>, parentId: Identifier, parent: CollapsibleFoldableContainer) {
        functions.forEach { (id, function) ->
            val isDefault = backing[parentId]?.get(id) == null
            createEntry(id, function, parentId, isDefault, parent)
        }
    }

    init {
        child(
            Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                backing[Identifier("unknown")] = mutableMapOf()
                val component = getOrCreateEntryContainer(Identifier("unknown"), false)
                component.remove()
                child(1, component)
            }
                .renderer(ButtonRenderers.STANDARD)
                .horizontalSizing(Sizing.content())
                .verticalSizing(Sizing.fixed(20))
        )

        for ((id, functions) in backing) {
            createFunctionEntries(functions, id, getOrCreateEntryContainer(id, !backing.containsKey(id)))
        }

        for ((id, functions) in DataAttributesAPI.serverManager.defaults.functions.entries) {
            createFunctionEntries(functions, id, getOrCreateEntryContainer(id, !backing.containsKey(id)))
        }
    }

    override fun isValid() = !this.option.detached()
    override fun parsedValue() = AttributeFunctionConfig(backing)
}