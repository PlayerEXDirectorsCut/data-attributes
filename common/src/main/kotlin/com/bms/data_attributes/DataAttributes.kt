package com.bms.data_attributes

import com.bms.data_attributes.config.impl.AttributeConfigManager
import com.bms.data_attributes.config.models.DataAttributesConfig
import com.bms.data_attributes.config.models.EntityTypesConfig
import com.bms.data_attributes.config.models.FunctionsConfig
import com.bms.data_attributes.config.models.OverridesConfig
import com.bms.data_attributes.networking.ConfigPacketBufs
import com.bms.data_attributes.serde.JanksonBuilders
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DataAttributes {
    companion object {
        const val MOD_ID = "data_attributes"

        @JvmField val LOGGER: Logger = LogManager.getLogger()

        @JvmField val CONFIG: DataAttributesConfig = DataAttributesConfig.createAndLoad()
        @JvmField val OVERRIDES_CONFIG: OverridesConfig = OverridesConfig.createAndLoad()
        @JvmField val FUNCTIONS_CONFIG: FunctionsConfig = FunctionsConfig.createAndLoad(JanksonBuilders::applyFunctions)
        @JvmField val ENTITY_TYPES_CONFIG: EntityTypesConfig = EntityTypesConfig.createAndLoad(JanksonBuilders::applyEntityTypes)


        val MANAGER = AttributeConfigManager()

        /** Creates an [ResourceLocation] associated with the [MOD_ID]. */
        fun id(str: String) = ResourceLocation.tryBuild(MOD_ID, str)!!

        /** Acquires the proper manager based on the level. */
        fun getManagerFromLevel(world: Level) = if (world.isClientSide) DataAttributesClient.MANAGER else MANAGER

        /** Reload all the data-attributes configs at once. */
        @JvmStatic
        fun reloadConfigs() {
            OVERRIDES_CONFIG.load()
            FUNCTIONS_CONFIG.load()
            ENTITY_TYPES_CONFIG.load()
            CONFIG.load()
        }

        /** Save all the data-attributes configs at once. */
        @JvmStatic
        fun saveConfigs() {
            OVERRIDES_CONFIG.save()
            FUNCTIONS_CONFIG.save()
            ENTITY_TYPES_CONFIG.save()
            CONFIG.save()
        }

        /**
         * Initiates a reload of config and default data for the server's [AttributeConfigManager].
         * Changes are then networked to the client.
         * */
        @JvmStatic
        fun reload() {
            reloadConfigs()

            MANAGER.update()
            MANAGER.nextUpdateFlag()
        }

        init {
            ConfigPacketBufs.registerPacketSerializers()
        }

        fun init() {
        }
    }
}