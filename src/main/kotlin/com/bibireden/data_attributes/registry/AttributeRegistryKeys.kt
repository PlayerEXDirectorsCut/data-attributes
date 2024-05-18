package com.bibireden.data_attributes.registry

import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

object AttributeRegistryKeys {
    val SKILLS = RegistryKey.ofRegistry<MutableMap<Identifier, AttributeOverrideData>>(Identifier("attributes", "skills"))
    val ENTITIES = RegistryKey.ofRegistry<GeneralAttributeDataType>(Identifier("attributes", "entities"))
    val OVERRIDES = RegistryKey.ofRegistry<GeneralAttributeDataType>(Identifier("attributes", "overrides"))
}