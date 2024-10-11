package com.bibireden.data_attributes.ui.components.fields

import net.minecraft.util.Identifier

object FieldComponents {
    fun identifier(onConfirmation: EditFieldDecision<Identifier>, onCancel: EditFieldCancellation<Identifier>? = null, autocomplete: Collection<Identifier>? = null): EditFieldComponent<Identifier> {
        return EditFieldComponent(Identifier::tryParse, onConfirmation, onCancel, autocomplete)
    }
}