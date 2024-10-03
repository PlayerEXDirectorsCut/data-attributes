package com.bibireden.data_attributes.ui.components.fields

import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.jetbrains.annotations.ApiStatus
import java.util.function.Consumer

typealias ColorPredicate<T> = (T) -> Boolean

// todo: add parser on EditFieldComponent
@ApiStatus.Internal
class EditFieldComponent(private val onConfirmation: Consumer<EditFieldComponent>, private val onDenial: Consumer<EditFieldComponent>) : FlowLayout(Sizing.fill(70), Sizing.fill(5), Algorithm.HORIZONTAL) {
    val textBox: ParsedTextBoxComponent<Identifier>

    init {
        verticalAlignment(VerticalAlignment.CENTER)

        this.textBox = ParsedTextBoxComponent(Identifier::tryParse, Sizing.fill(60))
            .apply { verticalSizing(Sizing.fixed(12)) }
            .also(::child)

        child(Components.button(Text.translatable("text.config.data_attributes.data_entry.yes")) { onConfirmation.accept(this) }
            .renderer(ButtonRenderers.STANDARD)
        )
        child(Components.button(Text.translatable("text.config.data_attributes.data_entry.no")) { onDenial.accept(this) }
            .renderer(ButtonRenderers.STANDARD)
        )
    }
}