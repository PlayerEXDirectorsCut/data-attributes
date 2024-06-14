package com.bibireden.data_attributes.utils

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.round(places: Int) = BigDecimal(this).setScale(places, RoundingMode.HALF_UP).toDouble()