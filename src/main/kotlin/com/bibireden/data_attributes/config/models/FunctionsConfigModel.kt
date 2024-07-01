package com.bibireden.data_attributes.config.models

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.data.AttributeFunctionConfig
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Hook
import io.wispforest.owo.config.annotation.SectionHeader
import io.wispforest.owo.config.annotation.Sync

@Suppress("UNUSED")
@Config(name = "${DataAttributes.MOD_ID}/functions", wrapperName = "DataAttributesFunctionsConfig")
@Sync(SyncMode.NONE)
class FunctionsConfigModel {
    @SectionHeader("functions")

    @JvmField
    @Hook
    var functions: AttributeFunctionConfig = AttributeFunctionConfig()
}