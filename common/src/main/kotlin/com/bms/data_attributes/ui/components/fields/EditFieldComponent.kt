package com.bms.data_attributes.ui.components.fields

import com.bms.data_attributes.api.parser.Parser
import com.bms.data_attributes.ui.colors.ColorCodes
import com.bms.data_attributes.ui.components.boxes.ParsedTextBoxComponent
import com.bms.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.ApiStatus
import org.lwjgl.glfw.GLFW

typealias EditFieldDecision<T> = (T, EditFieldComponent<T>) -> Unit
typealias EditFieldCancellation<T> = (EditFieldComponent<T>) -> Unit

@ApiStatus.Internal
class EditFieldComponent<A>(parser: Parser<String, A>, private val onConfirmation: EditFieldDecision<A>, private val onCancel: EditFieldCancellation<A>? = null, private val autocomplete: Collection<A>? = null) : FlowLayout(Sizing.fill(70), Sizing.content(), Algorithm.VERTICAL) {
    val textBox: ParsedTextBoxComponent<A>

    private val choices = mutableListOf<A>()

    init {
        padding(Insets.vertical(2))

        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content(2)).also { hf ->
            hf.verticalAlignment(VerticalAlignment.CENTER)

            this.textBox = ParsedTextBoxComponent(parser, Sizing.fill(60))
                .apply { verticalSizing(Sizing.fixed(12)) }
                .also(hf::child)

            hf.child(Components.button(Component.translatable("text.config.data_attributes.data_entry.yes")) {
                textBox.validate {
                    this.remove()
                    onConfirmation(it, this)
                }
            }
                .renderer(ButtonRenderers.STANDARD)
            )
            hf.child(Components.button(Component.translatable("text.config.data_attributes.data_entry.no")) {
                this.remove()
                onCancel?.let { it(this) }
            }
                .renderer(ButtonRenderers.STANDARD)
            )
        })

        if (autocomplete != null) {
            child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).also { sl ->
                sl.gap(2)
                sl.clearChildren()

                textBox.onChanged().subscribe { txt ->
                    sl.clearChildren()
                    choices.clear()

                    if (txt.isEmpty()) return@subscribe

                    for (k in autocomplete) {
                        if (k.toString().contains(txt)) {
                            choices.add(k)
                            sl.child(Components.label(Component.literal(k.toString()).setStyle(Style.EMPTY.withColor(ColorCodes.TAN))))
                        }
                    }
                }

                textBox.keyPress().subscribe { code, _, _ ->
                    if (choices.isNotEmpty() && code == GLFW.GLFW_KEY_ENTER) {
                        textBox.text(choices.first().toString())
                    }
                    true
                }
            })
        }
    }
}