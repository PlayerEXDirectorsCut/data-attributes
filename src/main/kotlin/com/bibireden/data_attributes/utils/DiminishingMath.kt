package com.bibireden.data_attributes.utils

import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeInstance
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/*
    The math in the legacy version of this library uses classes which I dislike
    So lets just have the math be functions!

    - DataEncoded
 */


fun calculateStacking(positiveChanges: Set<Double>?, negativeChanges: Set<Double>?, rawIterativeValue: Double = 0.001, functionBehavior: StackingBehavior, stackingFormula: StackingFormula, maximum: Double = 1.0, minimum: Double = 0.0): Double
{
    val iterativeValue = max(min(rawIterativeValue, 1.0), 0.001)

    var returnValue = 0.0;

    if (functionBehavior == StackingBehavior.Add)
    {
        var positiveMaximum = 0.0
        var positiveSum = 0.0

        var negativeMaximum = 0.0
        var negativeSum = 0.0

        if (positiveChanges != null) {
            for (positiveValue in positiveChanges) {
                // TODO: Clamp all value to maximum
                val value = abs(positiveValue)
                positiveSum += value
                positiveMaximum = max(positiveMaximum, value)
            }
        }

        if (negativeChanges != null) {
            for (negativeValue in negativeChanges) {
                val value = abs(negativeValue)
                negativeSum += value
                negativeMaximum = max(negativeMaximum, value)
            }
        }

        if (stackingFormula == StackingFormula.Flat)
        {
            returnValue = positiveSum - negativeSum
        } else if (stackingFormula == StackingFormula.Diminished)
        {
            returnValue = (1-negativeMaximum)*((1-iterativeValue).pow(((negativeSum-negativeMaximum)/iterativeValue))) + (1-positiveMaximum)*((1-iterativeValue).pow(((positiveSum-positiveMaximum)/iterativeValue)))
        }
    } else if (functionBehavior == StackingBehavior.Multiply)
    {
        var positiveMaximum: Double = 0.0
        var positiveMultiplier: Double = 1.0
        var negativeMaximum: Double = 1.0
        var negativeMultiplier: Double = 0.0

        if (positiveChanges != null) {
            for (positiveValue in positiveChanges) {
                // TODO: Clamp all value to maximum
                val value = abs(positiveValue)
                positiveMultiplier *= (1 - value)
                positiveMaximum = max(positiveMaximum, value)
            }
        }

        if (negativeChanges != null) {
            for (negativeValue in negativeChanges) {
                val value = abs(negativeValue)
                negativeMultiplier *= (1+value)
                negativeMaximum = max(negativeMaximum, value)
            }
        }

        if (stackingFormula == StackingFormula.Flat)
        {
            returnValue = positiveMultiplier - negativeMultiplier
        } else if (stackingFormula == StackingFormula.Diminished)
        {
            returnValue = (1-negativeMaximum)*((1-iterativeValue).pow(((negativeMultiplier-negativeMaximum)/iterativeValue))) + (1-positiveMaximum)*((1-iterativeValue).pow(((positiveMultiplier-positiveMaximum)/iterativeValue)))
        }

    }

    return returnValue * (maximum-minimum) + minimum
}

fun computeStacking(instance: EntityAttributeInstance, attributeType: EntityAttribute, container: AttributeContainer?): Double {
    val attribute = instance.attribute as MutableEntityAttribute

    val formula = attribute.`data_attributes$formula`()
    val parents = attribute.`data_attributes$parents`()
    val baseValue = instance.baseValue


    var addedValue = baseValue

    // Factor in ALL modifiers with addition operation FIRST

    for (modifier in instance.getModifiers(Operation.ADDITION)) {
        addedValue += modifier.value;
    }

    // -- MODDED PART :: ADDITION

    if (container != null) {
        for ((parent, function) in parents) {
            if (function.behavior() != StackingBehavior.Add) continue

            val entityAttributeInstance = container.getCustomInstance(parent as EntityAttribute) ?: continue

            val multiplier = function.value()
            val value = multiplier * entityAttributeInstance.value
        }
    }

    // -- END OF MODDED PART

    // Provide the final result with the value that has factored in all addition operations
    var finalValue = addedValue

    // Multiply Base utilizes addedValue in its calcs

    for (modifier in instance.getModifiers(Operation.MULTIPLY_BASE)) {
        finalValue += addedValue * modifier.value
    }

    for (modifier in instance.getModifiers(Operation.MULTIPLY_TOTAL)) {
        finalValue *= 1.0 + modifier.value
    }

    return attributeType.clamp(finalValue)
}