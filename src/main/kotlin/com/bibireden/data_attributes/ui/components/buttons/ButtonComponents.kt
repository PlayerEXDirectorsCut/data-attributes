package com.bibireden.data_attributes.ui.components.buttons

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import net.minecraft.text.Text
import java.util.function.Consumer

object ButtonComponents {
    fun remove(onPress: Consumer<ButtonComponent>) = Components.button(Text.translatable("text.config.data_attributes.data_entry.remove"), onPress)
}