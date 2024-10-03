package com.bibireden.data_attributes.ui.components.boxes

import com.bibireden.data_attributes.ui.colors.ColorCodes
import com.bibireden.data_attributes.ui.components.fields.ColorPredicate
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.core.Sizing
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class ParsedTextBoxComponent<T>(val parser: (String) -> T?, horizontalSizing: Sizing?) : TextBoxComponent(horizontalSizing) {
    var defaultColor = ColorCodes.TAN
    private val colorConditions: MutableList<Pair<Int, ColorPredicate<T>>> = mutableListOf()

    fun parse(): T? = parser(text)

    init {
        textValue.observe { text ->
            val parsed = parser(text)
            if (parsed != null) {
                for ((color, predicate) in colorConditions) {
                    if (predicate(parsed)) {
                        setEditableColor(color)
                        return@observe
                    }
                }
            }
            setEditableColor(defaultColor)
        }
    }

    /** Adds to the predicate what color to change the field to. */
    fun addColorCondition(color: Int, condition: ColorPredicate<T>): ParsedTextBoxComponent<T> {
        colorConditions.add(Pair(color, condition))
        return this
    }
}