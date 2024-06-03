package com.bibireden.data_attributes.data

class FunctionData(values: Map<String, Map<String, AttributeFunction>>) : DataMerger<AttributeFunction>(values) {
    companion object {
        val endec = AttributeFunction.endec.mapOf().mapOf().xmap(::FunctionData) { it.values }
    }
}