package com.bibireden.data_attributes.ui.config.providers

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.config.Validators
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.functions.AttributeFunctionConfig
import com.bibireden.data_attributes.data.EntityTypeData
import com.bibireden.data_attributes.ui.colors.ColorCodes
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.components.RemoveButtonComponent
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
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

    private fun getOrCreateEntryContainer(id: Identifier, isDefault: Boolean): CollapsibleFoldableContainer {
        return childById(CollapsibleFoldableContainer::class.java, id.toString()) ?: CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), registryEntryToText(id, Registries.ATTRIBUTE, { it.translationKey }, isDefault), true).also { cf ->
            headerComponents[id] = cf
            cf.id(id.toString())

            cf.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                .apply {
                    verticalAlignment(VerticalAlignment.CENTER)
                    gap(10)
                    id("dock")
                }
                .also { fl ->
                    if (!isDefault) {
                        fl.child(RemoveButtonComponent { backing.remove(id); refreshAndDisplayEntries() }
                            .renderer(ButtonRenderers.STANDARD))

                        fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                            if (cf.childById(FlowLayout::class.java, "edit-field") == null) {
                                val field = FieldComponents.identifier(
                                    { newId, _ ->
                                        if (backing.containsKey(newId) || !Registries.ATTRIBUTE.containsId(newId)) return@identifier
                                        // ensured that this exists and is possible to swap
                                        backing.remove(id)?.let { backing[newId] = it }
                                        refreshAndDisplayEntries()
                                    }
                                )

                                field.textBox
                                    .addColorCondition(ColorCodes.RED) { backing[id]?.get(it) != null }
                                    .addColorCondition(ColorCodes.GREEN) { Registries.ATTRIBUTE.containsId(it) }

                                cf.child(0, field)
                            }
                        }
                            .renderer(ButtonRenderers.STANDARD)
                        )
                    }

                    fl.child(
                        Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                            val map = backing[id] ?: mutableMapOf()
                            map[Identifier("unknown")] = AttributeFunction()
                            backing[id] = map
                            refreshAndDisplayEntries(true)
                        }
                            .renderer(ButtonRenderers.STANDARD)
                    )
                }
            )

            child(cf)
        }
    }

    private fun createEntry(id: Identifier, function: AttributeFunction, parentId: Identifier, isDefault: Boolean, parent: CollapsibleFoldableContainer): CollapsibleContainer {
        // find if it exists already to ignore the default
        val located = parent.childById(CollapsibleContainer::class.java, "${id}#child-fn")
        if (located != null) return located

        val container = Containers.collapsible(Sizing.content(), Sizing.content(), registryEntryToText(id, Registries.ATTRIBUTE, { it.translationKey }, isDefault), true).apply {
            gap(4)
            id("${id}#child-fn")

            val attribute = Registries.ATTRIBUTE[id]
            if (attribute !is EntityAttribute) {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
            } else if (isDefault) {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
            }

            if (!isDefault) {
                child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                    .apply {
                        verticalAlignment(VerticalAlignment.BOTTOM)
                        gap(10)
                    }
                    .also { fl ->
                        fl.child(ConfigToggleButton().also { button ->
                            button.enabled(function.enabled)
                            button.onPress {
                                backing[parentId]?.replace(id, function.copy(enabled = !function.enabled))
                                refreshAndDisplayEntries(isDefault)
                            }
                            button.renderer(ButtonRenderers.STANDARD)
                        })
                        fl.child(
                            RemoveButtonComponent {
                                backing[parentId]?.remove(id)
                                refreshAndDisplayEntries()
                            }
                            .renderer(ButtonRenderers.STANDARD)
                        )
                        fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                            if (fl.childById(FlowLayout::class.java, "edit-field") == null) {
                                val field = FieldComponents.identifier(
                                    { newId, _ ->
                                        val entry = backing[parentId] ?: return@identifier
                                        if (!Registries.ATTRIBUTE.containsId(newId) || entry[newId] != null) return@identifier
                                        // ensured that this exists and is possible to swap
                                        entry[newId] = entry.remove(id) ?: return@identifier
                                        backing[parentId] = entry
                                        refreshAndDisplayEntries()
                                    }
                                )

                                field.textBox
                                    .addColorCondition(ColorCodes.RED) { backing[parentId]?.get(it) != null }
                                    .addColorCondition(ColorCodes.GREEN) { Registries.ATTRIBUTE.containsId(it) }

                                child(0, field)
                            }
                        }
                            .renderer(ButtonRenderers.STANDARD)
                        )
                    }
                )
            }

            child(textBoxComponent(
                Text.translatable("text.config.data_attributes.data_entry.functions.value"),
                function.value,
                Validators::isNumeric,
                onChange = {
                    it.toDoubleOrNull()?.let { v ->
                        backing[parentId]?.replace(id, function.copy(value = v))
                        refreshAndDisplayEntries(isDefault)
                    }
                }
            ).also {
                if (attribute is ClampedEntityAttribute) {
                    it.tooltip(Text.translatable("text.config.data_attributes.data_entry.function_child", id, attribute.minValue, attribute.maxValue))
                }
            })

            child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).apply {
                    verticalAlignment(VerticalAlignment.CENTER)

                    child(
                        Components.label(Text.translatable("text.config.data_attributes.data_entry.functions.behavior"))
                            .sizing(Sizing.content(), Sizing.fixed(20))
                    )
                    child(
                        Components.button(Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")) {
                            function.behavior = StackingBehavior.entries[function.behavior.ordinal xor 1]

                            it.message = Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")

                            backing[parentId]?.replace(id, function.copy(behavior = function.behavior))
                            refreshAndDisplayEntries(isDefault)
                        }
                            .renderer(ButtonRenderers.STANDARD)
                            .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
                    )
                }
            )

            child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { id.toString() }, { Text.translatable(id.toTranslationKey()).toString() }))
        }

        parent.child(container)
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

    private fun refreshAndDisplayEntries(clearHeaders: Boolean = true) {
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

        for ((id, functions) in backing) {
            createFunctionEntries(functions, id, getOrCreateEntryContainer(id, !backing.containsKey(id)))
        }

        for ((id, functions) in DataAttributesAPI.serverManager.defaults.functions.entries) {
            createFunctionEntries(functions, id, getOrCreateEntryContainer(id, !backing.containsKey(id)))
        }
    }

    init {
        child(
            Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                backing[Identifier("unknown")] = mutableMapOf()
                refreshAndDisplayEntries()
            }
                .renderer(ButtonRenderers.STANDARD)
                .horizontalSizing(Sizing.content())
                .verticalSizing(Sizing.fixed(20))
        )

        refreshAndDisplayEntries()
    }

    override fun isValid() = !this.option.detached()
    override fun parsedValue() = AttributeFunctionConfig(backing)
}