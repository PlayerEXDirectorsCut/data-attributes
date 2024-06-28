package com.bibireden.data_attributes

import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents
import com.bibireden.data_attributes.config.*
import com.bibireden.data_attributes.config.models.DataAttributesConfig
import com.bibireden.data_attributes.config.models.DataAttributesEntityTypesConfig
import com.bibireden.data_attributes.config.models.DataAttributesFunctionsConfig
import com.bibireden.data_attributes.config.models.DataAttributesOverridesConfig
import com.bibireden.data_attributes.data.AttributeFunctionConfig
import com.bibireden.data_attributes.data.AttributeFunctionConfigData
import com.bibireden.data_attributes.data.EntityTypeData
import com.bibireden.data_attributes.ext.refreshAttributes
import com.bibireden.data_attributes.networking.Channels
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DataAttributes : ModInitializer {
    companion object {
        const val MOD_ID = "data_attributes"

        @JvmField val LOGGER: Logger = LogManager.getLogger()

        @JvmField val SERVER_MANAGER = AttributeConfigManager()

        @JvmField val CONFIG: DataAttributesConfig = DataAttributesConfig.createAndLoad()
        @JvmField val OVERRIDES_CONFIG: DataAttributesOverridesConfig = DataAttributesOverridesConfig.createAndLoad()
        @JvmField val FUNCTIONS_CONFIG: DataAttributesFunctionsConfig = DataAttributesFunctionsConfig.createAndLoad { builder ->
            builder.registerSerializer(AttributeFunctionConfigData::class.java) { dat, marshaller -> marshaller.serialize(dat.data) }
            builder.registerDeserializer(JsonObject::class.java, AttributeFunctionConfigData::class.java) { obj, marshaller ->
                val unmapped = marshaller.marshall(Map::class.java, obj)
                val mapped = mutableMapOf<Identifier, List<AttributeFunctionConfig>>()

                unmapped.forEach { (key, array) ->
                    if (key !is String) return@forEach
                    if (array !is JsonArray) return@forEach

                    val listing = mutableListOf<AttributeFunctionConfig>()

                    array.forEach { value ->
                        val ls = marshaller.marshallCarefully(AttributeFunctionConfig::class.java, value)
                        listing.add(ls)
                    }

                    mapped[Identifier(key)] = listing
                }
                AttributeFunctionConfigData(mapped)
            }
        }
        @JvmField val ENTITY_TYPES_CONFIG: DataAttributesEntityTypesConfig = DataAttributesEntityTypesConfig.createAndLoad { builder ->
            builder.registerSerializer(EntityTypeData::class.java) { dat, marshaller -> marshaller.serialize(dat.data) }
            builder.registerDeserializer(JsonObject::class.java, EntityTypeData::class.java) { des, marshaller ->
                EntityTypeData(des.map { (id, value) -> Identifier(id) to marshaller.marshall(Double::class.java, value) }.toMap().toMutableMap())
            }
        }

        fun id(str: String) = Identifier.of(MOD_ID, str)!!

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

        init {
            ConfigPacketBufs.registerPacketSerializers()
        }
    }

    override fun onInitialize() {
        ServerLoginNetworking.registerGlobalReceiver(Channels.HANDSHAKE) { _, _, _, _, _, _ -> }

        ServerLoginConnectionEvents.QUERY_START.register { _, _, sender, _ ->
            sender.sendPacket(Channels.HANDSHAKE, AttributeConfigManager.Packet.ENDEC.encodeFully({ ByteBufSerializer.of(PacketByteBufs.create()) }, SERVER_MANAGER.toPacket()))
        }

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, newEntity, _, _ ->
            if (newEntity is LivingEntity) newEntity.refreshAttributes()
        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> player.refreshAttributes() }

        EntityAttributeModifiedEvents.MODIFIED.register { attribute, entity, _, _, _ ->
            if (attribute == EntityAttributes.GENERIC_MAX_HEALTH && entity?.world?.isClient == false) {
                entity.health = MathHelper.clamp(entity.health, 0.0F, entity.maxHealth)
            }
        }
    }
}