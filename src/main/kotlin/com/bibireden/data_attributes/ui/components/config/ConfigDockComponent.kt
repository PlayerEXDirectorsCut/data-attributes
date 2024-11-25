package com.bibireden.data_attributes.ui.components.config

import com.bibireden.data_attributes.ui.components.buttons.ButtonComponents
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
@Suppress("UnstableApiUsage")
class ConfigDockComponent(private val defaultProperties: ConfigDefaultProperties) : FlowLayout(Sizing.fill(100), Sizing.fixed(15), Algorithm.HORIZONTAL) {
    class ConfigDefaultProperties(val onRemoveToggled: (ConfigDockComponent, ButtonComponent) -> Unit, val onEditToggled: (ConfigDockComponent, ButtonComponent) -> Unit)

    init {
        verticalAlignment(VerticalAlignment.BOTTOM)
        gap(10)
        id("dock")

        child(ButtonComponents.remove { defaultProperties.onRemoveToggled(this, it) }
            .renderer(ButtonRenderers.STANDARD))
        child(Components.button(Text.translatable("text.config.data_attributes.data_entry.edit")) { defaultProperties.onEditToggled(this, it) }
            .renderer(ButtonRenderers.STANDARD))
    }
}