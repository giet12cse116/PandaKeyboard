package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SuggestionEngineTest {

    private lateinit var trie: WordTrie
    private lateinit var userDict: UserDictionary
    private lateinit var engine: SuggestionEngine

    @Before
    fun setUp() {
        trie = WordTrie().apply {
            insert("hello", 1000)
            insert("help", 800)
            insert("helicopter", 100)
            insert("keyboard", 900)
            insert("keyboards", 700)
        }
        userDict = InMemoryUserDictionary()
        engine = SuggestionEngine(trie, userDict)
    }

    @Test
    fun suggestionsFor_shortInputUnderTwoChars_returnsEmptyList() {
        assertTrue(engine.suggestionsFor("").isEmpty())
        assertTrue(engine.suggestionsFor("h").isEmpty())
    }

    @Test
    fun suggestionsFor_lowercaseInput_preservesLowercaseCasing() {
        val suggestions = engine.suggestionsFor("hel", limit = 3)

        assertEquals(3, suggestions.size)
        assertEquals("hello", suggestions[0])
        assertEquals("help", suggestions[1])
        assertEquals("helicopter", suggestions[2])
    }

    @Test
    fun suggestionsFor_capitalizedInput_preservesCapitalizedCasing() {
        val suggestions = engine.suggestionsFor("Hel", limit = 3)

        assertEquals(3, suggestions.size)
        assertEquals("Hello", suggestions[0])
        assertEquals("Help", suggestions[1])
        assertEquals("Helicopter", suggestions[2])
    }

    @Test
    fun suggestionsFor_uppercaseInput_preservesUppercaseCasing() {
        val suggestions = engine.suggestionsFor("HEL", limit = 3)

        assertEquals(3, suggestions.size)
        assertEquals("HELLO", suggestions[0])
        assertEquals("HELP", suggestions[1])
        assertEquals("HELICOPTER", suggestions[2])
    }

    @Test
    fun suggestionsFor_userDictionaryBlending_boostsUserWordsToTop() {
        // Record a custom user-specific word starting with "hel"
        userDict.recordWord("helmuth")

        val suggestions = engine.suggestionsFor("hel", limit = 3)

        assertEquals(3, suggestions.size)
        assertEquals("helmuth", suggestions[0]) // User-learned word ranks first due to boosting!
        assertEquals("hello", suggestions[1])
        assertEquals("help", suggestions[2])
    }

    @Test
    fun suggestionsFor_defaultsToLimitFour() {
        trie.insert("hellen", 500)
        val suggestions = engine.suggestionsFor("hel")

        assertEquals("Default limit must return up to 4 suggestions", 4, suggestions.size)
        assertEquals("hello", suggestions[0])
        assertEquals("help", suggestions[1])
        assertEquals("hellen", suggestions[2])
        assertEquals("helicopter", suggestions[3])
    }

    @Test
    fun suggestionsFor_transformedFontPrefix_normalizesAndMatchesDictionary() {
        // Bold Serif "𝐡𝐞𝐥" (U+1D489 U+1D486 U+1D48D)
        val boldSerifPrefix = com.panda.keyboards.fonts.FontTransformer.transform("hel", com.panda.keyboards.fonts.FontStyle.BOLD_SERIF)
        val suggestions = engine.suggestionsFor(boldSerifPrefix, limit = 4)

        assertEquals(3, suggestions.size)
        assertEquals("hello", suggestions[0])
        assertEquals("help", suggestions[1])
        assertEquals("helicopter", suggestions[2])
    }

    @Test
    fun suggestionsFor_dictionaryResource_providesMultipleSuggestionsForWheAnaPe() {
        val loadedTrie = DictionaryLoader.loadFromResource()
        val loadedEngine = SuggestionEngine(loadedTrie)

        // Whe -> When, Where, Wherever, Whether, etc.
        val wheSuggestions = loadedEngine.suggestionsFor("Whe", limit = 4)
        assertTrue("Whe should return at least 4 suggestions", wheSuggestions.size >= 4)
        assertTrue("Should contain When", wheSuggestions.contains("When"))
        assertTrue("Should contain Where", wheSuggestions.contains("Where"))

        // Ana -> Analysis, Analyze, Analyst, Analytics
        val anaSuggestions = loadedEngine.suggestionsFor("Ana", limit = 4)
        assertTrue("Ana should return at least 4 suggestions", anaSuggestions.size >= 4)
        assertTrue("Should contain Analysis", anaSuggestions.contains("Analysis"))
        assertTrue("Should contain Analyze", anaSuggestions.contains("Analyze"))

        // Pe -> People, Person, Personal, Personality
        val peSuggestions = loadedEngine.suggestionsFor("Pe", limit = 4)
        assertTrue("Pe should return at least 4 suggestions", peSuggestions.size >= 4)
        assertTrue("Should contain People", peSuggestions.contains("People"))
        assertTrue("Should contain Person", peSuggestions.contains("Person"))
    }
}
