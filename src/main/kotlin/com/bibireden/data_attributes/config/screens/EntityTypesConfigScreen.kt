package com.bibireden.data_attributes.config.screens

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.DataAttributesConfigScreen
import net.minecraft.client.gui.screen.Screen

class EntityTypesConfigScreen(parent: Screen?): DataAttributesConfigScreen(parent, DataAttributes.ENTITY_TYPES_CONFIG) {
    init {
//        this.extraFactories.put({ it.backingField().field.name.equals("entity_types") }, ENTITY_TYPES_FACTORY)
    }
}