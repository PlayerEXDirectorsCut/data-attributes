package com.bibireden.data_attributes

import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents
import com.bibireden.data_attributes.config.AttributeConfigManager
import com.bibireden.data_attributes.config.DataAttributesConfig
import com.bibireden.data_attributes.config.DataAttributesEntityTypesConfig
import com.bibireden.data_attributes.config.DataAttributesFunctionsConfig
import com.bibireden.data_attributes.config.DataAttributesOverridesConfig
import com.bibireden.data_attributes.config.data.EntityTypesConfigData
import com.bibireden.data_attributes.data.AttributeFunctionConfig
import com.bibireden.data_attributes.data.AttributeFunctionConfigData
import com.bibireden.data_attributes.data.EntityTypeData
import com.bibireden.data_attributes.mutable.MutableAttributeContainer
import com.bibireden.data_attributes.networking.Channels
import com.bibireden.data_attributes.serde.PacketBufs
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking.LoginSynchronizer
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerLoginNetworkHandler
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

class DataAttributes : ModInitializer {
    companion object {
        const val MOD_ID = "data_attributes"

        val LOGGER = LogManager.getLogger()

        @JvmField var CLIENT_MANAGER = AttributeConfigManager()
        @JvmField val SERVER_MANAGER = AttributeConfigManager()

        init {
            PacketBufs.registerPacketSerializers()
        }

        fun id(str: String) = Identifier.of(MOD_ID, str)!!

        val CONFIG = DataAttributesConfig.createAndLoad()
        val OVERRIDES_CONFIG = DataAttributesOverridesConfig.createAndLoad()
        val FUNCTIONS_CONFIG = DataAttributesFunctionsConfig.createAndLoad({ builder ->
            builder.registerSerializer(AttributeFunctionConfigData::class.java) { dat, marshaller ->
                marshaller.serialize(dat.data)
            }
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
        })
        val ENTITY_TYPES_CONFIG = DataAttributesEntityTypesConfig.createAndLoad() { builder ->
            builder.registerSerializer(EntityTypeData::class.java) { data, marshaller ->
                marshaller.serialize(data.data)
            }
            builder.registerSerializer(EntityTypesConfigData::class.java) { data, marshaller ->
                marshaller.serialize(data.data)
            }
            builder.registerDeserializer(JsonObject::class.java, EntityTypesConfigData::class.java) { obj, marshaller ->
                val unmapped = marshaller.marshall(Map::class.java, obj)
                val mapped = mutableMapOf<Identifier, EntityTypeData>()

                unmapped.forEach { (key, obj) ->
                    if (key !is String) return@forEach
                    if (obj !is JsonObject) return@forEach

                    val mapping = mutableMapOf<Identifier, Double>()

                    obj.forEach { (id, value) ->
                        mapping[Identifier(id)] = marshaller.marshall(Double::class.java, value)
                    }

                    mapped[Identifier(key)] = EntityTypeData(mapping)
                }
                EntityTypesConfigData(mapped)
            }
        }

        fun loginQueryStart(handler: ServerLoginNetworkHandler, server: MinecraftServer, sender: PacketSender, synchronizer: LoginSynchronizer) {
            sender.sendPacket(Channels.HANDSHAKE, AttributeConfigManager.ENDEC.encodeFully({ ByteBufSerializer.of(PacketByteBufs.create()) }, SERVER_MANAGER))
        }

        @JvmStatic
        fun refreshAttributes(entity: Entity) {
            if (entity is LivingEntity) {
                (entity.attributes as MutableAttributeContainer).`data_attributes$refresh`()
            }
        }

        fun onHealthModified(attribute: EntityAttribute, entity: LivingEntity?, modifier: EntityAttributeModifier?, previous: Double, added: Boolean) {
            if (entity?.world?.isClient == false && attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
                entity.health = (entity.health * entity.maxHealth / previous).toFloat()
            }
        }
    }

    override fun onInitialize() {
        SERVER_MANAGER.updateOverrides(OVERRIDES_CONFIG.overrides())
        SERVER_MANAGER.updateFunctions(FUNCTIONS_CONFIG.functions())
        SERVER_MANAGER.updateEntityTypes(ENTITY_TYPES_CONFIG.entity_types())

        OVERRIDES_CONFIG.subscribeToOverrides(SERVER_MANAGER::updateOverrides)
        FUNCTIONS_CONFIG.subscribeToFunctions(SERVER_MANAGER::updateFunctions)
        ENTITY_TYPES_CONFIG.subscribeToEntity_types(SERVER_MANAGER::updateEntityTypes)

        ServerLoginNetworking.registerGlobalReceiver(Channels.HANDSHAKE) { _, _, _, _, _, _ -> }

        ServerLoginConnectionEvents.QUERY_START.register(::loginQueryStart)

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, newEntity, _, _ -> refreshAttributes(newEntity) }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> refreshAttributes(player) }

        EntityAttributeModifiedEvents.MODIFIED.register(::onHealthModified)
    }
}