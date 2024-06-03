package com.bibireden.data_attributes.data.merged

import io.wispforest.endec.Endec

class EntityTypes(values: Map<String, Map<String, Double>>) : DataMerger<Double>(values) {
    companion object {
        @JvmField
        val ENDEC: Endec<EntityTypes> = Endec.DOUBLE.mapOf().mapOf().xmap(::EntityTypes, EntityTypes::values)
    }
}