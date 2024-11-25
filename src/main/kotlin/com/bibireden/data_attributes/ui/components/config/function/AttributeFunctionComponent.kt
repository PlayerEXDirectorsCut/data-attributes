package com.bibireden.data_attributes.ui.components.config.function

import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.config.DataAttributesConfigProviders.registryEntryToText
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.ui.components.buttons.ButtonComponents
import com.bibireden.data_attributes.ui.components.config.AttributeConfigComponent
import com.bibireden.data_attributes.ui.components.config.AttributeOverrideComponent
import com.bibireden.data_attributes.ui.components.config.ConfigDockComponent
import com.bibireden.data_attributes.ui.components.config.entities.EntityTypesComponent
import com.bibireden.data_attributes.ui.components.entries.DataEntryComponent
import com.bibireden.data_attributes.ui.components.entries.EntryComponents
import com.bibireden.data_attributes.ui.components.fields.FieldComponents
import com.bibireden.data_attributes.ui.config.providers.AttributeFunctionProvider
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigToggleButton
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.concurrent.Flow

class AttributeFunctionComponent(override var identifier: Identifier, private var function: AttributeFunction, private var parentId: Identifier, private val provider: AttributeFunctionProvider)
    : CollapsibleContainer(Sizing.content(), Sizing.content(), Text.of("<n/a>"), DataAttributesClient.UI_STATE.collapsible.functionChildren[parentId.toString()]?.get(identifier.toString()) ?: true),
    AttributeConfigComponent<MutableEntityAttribute>
{
    @Suppress("UNCHECKED_CAST")
    override val registry: Registry<MutableEntityAttribute> = Registries.ATTRIBUTE as Registry<MutableEntityAttribute>

    private val backing = provider.backing

    override val isDefault get() = provider.backing[parentId]?.get(identifier) == null

    private fun updateEntryToBacking(id: Identifier, function: AttributeFunction) {
        backing.computeIfAbsent(parentId) { mutableMapOf() }[id] = function
        update()
    }

    private fun updateSearchAnchor() {
        childById(SearchAnchorComponent::class.java, "search-anchor")?.remove()
        child(SearchAnchorComponent(titleLayout(), Option.Key.ROOT, { identifier.toString() }, { Text.translatable(identifier.toTranslationKey()).toString() }).id("search-anchor"))
    }

    private fun updateTextLabel() {
        titleLayout().children().filterIsInstance<LabelComponent>().first().text(registryEntryToText(identifier, Registries.ATTRIBUTE, { it.translationKey }, isDefault))
    }

    private val toggleButton = ConfigToggleButton().enabled(function.enabled).also {
        it.onPress { updateEntryToBacking(identifier, function.copy(enabled = !function.enabled)) }
        it.renderer(ButtonRenderers.STANDARD)
    }

    private val valueEntry: DataEntryComponent<Double> = EntryComponents.double(Text.translatable("text.config.data_attributes.data_entry.functions.value"), DataEntryComponent.Properties({ updateEntryToBacking(identifier, function.copy(value = it)) }), function.value.toString()).also {
        if (registryEntry is ClampedEntityAttribute) {
            val clamped = registryEntry as ClampedEntityAttribute
            it.tooltip(Text.translatable("text.config.data_attributes.data_entry.function_child", identifier, clamped.minValue, clamped.maxValue))
        }
    }

    private val stackingBehaviorButton = Components.button(Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")) { updateStackingBehavior(StackingBehavior.entries[function.behavior.ordinal xor 1]) }
        .apply {
            renderer(ButtonRenderers.STANDARD)
            positioning(Positioning.relative(100, 0)).horizontalSizing(Sizing.fixed(65))
        }

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

    fun updateParent(parentId: Identifier) { this.parentId = parentId }

    private fun updateStackingBehavior(to: StackingBehavior) {
        function.behavior = to
        stackingBehaviorButton.message = Text.translatable("text.config.data_attributes.enum.functionBehavior.${function.behavior.name.lowercase()}")
        updateEntryToBacking(identifier, function.copy(behavior = function.behavior))
    }

    init {
        onToggled().subscribe { DataAttributesClient.UI_STATE.collapsible.functionChildren.computeIfAbsent(parentId.toString()) { mutableMapOf() }[identifier.toString()] = it }

        child(ConfigDockComponent(
            ConfigDockComponent.ConfigDefaultProperties({ _, _ ->
                val defaultEntry = DataAttributesAPI.serverManager.defaults.functions.entries[parentId]?.get(identifier)
                if (defaultEntry != null) {
                    toggleButton.enabled(defaultEntry.enabled)
                    valueEntry.textbox.text = defaultEntry.value.toString()
                    updateStackingBehavior(defaultEntry.behavior)
                }
                else remove()

                backing[parentId]?.remove(identifier) != null

                update()
            })
            { dc, _ ->
                if (dc.childById(FlowLayout::class.java, "edit-field") == null && backing[parentId]?.get(identifier) != null) {
                    val field = FieldComponents.identifier(
                        { newId, _ ->
                            val entry = backing.computeIfAbsent(parentId) { mutableMapOf() }
                            if (!registry.containsId(newId) || entry[newId] != null) return@identifier

                            entry[newId] = entry.remove(identifier) ?: return@identifier
                            backing[parentId] = entry

                            identifier = newId

                            for (fc in children().filterIsInstance<AttributeFunctionComponent>()) { fc.updateParent(identifier) }

                            update()
                        },
                        autocomplete = Registries.ATTRIBUTE.ids
                    )

                    field.textBox.predicate = { backing[parentId]?.get(it) == null && Registries.ATTRIBUTE.containsId(it) }

                    child(0, field)
                }
            }
        ).apply {
            child(0, toggleButton)
        })

       valueEntry.also(::child)

        child(
            Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).apply {
                verticalAlignment(VerticalAlignment.CENTER)

                child(Components.label(Text.translatable("text.config.data_attributes.data_entry.functions.behavior")).sizing(Sizing.content(), Sizing.fixed(20)))
                child(stackingBehaviorButton)
            }
        )

        update()
    }
}