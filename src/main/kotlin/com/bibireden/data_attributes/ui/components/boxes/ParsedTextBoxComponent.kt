package com.bibireden.data_attributes.ui.components.boxes

import com.bibireden.data_attributes.api.parser.Parser
import com.bibireden.data_attributes.ui.colors.ColorCodes
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.core.Sizing
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class ParsedTextBoxComponent<T>(val parser: Parser<String, T>, horizontalSizing: Sizing?) : TextBoxComponent(horizontalSizing) {
    var validColor = ColorCodes.GREEN
    var invalidColor = ColorCodes.TAN

    private var parsed: T? = null

    var predicate: ((T) -> Boolean)? = null

    fun validate(onSuccess: ((T) -> Unit)? = null): Boolean = parsed
        .let { z -> z != null && predicate.let { it == null || it(z) }
        .also { if (it && onSuccess != null) onSuccess(z)  } }

    init {
        setMaxLength(500)
        textValue.observe {
            parsed = parser(it)
            if (!validate { setEditableColor(validColor) }) {
                setEditableColor(invalidColor)
            }
        }
    }
}