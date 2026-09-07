package com.panda.keyboards.data

import com.panda.keyboards.fonts.FontStyle

/**
 * In-memory catalog of all available font styles.
 * Serves as a simple data source exposing all [FontStyle] entries.
 */
object FontCatalog {
    /** All available font styles, ordered as defined in the enum. */
    val allStyles: List<FontStyle> = FontStyle.entries.toList()

    /** Look up a style by its stable [FontStyle.id]. */
    fun findById(id: String): FontStyle? = FontStyle.fromId(id)
}
