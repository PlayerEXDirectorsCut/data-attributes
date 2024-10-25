package com.bibireden.data_attributes.config

data class ConfigUIState(val collapsible: Collapsible = Collapsible()) {
    data class Collapsible(
        val overrides: MutableMap<String, Boolean> = mutableMapOf(),
        val functions: MutableMap<String, Boolean> = mutableMapOf(),
        val entityTypes: MutableMap<String, Boolean> = mutableMapOf()
    )
}