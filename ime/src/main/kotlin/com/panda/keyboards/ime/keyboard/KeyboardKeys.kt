package com.panda.keyboards.ime.keyboard

/**
 * Type classification for keyboard keys, determining behavior and appearance.
 */
enum class KeyType {
    CHARACTER,
    BACKSPACE,
    SHIFT,
    SYMBOLS,    // Toggle ?123 / ABC
    EMOJI,      // Toggle Emoji & Stickers panel
    SPACE,
    ENTER,
    GLOBE,      // Language switch placeholder
    HIDE_KEYBOARD // Dismiss soft keyboard key
}

/**
 * Data for a single keyboard key.
 *
 * @property label Display text for the key (can be a character or icon label).
 * @property widthWeight Relative width weight in the row (1.0 = standard key).
 * @property type Behavioral classification.
 * @property output The text to commit when pressed (for CHARACTER keys).
 *           For special keys, this is ignored — behavior is handled by type.
 * @property numberHint Corner badge and long-press number shortcut (for top row letter keys).
 */
data class KeyData(
    val label: String,
    val widthWeight: Float = 1f,
    val type: KeyType = KeyType.CHARACTER,
    val output: String = label,
    val numberHint: String? = null
)

/**
 * Layout definitions for all keyboard layers.
 *
 * All rows are defined as lists of [KeyData]. The layout follows a
 * standard QWERTY arrangement with 4 rows.
 */
object KeyboardLayouts {

    // ── Dedicated Number Row ─────────────────────────────────────────────

    val numberRow = listOf(
        KeyData("1"), KeyData("2"), KeyData("3"), KeyData("4"), KeyData("5"),
        KeyData("6"), KeyData("7"), KeyData("8"), KeyData("9"), KeyData("0")
    )

    // ── QWERTY Letter Rows ──────────────────────────────────────────────


    private val qwertyRow1 = listOf(
        KeyData("q", numberHint = "1"), KeyData("w", numberHint = "2"), KeyData("e", numberHint = "3"),
        KeyData("r", numberHint = "4"), KeyData("t", numberHint = "5"), KeyData("y", numberHint = "6"),
        KeyData("u", numberHint = "7"), KeyData("i", numberHint = "8"), KeyData("o", numberHint = "9"),
        KeyData("p", numberHint = "0")
    )

    private val qwertyRow2 = listOf(
        KeyData("a"), KeyData("s"), KeyData("d"), KeyData("f"), KeyData("g"),
        KeyData("h"), KeyData("j"), KeyData("k"), KeyData("l")
    )

    private val qwertyRow3 = listOf(
        KeyData("⇧", widthWeight = 1.5f, type = KeyType.SHIFT),
        KeyData("z"), KeyData("x"), KeyData("c"), KeyData("v"),
        KeyData("b"), KeyData("n"), KeyData("m"),
        KeyData("⌫", widthWeight = 1.5f, type = KeyType.BACKSPACE)
    )

    private val qwertyRow4 = listOf(
        KeyData("?123", widthWeight = 1.25f, type = KeyType.SYMBOLS),
        KeyData("😀", widthWeight = 1f, type = KeyType.EMOJI),
        KeyData(",", widthWeight = 1f),
        KeyData("space", widthWeight = 3.75f, type = KeyType.SPACE, output = " "),
        KeyData(".", widthWeight = 1f),
        KeyData("↵", widthWeight = 1.5f, type = KeyType.ENTER)
    )

    val letterRows = listOf(qwertyRow1, qwertyRow2, qwertyRow3, qwertyRow4)

    // ── QWERTZ Letter Rows (German) ─────────────────────────────────────

    private val qwertzRow1 = listOf(
        KeyData("q", numberHint = "1"), KeyData("w", numberHint = "2"), KeyData("e", numberHint = "3"),
        KeyData("r", numberHint = "4"), KeyData("t", numberHint = "5"), KeyData("z", numberHint = "6"),
        KeyData("u", numberHint = "7"), KeyData("i", numberHint = "8"), KeyData("o", numberHint = "9"),
        KeyData("p", numberHint = "0")
    )

    private val qwertzRow2 = listOf(
        KeyData("a"), KeyData("s"), KeyData("d"), KeyData("f"), KeyData("g"),
        KeyData("h"), KeyData("j"), KeyData("k"), KeyData("l")
    )

    private val qwertzRow3 = listOf(
        KeyData("⇧", widthWeight = 1.5f, type = KeyType.SHIFT),
        KeyData("y"), KeyData("x"), KeyData("c"), KeyData("v"),
        KeyData("b"), KeyData("n"), KeyData("m"),
        KeyData("⌫", widthWeight = 1.5f, type = KeyType.BACKSPACE)
    )

    val qwertzLetterRows = listOf(qwertzRow1, qwertzRow2, qwertzRow3, qwertyRow4)

    // ── AZERTY Letter Rows (French) ─────────────────────────────────────

