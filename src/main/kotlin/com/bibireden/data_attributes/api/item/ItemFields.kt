package com.bibireden.data_attributes.api.item

import net.minecraft.item.Item

class ItemFields : Item(Item.Settings()) {
    companion object {
        fun attackDamageModifierID() = ATTACK_DAMAGE_MODIFIER_ID
        fun attackSpeedModifierID() = ATTACK_SPEED_MODIFIER_ID
    }
}