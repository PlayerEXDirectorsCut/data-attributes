package com.bms.data_attributes.ui.components.config

import com.bms.data_attributes.config.models.OverridesConfigModel
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.SliderComponent
import io.wispforest.owo.ui.core.Sizing
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object AttributeConfigComponents {
    fun smoothnessSlider(override: OverridesConfigModel.AttributeOverride, onChanged: (SliderComponent) -> Unit): SliderComponent {
        return Components.discreteSlider(Sizing.fill(30), 0.001, 100.0)
            .apply {
                value(override.smoothness)
                onChanged().subscribe {
                    onChanged(this)
                }
            }
    }
}