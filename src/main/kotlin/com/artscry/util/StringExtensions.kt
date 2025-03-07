package com.artscry.util

import androidx.compose.ui.graphics.Color

object StringExtensions {
    fun String.toColor(): Color {
        return if (this.startsWith("#")) {
            Color(this.substring(1).toLong(16))
        } else {
            val hashCode = this.hashCode()
            val r = (hashCode and 0xFF0000) shr 16
            val g = (hashCode and 0x00FF00) shr 8
            val b = hashCode and 0x0000FF
            Color(r, g, b)
        }
    }

    fun String.toColorInt(): Long {
        if (this.startsWith("#")) {
            return this.substring(1).toLong(16)
        }

        return this.hashCode().toLong() and 0xFFFFFFFF
    }
}