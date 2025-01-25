package com.bms.data_attributes.ui

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.config.DataAttributesConfigProviders
import com.bms.data_attributes.config.models.EntityTypesConfig
import com.bms.data_attributes.config.models.FunctionsConfig
import com.bms.data_attributes.config.models.OverridesConfig
import io.wispforest.owo.Owo
import io.wispforest.owo.config.Option
import io.wispforest.owo.config.annotation.ExcludeFromScreen
import io.wispforest.owo.config.annotation.Expanded
import io.wispforest.owo.config.annotation.RestartRequired
import io.wispforest.owo.config.annotation.SectionHeader
import io.wispforest.owo.config.ui.ConfigScreen
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.core.Component as OwoComponent
import io.wispforest.owo.ui.event.MouseDown
import io.wispforest.owo.ui.event.MouseEnter
import io.wispforest.owo.ui.event.MouseLeave
import io.wispforest.owo.ui.util.UISounds
import net.minecraft.util.FormattedCharSequence
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.resources.language.I18n
import org.apache.commons.lang3.mutable.MutableInt
import kotlin.math.min

class DataAttributesConfigScreen(val overrides: OverridesConfig, val functions: FunctionsConfig, val entity_types: EntityTypesConfig, parent: Screen?) : ConfigScreen(DEFAULT_MODEL_ID, DataAttributes.CONFIG, parent) {
    override fun build(rootComponent: FlowLayout) {
        this.extraFactories.put({ it.configName() == "data_attributes/entity_types" && it.backingField().field.name.equals("entries") },
            DataAttributesConfigProviders.ENTITY_TYPES_FACTORY
        )
        this.extraFactories.put({ it.configName() == "data_attributes/overrides" && it.backingField().field.name.equals("entries") },
            DataAttributesConfigProviders.ATTRIBUTE_OVERRIDE_FACTORY
        )
        this.extraFactories.put({ it.configName() == "data_attributes/functions" && it.backingField().field.name.equals("entries") },
            DataAttributesConfigProviders.ATTRIBUTE_FUNCTIONS_FACTORY
        )

        super.build(rootComponent)

        val optionPanel = rootComponent.childById(FlowLayout::class.java, "option-panel")

        if (this.minecraft?.level != null) rootComponent.surface(Surface.VANILLA_TRANSLUCENT)

        val sections = LinkedHashMap<OwoComponent, Component>()

        val containers = mutableMapOf<Option.Key, FlowLayout?>(Option.Key.ROOT to optionPanel)

        rootComponent.childById(ButtonComponent::class.java, "done-button")?.onPress {
            DataAttributes.saveConfigs()
            this.onClose()
        }

        rootComponent.childById(ButtonComponent::class.java, "reload-button")?.onPress {
            DataAttributes.reloadConfigs()
            this.uiAdapter = null
            this.rebuildWidgets()
        }

        fun optFunc(option: Option<*>) {
            if (option.backingField().hasAnnotation(ExcludeFromScreen::class.java)) return
            val parentKey = option.key().parent()
            if (!parentKey.isRoot && config.fieldForKey(parentKey)!!.isAnnotationPresent(ExcludeFromScreen::class.java)) return

            val factory = this.factoryForOption(option)
            if (factory == null) {
                Owo.LOGGER.warn("Could not create UI component for config option {}", option)
                return
            }

            val result = factory.make(this.model, option)
            options[option] = result.optionProvider()

            val expanded = !parentKey.isRoot && config.fieldForKey(parentKey)!!.isAnnotationPresent(Expanded::class.java)
            val container = containers.getOrDefault(
                parentKey,
                Containers.collapsible(Sizing.fill(100), Sizing.content(), Component.translatable("text.config." + config.name() + ".category." + parentKey.asString()), expanded).configure<CollapsibleContainer> { cc ->
                    val categoryKey = "text.config." + config.name() + ".category." + parentKey.asString()
                    if (I18n.exists("$categoryKey.tooltip")) {
                        cc.titleLayout().tooltip(Component.translatable("$categoryKey.tooltip"))
                    }
                    cc.titleLayout().child(SearchAnchorComponent(cc.titleLayout(), option.key(), { I18n.get(categoryKey) }).highlightConfigurator { highlight ->
                        highlight.positioning(Positioning.absolute(-5, -5)).verticalSizing(Sizing.fixed(19))
                    })
                }
            )

            if (!containers.containsKey(parentKey) && containers.containsKey(parentKey.parent())) {
                if (config.fieldForKey(parentKey)!!.isAnnotationPresent(SectionHeader::class.java)) {
                    this.appendSection(sections, config.fieldForKey(parentKey), containers.get(parentKey.parent()))
                }

                containers[parentKey] = container
                containers.get(parentKey.parent())?.child(container)
            }

            if (!option.detached()) {
                val tooltipText = ArrayList<FormattedCharSequence>()
                val tooltipTranslationKey = option.translationKey() + ".tooltip"

                if (I18n.exists(tooltipTranslationKey)) {
                    tooltipText.addAll(
                        minecraft!!.font.split(Component.translatable(tooltipTranslationKey), Int.MAX_VALUE)
                    )
                }

                if (option.backingField().hasAnnotation(RestartRequired::class.java)) {
                    tooltipText.add(Component.translatable("text.owo.config.applies_after_restart").visualOrderText)
                }

                if (tooltipText.isNotEmpty()) {
                    result.baseComponent().tooltip(tooltipText.stream().map(ClientTooltipComponent::create).toList())
                }
            }

            if (option.backingField().hasAnnotation(SectionHeader::class.java)) {
                this.appendSection(sections, option.backingField().field(), container)
            }
            container?.child(result.baseComponent())
        }

        overrides.forEachOption(::optFunc)
        functions.forEachOption(::optFunc)
        entity_types.forEachOption(::optFunc)

        if (!sections.isEmpty()) {
            val panelContainer = rootComponent.childById(FlowLayout::class.java, "option-panel-container")
            val panelScroll = rootComponent.childById(ScrollContainer::class.java, "option-panel-scroll")!!
            panelScroll.margins(Insets.right(10))

            val buttonPanel = model.expandTemplate(FlowLayout::class.java, "section-buttons", mapOf())
            val widestText = MutableInt()
            val textRenderer = minecraft!!.font

            sections.forEach { (component: OwoComponent?, text: Component) ->
                val hoveredText = text.copy().withStyle(ChatFormatting.YELLOW)
                if (textRenderer.width(text) > widestText.toInt()) {
                    widestText.setValue(textRenderer.width(text))
                }

                val label = Components.label(text)
                label.cursorStyle(CursorStyle.HAND).margins(Insets.of(2))

                label.mouseEnter().subscribe(MouseEnter { label.text(hoveredText) })
                label.mouseLeave().subscribe(MouseLeave { label.text(text) })

                label.mouseDown().subscribe(MouseDown { _, _, _ ->
                    panelScroll.scrollTo(component)
                    UISounds.playInteractionSound()
                    true
                })
                buttonPanel.child(label)
            }

            val closeButton = Components.label(Component.literal("<").withStyle(ChatFormatting.BOLD))
            closeButton.positioning(Positioning.relative(100, 50)).cursorStyle(CursorStyle.HAND)
                .margins(Insets.right(2))

            panelContainer!!.child(closeButton)
            panelContainer.mouseDown().subscribe(MouseDown { mouseX: Double, _: Double, _: Int ->
                if (mouseX < panelContainer.width() - 10) return@MouseDown false
                if (buttonPanel.horizontalSizing().animation() == null) {
                    val percentage = min(Math.round(((widestText.toInt() + 25f) / panelContainer.width()) * 100).toDouble(), 50.0).toInt()

                    buttonPanel.horizontalSizing().animate(350, Easing.CUBIC, Sizing.fill(percentage))
                    panelContainer.horizontalSizing().animate(350, Easing.CUBIC, Sizing.fill(100 - percentage))
                }

                buttonPanel.horizontalSizing().animation()!!.reverse()
                panelContainer.horizontalSizing().animation()!!.reverse()

                closeButton.text(
                    Component.literal(if (closeButton.text().string == ">") "<" else ">")
                        .withStyle(ChatFormatting.BOLD)
                )

                UISounds.playInteractionSound()
                true
            })

            rootComponent.childById(FlowLayout::class.java, "main-panel")!!
                .child(buttonPanel)
        }
    }
}