    private val azertyRow1 = listOf(
        KeyData("a", numberHint = "1"), KeyData("z", numberHint = "2"), KeyData("e", numberHint = "3"),
        KeyData("r", numberHint = "4"), KeyData("t", numberHint = "5"), KeyData("y", numberHint = "6"),
        KeyData("u", numberHint = "7"), KeyData("i", numberHint = "8"), KeyData("o", numberHint = "9"),
        KeyData("p", numberHint = "0")
    )

    private val azertyRow2 = listOf(
        KeyData("q"), KeyData("s"), KeyData("d"), KeyData("f"), KeyData("g"),
        KeyData("h"), KeyData("j"), KeyData("k"), KeyData("l"), KeyData("m")
    )

    private val azertyRow3 = listOf(
        KeyData("⇧", widthWeight = 1.5f, type = KeyType.SHIFT),
        KeyData("w"), KeyData("x"), KeyData("c"), KeyData("v"),
        KeyData("b"), KeyData("n"),
        KeyData("⌫", widthWeight = 1.5f, type = KeyType.BACKSPACE)
    )

    val azertyLetterRows = listOf(azertyRow1, azertyRow2, azertyRow3, qwertyRow4)

    /**
     * Get letter rows corresponding to the user's selected [com.panda.keyboards.theme.QwertyOrder].
     */
    fun getLetterRows(order: com.panda.keyboards.theme.QwertyOrder): List<List<KeyData>> = when (order) {
        com.panda.keyboards.theme.QwertyOrder.QWERTY -> letterRows
        com.panda.keyboards.theme.QwertyOrder.QWERTZ -> qwertzLetterRows
        com.panda.keyboards.theme.QwertyOrder.AZERTY -> azertyLetterRows
    }

    // ── Symbols Layer 1 ─────────────────────────────────────────────────

    private val symbolsRow1_1 = listOf(
        KeyData("1"), KeyData("2"), KeyData("3"), KeyData("4"), KeyData("5"),
        KeyData("6"), KeyData("7"), KeyData("8"), KeyData("9"), KeyData("0")
    )

    private val symbolsRow1_2 = listOf(
        KeyData("@"), KeyData("#"), KeyData("\$"), KeyData("_"), KeyData("&"),
        KeyData("-"), KeyData("+"), KeyData("("), KeyData(")")
    )

    private val symbolsRow1_3 = listOf(
        KeyData("=\\<", widthWeight = 1.5f, type = KeyType.SHIFT),
        KeyData("*"), KeyData("\""), KeyData("'"), KeyData(":"),
        KeyData(";"), KeyData("!"), KeyData("?"),
        KeyData("⌫", widthWeight = 1.5f, type = KeyType.BACKSPACE)
    )

    private val symbolsRow1_4 = listOf(
        KeyData("ABC", widthWeight = 1.25f, type = KeyType.SYMBOLS),
        KeyData("😀", widthWeight = 1f, type = KeyType.EMOJI),
        KeyData(",", widthWeight = 1f),
        KeyData("space", widthWeight = 3.75f, type = KeyType.SPACE, output = " "),
        KeyData(".", widthWeight = 1f),
        KeyData("↵", widthWeight = 1.5f, type = KeyType.ENTER)
    )

    val symbols1Rows = listOf(symbolsRow1_1, symbolsRow1_2, symbolsRow1_3, symbolsRow1_4)

    // ── Symbols Layer 2 ─────────────────────────────────────────────────

    private val symbolsRow2_1 = listOf(
        KeyData("~"), KeyData("`"), KeyData("|"), KeyData("•"), KeyData("√"),
        KeyData("π"), KeyData("÷"), KeyData("×"), KeyData("¶"), KeyData("∆")
    )

    private val symbolsRow2_2 = listOf(
        KeyData("£"), KeyData("€"), KeyData("¥"), KeyData("^"), KeyData("°"),
        KeyData("="), KeyData("{"), KeyData("}"), KeyData("\\")
    )

    private val symbolsRow2_3 = listOf(
        KeyData("?123", widthWeight = 1.5f, type = KeyType.SHIFT),
        KeyData("%"), KeyData("©"), KeyData("®"), KeyData("™"),
        KeyData("✓"), KeyData("["), KeyData("]"),
        KeyData("⌫", widthWeight = 1.5f, type = KeyType.BACKSPACE)
    )

    private val symbolsRow2_4 = listOf(
        KeyData("ABC", widthWeight = 1.25f, type = KeyType.SYMBOLS),
        KeyData("😀", widthWeight = 1f, type = KeyType.EMOJI),
        KeyData(",", widthWeight = 1f),
        KeyData("space", widthWeight = 3.75f, type = KeyType.SPACE, output = " "),
        KeyData("<"), KeyData(">"),
        KeyData("↵", widthWeight = 1.5f, type = KeyType.ENTER)
    )

    val symbols2Rows = listOf(symbolsRow2_1, symbolsRow2_2, symbolsRow2_3, symbolsRow2_4)
}
