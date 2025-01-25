package com.bms.data_attributes.ui.components.fields

import net.minecraft.resources.ResourceLocation

object FieldComponents {
    fun identifier(onConfirmation: EditFieldDecision<ResourceLocation>, onCancel: EditFieldCancellation<ResourceLocation>? = null, autocomplete: Collection<ResourceLocation>? = null): EditFieldComponent<ResourceLocation> {
        return EditFieldComponent(ResourceLocation::tryParse, onConfirmation, onCancel, autocomplete)
    }
}