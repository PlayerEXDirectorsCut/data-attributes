package com.bibireden.data_attributes.utils

import java.nio.ByteBuffer

class Math {
    companion object {
        /**
         * @param value
         * @param arraySize This MUST be 8 or greater! No Checks are implemented for the sake of performance.
         * @return Returns a byte array with the first 8 bytes storing the input double.
         * @since 1.4.0
         */
        fun doubleToByteArray(value: Double , arraySize: Int): ByteArray
        {
            val array = ByteArray(arraySize);
            ByteBuffer.wrap(array).putDouble(value);
            return array;
        }

        /**
         * @param array This MUST be an array of size 8 or greater! No Checks are implemented for the sake of performance.
         * @return Returns the double stored in the first 8 bytes of the array.
         * @since 1.4.0
         */
        fun byteArrayToDouble(array: ByteArray): Double
        {
            return ByteBuffer.wrap(array).getDouble();
        }
    }
}