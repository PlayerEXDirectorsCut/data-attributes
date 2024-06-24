package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.DataAttributes
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Sync

@Suppress("UNUSED")
@Config(name = DataAttributes.MOD_ID, wrapperName = "DataAttributesConfig")
@Sync(SyncMode.NONE)
class DataAttributesConfigModel