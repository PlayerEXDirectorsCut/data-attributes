package com.bibireden.data_attributes

import com.mojang.logging.LogUtils
import org.slf4j.Logger

object DataAttributes {
    /** The mod id for data_attributes. */
    const val MOD_ID: String = "data_attributes"

    /** The logger for data_attributes. */
    val LOGGER: Logger = LogUtils.getLogger()

    /** Initializes the mod. */
    @JvmStatic
    fun init() {
        LOGGER.info("...")
    }
}
