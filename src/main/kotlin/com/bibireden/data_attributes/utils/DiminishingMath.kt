package com.bibireden.data_attributes.utils

import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.utils.DiminishingMathOptions
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/*
    The math in the legacy version of this library uses classes which I dislike
    So lets just have the math be functions!

    - DataEncoded
 */

/*


fun calculateStacking(positiveChanges: Set<Double>?, negativeChanges: Set<Double>?, rawIterativeValue: Double?, functionBehavior: StackingBehavior, stackingFormula: StackingFormula) : Double
{
    var iterativeValue = 0.001

    if (rawIterativeValue != null)
    {
        // Cannot divide by 0 and above 1 has some undefined values
        iterativeValue = max(min(rawIterativeValue, 1.0), 0.001)
    }

    if (functionBehavior == StackingBehavior.Add)
    {
        var positiveMaximum: Double = 0.0
        var positiveSum: Double = 0.0

        var negativeMaximum: Double = 0.0
        var negativeSum: Double = 0.0

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
            return positiveSum - negativeSum
        } else if (stackingFormula == StackingFormula.Diminished)
        {
            return (1-negativeMaximum)*((1-iterativeValue).pow(((negativeSum-negativeMaximum)/iterativeValue))) + (1-positiveMaximum)*((1-iterativeValue).pow(((positiveSum-positiveMaximum)/iterativeValue)))
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
            return positiveMultiplier - negativeMultiplier
        } else if (stackingFormula == StackingFormula.Diminished)
        {
            return (1-negativeMaximum)*((1-iterativeValue).pow(((negativeMultiplier-negativeMaximum)/iterativeValue))) + (1-positiveMaximum)*((1-iterativeValue).pow(((positiveMultiplier-positiveMaximum)/iterativeValue)))
        }

    }

    return 1.0 // todo: for now, want to test a build
}
*/

fun calculateString(positiveChanges: Set<Double>?, negativeChanges: Set<Double>?, functionBehavior: StackingBehavior, stackingFormula: StackingFormula, options: DiminishingMathOptions?)
{
    var returnValue = 0.0
    if (functionBehavior == StackingBehavior.Add)
    {
        var totalChange = 0.0

        for (change in positiveChanges!!)
        {
            totalChange += change
        }
        for (change in negativeChanges!!)
        {
            totalChange -= change
        }

        if (stackingFormula == StackingFormula.Flat)
        {
            returnValue = totalChange
        } else if (stackingFormula == StackingFormula.Diminished && options != null) {
            // Now do the piecewise
            if (totalChange == 0.0)
            {
                returnValue = options.middlePoint
            } else if (totalChange > 0.0)
            {
                returnValue = (options.maximum - options.middlePoint) * (totalChange/(totalChange+options.smoothness)) + options.middlePoint
            } else if (totalChange < 0.0)
            {
                returnValue = (options.middlePoint - options.minimum) * (-(totalChange/(totalChange-options.smoothness))) + options.middlePoint
            }
        }
    } else if (functionBehavior == StackingBehavior.Multiply)
    {
        var totalChange = 1.0

        for (change in positiveChanges!!)
        {
            totalChange *= (1+change)
        }
        for (change in negativeChanges!!)
        {
            totalChange *= (1-change)
        }

        if (stackingFormula == StackingFormula.Flat)
        {
            returnValue = totalChange
        } else if (stackingFormula == StackingFormula.Diminished && options != null) {
            // Now do the piecewise
            if (totalChange == 1.0)
            {
                returnValue = options.middlePoint
            } else if (totalChange > 1.0)
            {
                returnValue = (options.maximum - options.middlePoint) * (totalChange/(totalChange+options.smoothness)) + options.middlePoint
            } else if (totalChange < 1.0)
            {
                returnValue = (options.middlePoint - options.minimum) * (-(totalChange/(totalChange-options.smoothness))) + options.middlePoint
            }
        }
    }


}