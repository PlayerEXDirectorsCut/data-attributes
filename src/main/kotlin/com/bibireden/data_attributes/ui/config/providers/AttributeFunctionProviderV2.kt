package com.bibireden.data_attributes.ui.config.providers

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.attributeIdToText
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.textBoxComponent
import com.bibireden.data_attributes.config.Validators
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.functions.AttributeFunctionConfig
import com.bibireden.data_attributes.ui.components.CollapsibleFoldableContainer
import com.bibireden.data_attributes.ui.components.fields.EditFieldComponent
import com.bibireden.data_attributes.ui.components.RemoveButtonComponent
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
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

class AttributeFunctionProviderV2(val option: Option<AttributeFunctionConfig>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    private val backing = option.value().data.toMutableMap()

    private val trackedEntryComponents: MutableMap<Identifier, MutableMap<Identifier, CollapsibleContainer>> = mutableMapOf()

    private fun insertFunctionEntry(entryId: Identifier, insertingFunction: AttributeFunction, functions: MutableList<AttributeFunction>, at: Int? = null) {
        if (at == null || at >= functions.size) functions.add(insertingFunction)
        else functions[at] = insertingFunction
        backing[entryId] = functions
    }

    private fun getOrCreateEntryContainer(id: Identifier, isDefault: Boolean): CollapsibleFoldableContainer {
        return childById(CollapsibleFoldableContainer::class.java, id.toString()) ?: CollapsibleFoldableContainer(Sizing.content(), Sizing.content(), attributeIdToText(id, isDefault), true).also { cf ->
            trackedEntryComponents[id] = mutableMapOf()
            cf.id(id.toString())

            if (!isDefault) {
                cf.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                    .apply {
                        verticalAlignment(VerticalAlignment.CENTER)
                        gap(10)
                        id("dock")
                    }
                    .also { fl ->
                        fl.child(RemoveButtonComponent { backing.remove(id); refreshAndDisplayAttributes() }
                            .renderer(ButtonRenderers.STANDARD))

                        fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                            if (cf.childById(FlowLayout::class.java, "edit-field") == null) {
                                val field = EditFieldComponent(
                                    { field, str ->
                                        val predId = Identifier.tryParse(str) ?: return@EditFieldComponent true
                                        if (backing.containsKey(predId)) {
                                            field.textBox.setEditableColor(0xe54d48)
                                        }
                                        else if (!Registries.ATTRIBUTE.containsId(predId)) {
                                            field.textBox.setEditableColor(0xf2e1c0)
                                        }
                                        else {
                                            field.textBox.setEditableColor(0x69d699)
                                        }
                                        true
                                    },
                                    {
                                        val newId = Identifier.tryParse(it.textBox.text.toString()) ?: return@EditFieldComponent
                                        if (backing.containsKey(newId)) return@EditFieldComponent
                                        // ensured that this exists and is possible to swap
                                        backing.remove(id)?.let { backing[newId] = it }
                                        refreshAndDisplayAttributes()
                                    },
                                    EditFieldComponent::remove
                                )
                                cf.child(0, field)
                            }
                        }
                            .renderer(ButtonRenderers.STANDARD)
                        )

                        fl.child(
                            Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                                backing[id]?.let {
                                    val list = it.toMutableList()
                                    list.add(AttributeFunction())
                                    backing[id] = list
                                }
                                refreshAndDisplayAttributes()
                            }
                                .renderer(ButtonRenderers.STANDARD)
                        )
                    }
                )
            }
            child(cf)
        }
    }

    private fun createFunctionEntry(function: AttributeFunction, parentId: Identifier, isDefault: Boolean, parent: CollapsibleFoldableContainer, index: Int? = null): CollapsibleContainer {
        // find if it exists already to ignore the default
        val located = parent.childById(CollapsibleContainer::class.java, "${function.id}#child-fn")
        if (located != null) return located

        val container = Containers.collapsible(Sizing.content(), Sizing.content(), attributeIdToText(function.id, isDefault), true).apply {
            gap(4)
            id("${function.id}#child-fn")

            val attribute = Registries.ATTRIBUTE[function.id]
            if (attribute == null) {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes.data_entry.invalid"))
            } else if (isDefault) {
                titleLayout().tooltip(Text.translatable("text.config.data_attributes_data_entry.default"))
            } else {
                if (attribute is ClampedEntityAttribute) {
                    tooltip(Text.translatable("text.config.data_attributes.data_entry.function_child", function.id, attribute.minValue, attribute.maxValue))
                }
            }

            if (!isDefault) {
                child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(15))
                    .apply {
                        verticalAlignment(VerticalAlignment.BOTTOM)
                        gap(10)
                    }
                    .also { fl ->
                        fl.child(RemoveButtonComponent { backing[parentId]?.let { backing[parentId] = it.filter { it.id != function.id } }; refreshAndDisplayAttributes() }
                            .renderer(ButtonRenderers.STANDARD))

                        fl.child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) {
                            if (fl.childById(FlowLayout::class.java, "edit-field") == null) {
                                val field = EditFieldComponent(
                                    { field, str ->
                                        val predId = Identifier.tryParse(str) ?: return@EditFieldComponent true
                                        if (backing[parentId]?.find { it.id == predId } != null) {
                                            field.textBox.setEditableColor(0xe54d48)
                                        }
                                        else if (!Registries.ATTRIBUTE.containsId(predId)) {
                                            field.textBox.setEditableColor(0xf2e1c0)
                                        }
                                        else {
                                            field.textBox.setEditableColor(0x69d699)
                                        }
                                        true
                                    },
                                    {
                                        val newId = Identifier.tryParse(it.textBox.text.toString()) ?: return@EditFieldComponent
                                        val newFunction = function.copy(id = newId)
                                        val currentList = backing[parentId]
                                        if (currentList?.find { it.id == newId } != null) return@EditFieldComponent
                                        // ensured that this exists and is possible to swap
                                        val list = currentList?.filter { it.id != function.id }?.toMutableList() ?: return@EditFieldComponent
                                        list.add(0, newFunction)
                                        backing[parentId] = list
                                        refreshAndDisplayAttributes()
                                    },
                                    EditFieldComponent::remove
                                )
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
                        backing[parentId] = (backing.remove(parentId)?.toMutableList() ?: mutableListOf()).apply {
                            insertFunctionEntry(parentId, function.copy(value = v), this, index)
                        }
                        refreshAndDisplayAttributes()
                    }
                }
            ))

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

                            backing[parentId] = (backing.remove(parentId)?.toMutableList() ?: mutableListOf()).apply {
                                insertFunctionEntry(parentId, function.copy(behavior = function.behavior), this, index)
                            }

                            refreshAndDisplayAttributes()
                        }
                            .renderer(ButtonRenderers.STANDARD)
                            .positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
                    )
                }
            )

            child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { function.id.toString() }, { Text.translatable(function.id.toTranslationKey()).toString() }))
        }

        if (index != null) parent.child(index, container) else parent.child(container)
        trackedEntryComponents[parentId]!![function.id] = container

        // force a rearrangement to bring the dock up top~
        parent.childById(FlowLayout::class.java, "dock")?.let {
            parent.removeChild(it)
            parent.child(0, it)
        }

        return container
    }

    private fun createFunctionEntries(functions: List<AttributeFunction>, parentId: Identifier, parent: CollapsibleFoldableContainer) {
        functions.forEachIndexed { index, function ->
            val isDefault = backing[parentId]?.find { it.id == function.id } == null
            createFunctionEntry(function, parentId, isDefault, parent, index)
        }
    }

    private fun refreshAndDisplayAttributes() {
        for ((key, map) in trackedEntryComponents) {
            for (container in map.values) {
                container.id("null")
                container.remove()
            }
            map.clear()

            // ensure cleanup of dead keys
            if (key !in backing) childById(CollapsibleContainer::class.java, key.toString())?.also {
                it.id("null")
                it.remove()
            }
        }

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
                backing[Identifier("unknown")] = listOf()
                refreshAndDisplayAttributes()
            }
                .renderer(ButtonRenderers.STANDARD)
                .horizontalSizing(Sizing.content())
                .verticalSizing(Sizing.fixed(20))
        )

        refreshAndDisplayAttributes()
    }

    override fun isValid() = !this.option.detached()
    override fun parsedValue() = AttributeFunctionConfig(backing)
}