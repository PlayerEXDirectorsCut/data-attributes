package com.bibireden.data_attributes.config.screens

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.DataAttributesConfigScreen
import net.minecraft.client.gui.screen.Screen

class FunctionsConfigScreen(parent: Screen?): DataAttributesConfigScreen(parent, DataAttributes.FUNCTIONS_CONFIG) {
    init {
        this.extraFactories.put({ it.backingField().field.name.equals("functions") }, ATTRIBUTE_FUNCTIONS_FACTORY)
    }
}