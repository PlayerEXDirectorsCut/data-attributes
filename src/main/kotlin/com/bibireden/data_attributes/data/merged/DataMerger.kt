package com.bibireden.data_attributes.data.merged

/**
 * Meant to simplify merging a map's values into a greater set of values.
 * This basically flattens a map from one string key.
 */
abstract class DataMerger<T>(val values: Map<String, Map<String, T>>) {
    fun merge(obj: MutableMap<String, MutableMap<String, T>>) {
        for ((key, value) in this.values) {
            val inner = obj.getOrDefault(key, mutableMapOf())
            inner.putAll(value)
            obj[key] = inner
        }
    }
}