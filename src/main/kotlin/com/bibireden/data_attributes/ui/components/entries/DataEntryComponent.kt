package com.bibireden.data_attributes.ui.components.entries

import com.bibireden.data_attributes.api.parser.Parser
import com.bibireden.data_attributes.ui.colors.ColorCodes
import com.bibireden.data_attributes.ui.components.boxes.ParsedTextBoxComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.CursorStyle
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text
import java.util.function.Consumer

class DataEntryComponent<V>(label: Text, val parser: Parser<String, V>, properties: Properties<V>? = null, private val suggestion: String? = null, hs: Sizing? = Sizing.fill(100), vs: Sizing? = Sizing.fixed(20)) : FlowLayout(hs, vs, Algorithm.HORIZONTAL) {
    data class Properties<V>(
        val onChanged: Consumer<V>,
        val predicate: (V) -> Boolean = { true },
    )

    private val isChangeable = properties != null

    val textbox: ParsedTextBoxComponent<V> = ParsedTextBoxComponent(parser, Sizing.fill(30))
        .apply {
            validColor = ColorCodes.BEE_YELLOW
            invalidColor = ColorCodes.RED

            setUneditableColor(ColorCodes.BEE_BLACK)

            verticalSizing(Sizing.fixed(16))
            positioning(Positioning.relative(100, 50))
        }

    private fun update() {
        textbox.setEditable(isChangeable)
        textbox.setPlaceholder(Text.of(suggestion))

        if (isChangeable) {
            textbox.cursorStyle(CursorStyle.TEXT)
            textbox.text = suggestion.toString()
        }
        else {
            textbox.tooltip(Text.translatable("text.config.data_attributes.data_entry.unchangeable"))
            textbox.cursorStyle(CursorStyle.POINTER)
            textbox.tooltip(null)
            textbox.text = ""
        }
    }

    init {
        verticalAlignment(VerticalAlignment.CENTER)
        gap(6)

        child(Components.label(label).sizing(Sizing.content(), Sizing.fixed(20)))

        child(textbox)
        
        update()

        if (properties != null) {
            textbox.onChanged().subscribe { txt ->
                val parsed = parser(txt)

                textbox.setEditableColor(if (parsed != null) ColorCodes.TAN else ColorCodes.RED)

                if (parsed != null && properties.predicate(parsed)) {
                    properties.onChanged.accept(parsed)
                }
            }
        }
    }
}