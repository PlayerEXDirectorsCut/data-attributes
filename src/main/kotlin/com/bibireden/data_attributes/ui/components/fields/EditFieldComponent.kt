package com.bibireden.data_attributes.ui.components.fields

import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text
import java.util.function.Consumer

class EditFieldComponent(private val predicate: (EditFieldComponent, String) -> Boolean, private val onConfirmation: Consumer<EditFieldComponent>, private val onDenial: Consumer<EditFieldComponent>) : FlowLayout(Sizing.fill(70), Sizing.fill(5), Algorithm.HORIZONTAL) {
    val textBox: TextBoxComponent

    init {
        val ref = this

        verticalAlignment(VerticalAlignment.CENTER)

        this.textBox = Components.textBox(Sizing.fill(60))
            .apply {
                setEditableColor(0xf2e1c0)
                setTextPredicate { predicate(ref, it) }
                verticalSizing(Sizing.fixed(12))
            }
        child(textBox)
        child(Components.button(Text.translatable("text.config.data_attributes.data_entry.yes")) { onConfirmation.accept(this) }
            .renderer(ButtonRenderers.STANDARD)
        )
        child(Components.button(Text.translatable("text.config.data_attributes.data_entry.no")) { onDenial.accept(this) }
            .renderer(ButtonRenderers.STANDARD)
        )
    }
}