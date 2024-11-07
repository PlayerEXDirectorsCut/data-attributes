package com.bibireden.data_attributes.ui.config.providers

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.ui.components.config.AttributeOverrideComponent
import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AttributeOverrideProvider(val option: Option<Map<Identifier, AttributeOverride>>) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL), OptionValueProvider {
    private val backing: MutableMap<Identifier, AttributeOverride> = option.value().toMutableMap()

    /** Construct single override entry that discerns defaults from config-based ones. */
    private fun createOverrideEntry(id: Identifier, override: AttributeOverride) {
        child(AttributeOverrideComponent(id, override, backing, this))
    }

    init {
        child(
            Components.button(Text.translatable("text.config.data_attributes.buttons.add")) {
                val identifier = Identifier("unknown")
                val override = AttributeOverride()
                backing[identifier] = override
                child(1, AttributeOverrideComponent(identifier, override, backing, this))
            }
                .renderer(ButtonRenderers.STANDARD)
                .horizontalSizing(Sizing.content())
                .verticalSizing(Sizing.fixed(20))
        )

        backing.forEach(::createOverrideEntry)
        DataAttributesAPI.serverManager.defaults.overrides.entries.forEach { (id, override) ->
            if (id !in backing) createOverrideEntry(id, override)
        }
    }

    override fun isValid() = !this.option.detached()

    override fun parsedValue() = backing
}