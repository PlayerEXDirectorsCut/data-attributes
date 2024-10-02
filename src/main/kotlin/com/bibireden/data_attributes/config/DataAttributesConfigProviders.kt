package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.config.providers.AttributeEntityTypesProvider
import com.bibireden.data_attributes.config.providers.AttributeFunctionsProvider
import com.bibireden.data_attributes.config.providers.AttributeOverrideProvider
import com.bibireden.data_attributes.ui.colors.ColorCodes
import com.bibireden.data_attributes.ui.config.providers.AttributeFunctionProviderV2
import com.bibireden.data_attributes.ui.config.providers.AttributeOverrideProviderV2
import com.google.common.base.Predicate
import io.wispforest.owo.config.ui.OptionComponentFactory
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.registry.Registries
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object DataAttributesConfigProviders {
    fun entityTypeIdentifierToText(id: Identifier): MutableText {
        val type = Registries.ENTITY_TYPE[id]
        return Text.empty().apply {
            append(Text.translatable(type.translationKey).append(" ").setStyle(Style.EMPTY.withColor(ColorCodes.BEE_YELLOW)))
            append(Text.literal("($id)").setStyle(Style.EMPTY.withColor(ColorCodes.BEE_BLACK)))
        }
    }
    fun attributeIdToText(id: Identifier, default: Boolean = false): MutableText {
        val attribute = Registries.ATTRIBUTE[id]
        val text = Text.empty()
        if (attribute != null) {
            text.append(Text.translatable(attribute.translationKey).append(" ")).setStyle(Style.EMPTY.withColor(if (default) 0x84de56 else 0xE7C14B))
        }
        text.append(Text.literal("($id)").also { t ->
            t.setStyle(Style.EMPTY.withColor(if (attribute != null) ColorCodes.BEE_BLACK else ColorCodes.UNEDITABLE))
        })
        return text
    }
    fun isAttributeUnregistered(id: Identifier) = !Registries.ATTRIBUTE.containsId(id)

    val ATTRIBUTE_OVERRIDE_FACTORY = OptionComponentFactory { _, option ->
        return@OptionComponentFactory AttributeOverrideProviderV2(option).let { OptionComponentFactory.Result(it, it) }
    }

    val ATTRIBUTE_FUNCTIONS_FACTORY = OptionComponentFactory { _, option ->
        return@OptionComponentFactory AttributeFunctionProviderV2(option).let { OptionComponentFactory.Result(it, it) }
    }

    val ENTITY_TYPES_FACTORY = OptionComponentFactory { _, option ->
        return@OptionComponentFactory AttributeEntityTypesProvider(option).let { OptionComponentFactory.Result(it, it) }
    }

    fun textBoxComponent(txt: Text, obj: Any, predicate: Predicate<String>? = null, onChange: ((String) -> Unit)? = null, textBoxID: String? = null): FlowLayout {
        val isUnchangeable = onChange == null
        return Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).also { hf ->
            hf.verticalAlignment(VerticalAlignment.CENTER)
            hf.gap(6)
            hf.child(Components.label(txt).sizing(Sizing.content(), Sizing.fixed(20)))
            hf.child(
                Components.textBox(Sizing.fill(30)).also { tb ->
                    tb.verticalSizing(Sizing.fixed(16))
                    tb.setEditableColor(ColorCodes.BEE_YELLOW)
                    tb.setUneditableColor(ColorCodes.BEE_BLACK)
                    tb.setEditable(!isUnchangeable)
                    if (isUnchangeable) {
                        tb.setTextPredicate { false }
                        tb.cursorStyle(CursorStyle.POINTER)
                        tb.setSuggestion(obj.toString())
                        tb.tooltip(Text.translatable("text.config.data_attributes.data_entry.unchangeable"))
                    }
                    else {
                        tb.setPlaceholder(Text.literal(obj.toString()))
                        tb.text = obj.toString()
                    }
                    if (onChange != null) {
                        tb.setTextPredicate { predicate == null || predicate.apply(it) }
                        tb.onChanged().subscribe(onChange::invoke)
                    }
                    tb.id("text")
                }.positioning(Positioning.relative(100, 50)).id(textBoxID)
            )
        }
    }
}