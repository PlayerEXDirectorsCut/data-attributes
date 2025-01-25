package com.bms.data_attributes.config

import com.bms.data_attributes.ui.colors.ColorCodes
import com.bms.data_attributes.ui.config.providers.AttributeFunctionProvider
import com.bms.data_attributes.ui.config.providers.AttributeOverrideProvider
import com.bms.data_attributes.ui.config.providers.EntityTypesProvider
import com.google.common.base.Predicate
import io.wispforest.owo.config.ui.OptionComponentFactory
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.core.Registry
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

object DataAttributesConfigProviders {
    fun <T> registryEntryToText(id: ResourceLocation, registry: Registry<T>, representation: (T) -> String, isDefault: Boolean = false): MutableComponent {
        val entry = registry[id]
        val text = Component.empty()
        if (entry != null) {
            text.append(Component.translatable(representation(entry)).append(" "))
                .setStyle(Style.EMPTY.withColor(if (isDefault) 0x84de56 else 0xE7C14B))
        }
        text.append(Component.literal("($id)").also { t ->
            t.setStyle(
                Style.EMPTY.withColor(
                    if (entry != null) ColorCodes.BEE_BLACK else ColorCodes.UNEDITABLE
                )
            )
        })
        return text
    }

    val ATTRIBUTE_OVERRIDE_FACTORY = OptionComponentFactory { _, option ->
        return@OptionComponentFactory AttributeOverrideProvider(option).let { OptionComponentFactory.Result(it, it) }
    }

    val ATTRIBUTE_FUNCTIONS_FACTORY = OptionComponentFactory { _, option ->
        return@OptionComponentFactory AttributeFunctionProvider(option).let { OptionComponentFactory.Result(it, it) }
    }

    val ENTITY_TYPES_FACTORY = OptionComponentFactory { _, option ->
        return@OptionComponentFactory EntityTypesProvider(option).let { OptionComponentFactory.Result(it, it) }
    }

    fun textBoxComponent(txt: Component, obj: Any, predicate: Predicate<String>? = null, onChange: ((String) -> Unit)? = null, textBoxID: String? = null): FlowLayout {
        val isUnchangeable = onChange == null
        return Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
            hf.verticalAlignment(VerticalAlignment.CENTER)
            hf.gap(6)
            hf.child(Components.label(txt).sizing(Sizing.content(), Sizing.fixed(20)))
            hf.child(
                Components.textBox(Sizing.fill(30)).also { tb ->
                    tb.verticalSizing(Sizing.fixed(16))
                    tb.setTextColor(ColorCodes.BEE_YELLOW)
                    tb.setTextColorUneditable(ColorCodes.BEE_BLACK)
                    tb.setEditable(!isUnchangeable)
                    if (isUnchangeable) {
                        tb.setFilter { false }
                        tb.cursorStyle(CursorStyle.POINTER)
                        tb.setSuggestion(obj.toString())
                        tb.tooltip(Component.translatable("text.config.data_attributes.data_entry.unchangeable"))
                    }
                    else {
                        tb.setHint(Component.literal(obj.toString()))
                        tb.text(obj.toString())
                    }
                    if (onChange != null) {
                        tb.setFilter { predicate == null || predicate.apply(it) }
                        tb.onChanged().subscribe(onChange::invoke)
                    }
                    tb.id("text")
                }.positioning(Positioning.relative(100, 50)).id(textBoxID)
            )
        }
    }
}