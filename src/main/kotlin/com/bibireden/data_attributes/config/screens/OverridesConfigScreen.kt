package com.bibireden.data_attributes.config.screens

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.DataAttributesConfigScreen
import net.minecraft.client.gui.screen.Screen

class OverridesConfigScreen(parent: Screen?): DataAttributesConfigScreen(parent, DataAttributes.OVERRIDES_CONFIG) {
    init {
        this.extraFactories.put({ it.backingField().field.name.equals("overrides") }, ATTRIBUTE_OVERRIDE_FACTORY)
    }
}