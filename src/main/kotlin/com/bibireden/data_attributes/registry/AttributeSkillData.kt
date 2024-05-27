package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.api.attribute.DynamicSkillAttribute

data class AttributeSkillData(private val value: Double) : DynamicSkillAttribute {
    override fun value(): Double = this.value
}
