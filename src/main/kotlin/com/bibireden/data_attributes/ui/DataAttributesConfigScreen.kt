package com.bibireden.data_attributes.ui

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.DataAttributesConfigProviders
import com.bibireden.data_attributes.config.models.DataAttributesEntityTypesConfig
import com.bibireden.data_attributes.config.models.DataAttributesFunctionsConfig
import com.bibireden.data_attributes.config.models.DataAttributesOverridesConfig
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
import io.wispforest.owo.ui.event.MouseDown
import io.wispforest.owo.ui.event.MouseEnter
import io.wispforest.owo.ui.event.MouseLeave
import io.wispforest.owo.ui.util.UISounds
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.TooltipComponent
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.commons.lang3.mutable.MutableInt
import java.util.function.BiConsumer
import kotlin.math.min

class DataAttributesConfigScreen(val overrides: DataAttributesOverridesConfig, val functions: DataAttributesFunctionsConfig, val entity_types: DataAttributesEntityTypesConfig, parent: Screen?) : ConfigScreen(DEFAULT_MODEL_ID, DataAttributes.CONFIG, parent) {
    override fun build(rootComponent: FlowLayout) {
        this.extraFactories.put({ it.backingField().field.name.equals("entity_types") },
            DataAttributesConfigProviders.ENTITY_TYPES_FACTORY
        )
        this.extraFactories.put({ it.backingField().field.name.equals("overrides") },
            DataAttributesConfigProviders.ATTRIBUTE_OVERRIDE_FACTORY
        )
        this.extraFactories.put({ it.backingField().field.name.equals("functions") },
            DataAttributesConfigProviders.ATTRIBUTE_FUNCTIONS_FACTORY
        )

        super.build(rootComponent)

        val optionPanel = rootComponent.childById(FlowLayout::class.java, "option-panel")

        if (this.client?.world != null) rootComponent.surface(Surface.blur(.55F, 2F))

        val sections = LinkedHashMap<Component, Text>()

        val containers = mutableMapOf<Option.Key, FlowLayout?>(Option.Key.ROOT to optionPanel)

        rootComponent.childById(ButtonComponent::class.java, "done-button")?.onPress {
            DataAttributes.saveConfigs()
            this.close()
        }

        rootComponent.childById(ButtonComponent::class.java, "reload-button")?.onPress {
            DataAttributes.reloadConfigs()
            this.uiAdapter = null
            this.clearAndInit()
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
                Containers.collapsible(Sizing.fill(100), Sizing.content(), Text.translatable("text.config." + config.name() + ".category." + parentKey.asString()), expanded).configure<CollapsibleContainer> { cc ->
                    val categoryKey = "text.config." + config.name() + ".category." + parentKey.asString()
                    if (I18n.hasTranslation("$categoryKey.tooltip")) {
                        cc.titleLayout().tooltip(Text.translatable("$categoryKey.tooltip"))
                    }
                    cc.titleLayout().child(SearchAnchorComponent(cc.titleLayout(), option.key(), { I18n.translate(categoryKey) }).highlightConfigurator { highlight ->
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
                val tooltipText = ArrayList<OrderedText>()
                val tooltipTranslationKey = option.translationKey() + ".tooltip"

                if (I18n.hasTranslation(tooltipTranslationKey)) {
                    tooltipText.addAll(
                        client!!.textRenderer.wrapLines(Text.translatable(tooltipTranslationKey), Int.MAX_VALUE)
                    )
                }

                if (option.backingField().hasAnnotation(RestartRequired::class.java)) {
                    tooltipText.add(Text.translatable("text.owo.config.applies_after_restart").asOrderedText())
                }

                if (!tooltipText.isEmpty()) {
                    result.baseComponent().tooltip(tooltipText.stream().map { text -> TooltipComponent.of(text) }.toList())
                }
            }

            if (option.backingField().hasAnnotation(SectionHeader::class.java)) {
                this.appendSection(sections, option.backingField().field(), container)
            }
            container?.child(result.baseComponent())
        }

        overrides.forEachOption(::optFunc);
        functions.forEachOption(::optFunc);
        entity_types.forEachOption(::optFunc);

        if (!sections.isEmpty()) {
            val panelContainer = rootComponent.childById(FlowLayout::class.java, "option-panel-container")
            val panelScroll = rootComponent.childById(ScrollContainer::class.java, "option-panel-scroll")!!
            panelScroll.margins(Insets.right(10))

            val buttonPanel = model.expandTemplate(FlowLayout::class.java, "section-buttons", mapOf())
            val widestText = MutableInt()

            sections.forEach(BiConsumer<Component, Text> { component: Component?, text: Text ->
                val hoveredText = text.copy().formatted(Formatting.YELLOW)
                if (textRenderer.getWidth(text) > widestText.toInt()) {
                    widestText.setValue(textRenderer.getWidth(text))
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
            })

            val closeButton = Components.label(Text.literal("<").formatted(Formatting.BOLD))
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
                    Text.literal(if (closeButton.text().string == ">") "<" else ">")
                        .formatted(Formatting.BOLD)
                )

                UISounds.playInteractionSound()
                true
            })

            rootComponent.childById(FlowLayout::class.java, "main-panel")!!
                .child(buttonPanel)
        }
    }
}