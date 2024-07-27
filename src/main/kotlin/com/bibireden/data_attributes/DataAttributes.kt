package com.bibireden.data_attributes

import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonElement
import blue.endless.jankson.JsonObject
import blue.endless.jankson.JsonPrimitive
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents
import com.bibireden.data_attributes.config.*
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.functions.AttributeFunctionConfig
import com.bibireden.data_attributes.config.models.DataAttributesConfig
import com.bibireden.data_attributes.config.models.EntityTypesConfig
import com.bibireden.data_attributes.config.models.FunctionsConfig
import com.bibireden.data_attributes.config.models.OverridesConfig
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
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DataAttributes : ModInitializer {
    companion object {
        const val MOD_ID = "data_attributes"

        @JvmField val LOGGER: Logger = LogManager.getLogger()

        @JvmField val CONFIG: DataAttributesConfig = DataAttributesConfig.createAndLoad()
        @JvmField val OVERRIDES_CONFIG: OverridesConfig = OverridesConfig.createAndLoad()
        @JvmField val FUNCTIONS_CONFIG: FunctionsConfig = FunctionsConfig.createAndLoad { builder ->
            builder.registerSerializer(AttributeFunctionConfig::class.java) { cfg, marshaller ->
                marshaller.serialize(cfg.data)
            }
            builder.registerDeserializer(JsonObject::class.java, AttributeFunctionConfig::class.java) { obj, marshaller ->
                AttributeFunctionConfig(marshaller.marshall<Map<String, JsonArray>>(Map::class.java, obj).entries
                    .associate { (id, array) ->
                        Identifier.tryParse(id)!! to array.map { v -> marshaller.marshall(AttributeFunction::class.java, v) }
                    }
                )
            }
        }
        @JvmField val ENTITY_TYPES_CONFIG: EntityTypesConfig = EntityTypesConfig.createAndLoad {
            it.registerSerializer(EntityTypeData::class.java) { cfg, marshaller -> marshaller.serialize(cfg.data) }
            it.registerDeserializer(JsonObject::class.java, EntityTypeData::class.java) { json, marshaller ->
                EntityTypeData(
                    marshaller.marshall<Map<String, JsonPrimitive>>(Map::class.java, json)
                        .entries.associate { (k, v) -> Identifier.tryParse(k)!! to marshaller.marshallCarefully(Double::class.java, v) }
                )
            }
        }


        val MANAGER = AttributeConfigManager()

        /** Creates an [Identifier] associated with the [MOD_ID]. */
        fun id(str: String) = Identifier.of(MOD_ID, str)!!

        /** Acquires the proper manager based on the world. */
        fun getManagerFromWorld(world: World) = if (world.isClient) DataAttributesClient.MANAGER else MANAGER

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

        ServerLoginConnectionEvents.INIT.register { _, _ -> if (CONFIG.applyOnWorldStart) MANAGER.update() }
        ServerLoginConnectionEvents.QUERY_START.register { _, _, sender, _ ->
            sender.sendPacket(Channels.HANDSHAKE, AttributeConfigManager.Packet.ENDEC.encodeFully({ ByteBufSerializer.of(PacketByteBufs.create()) }, MANAGER.toPacket()))
        }

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, current, _, _ ->
            if (current is LivingEntity) current.refreshAttributes()
        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> player.refreshAttributes() }

        EntityAttributeModifiedEvents.MODIFIED.register { attribute, entity, _, _, _ ->
            if (entity?.world == null) return@register // no entity & no world, skip

            if (!entity.world.isClient) {
                if (attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
                    entity.health = attribute.clamp(entity.health.toDouble()).toFloat()
                }
            }
        }
    }
}