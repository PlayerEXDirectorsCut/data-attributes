package com.bibireden.data_attributes.ui.components

import io.wispforest.owo.ui.component.ButtonComponent
import net.minecraft.text.Text
import java.util.function.Consumer

class RemoveButtonComponent(onPress: Consumer<ButtonComponent>) : ButtonComponent(Text.translatable("text.config.data_attributes.data_entry.remove"), onPress)