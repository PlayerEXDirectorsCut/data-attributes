package com.bms.data_attributes.config.models

import blue.endless.jankson.Comment
import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.config.functions.AttributeFunctionConfig
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Hook
import io.wispforest.owo.config.annotation.SectionHeader
import io.wispforest.owo.config.annotation.Sync

@Suppress("UNUSED")
@Config(name = "${DataAttributes.MOD_ID}/functions", wrapperName = "FunctionsConfig")
@Sync(SyncMode.NONE)
class FunctionsConfigModel {
    @SectionHeader("functions")

    @JvmField
    @Hook
    @Comment("attribute functions are able to compute child attributes based on an increment/decrement, or multiplication of a parent attribute.")
    var entries: AttributeFunctionConfig = AttributeFunctionConfig()
}