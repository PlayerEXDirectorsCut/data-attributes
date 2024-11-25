package com.bibireden.data_attributes.config

data class ConfigUIState(val collapsible: Collapsible = Collapsible()) {
    data class Collapsible(
        val overrides: MutableMap<String, Boolean> = mutableMapOf(),
        val functionParents: MutableMap<String, Boolean> = mutableMapOf(),
        val functionChildren: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf(),
        val entityTypeHeaders: MutableMap<String, Boolean> = mutableMapOf(),
        val entityTypeEntries: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf()
    )
}