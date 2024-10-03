package com.bibireden.data_attributes.ui.components.fields

import com.bibireden.data_attributes.api.parser.Parser
import com.bibireden.data_attributes.ui.components.boxes.ParsedTextBoxComponent
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text
import org.jetbrains.annotations.ApiStatus

typealias ColorPredicate<T> = (T) -> Boolean
typealias EditFieldDecision<T> = (T, EditFieldComponent<T>) -> Unit
typealias EditFieldCancellation<T> = (EditFieldComponent<T>) -> Unit

// todo: add parser on EditFieldComponent
@ApiStatus.Internal
class EditFieldComponent<A>(parser: Parser<String, A>, private val onConfirmation: EditFieldDecision<A>, private val onCancel: EditFieldCancellation<A>?) : FlowLayout(Sizing.fill(70), Sizing.fill(5), Algorithm.HORIZONTAL) {
    val textBox: ParsedTextBoxComponent<A>

    init {
        verticalAlignment(VerticalAlignment.CENTER)

        this.textBox = ParsedTextBoxComponent(parser, Sizing.fill(60))
            .apply { verticalSizing(Sizing.fixed(12)) }
            .also(::child)

        child(Components.button(Text.translatable("text.config.data_attributes.data_entry.yes")) {
            textBox.parse()?.let { onConfirmation(it, this); this.remove() }
        }
            .renderer(ButtonRenderers.STANDARD)
        )
        child(Components.button(Text.translatable("text.config.data_attributes.data_entry.no")) {
            onCancel?.let { it(this) }
            this.remove()
        }
            .renderer(ButtonRenderers.STANDARD)
        )
    }
}