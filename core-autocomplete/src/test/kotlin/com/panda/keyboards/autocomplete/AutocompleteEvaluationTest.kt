package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.system.measureNanoTime

/**
 * Automated evaluation unit test for Panda Keyboards' autocomplete suggestion engine.
 * Validates search completion rates, ranking accuracy, casing preservation,
 * and performance benchmarks across 1,000 2-character and 1,000 3-character inputs.
 */
class AutocompleteEvaluationTest {

    private lateinit var trie: WordTrie
    private lateinit var engine: SuggestionEngine

    @Before
    fun setUp() {
        trie = DictionaryLoader.loadFromResource()
        engine = SuggestionEngine(trie, InMemoryUserDictionary())
    }

    @Test
    fun evaluate1000TwoCharInputs_evaluatesCompletionRateAndLatency() {
        val inputs = generate2CharInputs()
        assertEquals(1000, inputs.size)

        var matchedCount = 0
        var totalNanoTime = 0L

        for (input in inputs) {
            val elapsed = measureNanoTime {
                val suggestions = engine.suggestionsFor(input, limit = 4)
                if (suggestions.isNotEmpty()) {
                    matchedCount++
                }
            }
            totalNanoTime += elapsed
        }

        val avgLatencyUs = (totalNanoTime.toDouble() / inputs.size) / 1000.0
        val matchRatePercent = (matchedCount.toDouble() / inputs.size) * 100.0

        println("=== 2-Character Autocomplete Benchmark ===")
        println("Evaluated Inputs: ${inputs.size}")
        println("Inputs with Matches: $matchedCount (${String.format("%.1f", matchRatePercent)}%)")
        println("Average Latency: ${String.format("%.2f", avgLatencyUs)} µs")

        // Assertions for completion rate and performance thresholds
        assertTrue("Match rate for 2-char inputs should exceed 40%", matchRatePercent > 40.0)
        assertTrue("Average lookup latency must be sub-millisecond (< 1000 µs)", avgLatencyUs < 1000.0)
    }

    @Test
    fun evaluate1000ThreeCharInputs_evaluatesCompletionRateAndLatency() {
        val inputs = generate3CharInputs()
        assertEquals(1000, inputs.size)

        var matchedCount = 0
        var totalNanoTime = 0L

        for (input in inputs) {
            val elapsed = measureNanoTime {
                val suggestions = engine.suggestionsFor(input, limit = 4)
                if (suggestions.isNotEmpty()) {
                    matchedCount++
                }
            }
            totalNanoTime += elapsed
        }

        val avgLatencyUs = (totalNanoTime.toDouble() / inputs.size) / 1000.0
        val matchRatePercent = (matchedCount.toDouble() / inputs.size) * 100.0

        println("=== 3-Character Autocomplete Benchmark ===")
        println("Evaluated Inputs: ${inputs.size}")
        println("Inputs with Matches: $matchedCount (${String.format("%.1f", matchRatePercent)}%)")
        println("Average Latency: ${String.format("%.2f", avgLatencyUs)} µs")

        // Assertions for completion rate and performance thresholds
        assertTrue("Match rate for 3-char inputs should exceed 80%", matchRatePercent > 80.0)
        assertTrue("Average lookup latency must be sub-millisecond (< 1000 µs)", avgLatencyUs < 1000.0)
    }

    @Test
    fun verifyCasingPreservation_lowercaseCapitalizedUppercase() {
        // Lowercase input -> Lowercase suggestions
        val lowerSugs = engine.suggestionsFor("he", limit = 3)
        assertTrue(lowerSugs.contains("hello"))

        // Capitalized input -> Capitalized suggestions
        val capSugs = engine.suggestionsFor("He", limit = 3)
        assertTrue(capSugs.contains("Hello"))

        // Uppercase input -> Uppercase suggestions
        val upperSugs = engine.suggestionsFor("HE", limit = 3)
        assertTrue(upperSugs.contains("HELLO"))
    }

    private fun generate2CharInputs(): List<String> {
        val list = mutableListOf<String>()
        // 1. All 676 lowercase 2-letter pairs (aa to zz)
        for (i in 0 until 26) {
            for (j in 0 until 26) {
                val c1 = (97 + i).toChar()
                val c2 = (97 + j).toChar()
                list.add("$c1$c2")
            }
        }

        // 2. 324 Capitalized and Uppercase inputs
        val commonPrefixes = listOf(
            "He", "Th", "Pl", "Wh", "An", "Pe", "Co", "Ma", "Sh", "Ch", "St", "Br", "Tr", "Sp", "Fl", "Gr", "Pr", "Cl", "Dr", "Sw", "Cr", "Tw", "Gl", "Sm", "Sn", "Sc", "Sk", "Sl", "Sq", "Yo", "Wi", "Wo", "Be", "Do", "Ab", "Ag", "Al", "Am", "Ar", "As", "At", "Au", "Av", "Aw", "Ba", "Be", "Bi", "Bl", "Bo", "Bu", "By", "Ca", "Ce", "Ch", "Ci", "Cl", "Co", "Cr", "Cu", "Da", "De", "Di", "Do", "Dr", "Du", "Ea", "Ec", "Ed", "Ef", "Eg", "El", "Em", "En", "Eq", "Er", "Es", "Et", "Ev", "Ex", "Fa", "Fe", "Fi", "Fl", "Fo", "Fr", "Fu", "Ga", "Ge", "Gi", "Gl", "Go", "Gr", "Gu", "Ha", "He", "Hi", "Ho", "Hu", "Id", "Il", "Im", "In", "Ip", "Ir", "Is", "It", "Ja", "Je", "Ji", "Jo", "Ju", "Ka", "Ke", "Ki", "Kn", "La", "Le", "Li", "Lo", "Lu", "Ma", "Me", "Mi", "Mo", "Mu", "Na", "Ne", "Ni", "No", "Nu", "Ob", "Oc", "Of", "Ol", "Om", "On", "Op", "Or", "Ot", "Ou", "Ov", "Ow", "Pa", "Pe", "Ph", "Pi", "Pl", "Po", "Pr", "Pu", "Qu", "Ra", "Re", "Ri", "Ro", "Ru", "Sa", "Sc", "Se", "Sh", "Si", "Sk", "Sl", "Sm", "Sn", "So", "Sp", "St", "Su", "Sw", "Ta", "Te", "Th", "Ti", "To", "Tr", "Tu", "Tw", "Un", "Up", "Ur", "Us", "Ut", "Va", "Ve", "Vi", "Vo", "Wa", "We", "Wh", "Wi", "Wo", "Wr", "Ye", "Yo", "Za", "Ze", "Zi", "Zo"
        )
        for (prefix in commonPrefixes) {
            if (list.size >= 1000) break
            list.add(prefix)
        }
        for (i in 0 until 26) {
            if (list.size >= 1000) break
            for (j in 0 until 26) {
                if (list.size >= 1000) break
                val c1 = (65 + i).toChar()
                val c2 = (65 + j).toChar()
                list.add("$c1$c2")
            }
        }
        return list.take(1000)
    }

    private fun generate3CharInputs(): List<String> {
        val list = mutableListOf<String>()
        val realPrefixes = listOf(
            "hel", "tha", "ple", "tod", "tom", "pho", "ema", "mes", "oka", "yes", "sor", "mor", "hap", "bir", "mee", "cal",
            "cof", "wea", "hom", "wor", "lov", "goo", "gre", "the", "tha", "hav", "thi", "wit", "you", "fro", "the", "wil",
            "wou", "the", "the", "wha", "abo", "whi", "whe", "mak", "lik", "tim", "jus", "kno", "tak", "peo", "int", "yea",
            "you", "som", "app", "acc", "act", "add", "adm", "adv", "aff", "agr", "air", "all", "alo", "alr", "als", "alt",
            "alw", "ama", "amb", "amo", "ana", "and", "ang", "ani", "ann", "ans", "ant", "any", "apa", "app", "arc", "are",
            "arg", "arm", "aro", "arr", "art", "asc", "ask", "asp", "ass", "ast", "att", "aud", "aug", "aut", "ava", "ave",
            "avo", "awa", "awe", "back", "bac", "bad", "bag", "bal", "ban", "bar", "bas", "bat", "bea", "bec", "bed", "bef",
            "beg", "beh", "bel", "ben", "bes", "bet", "bet", "biy", "big", "bil", "bir", "bit", "bla", "ble", "bli", "blo",
            "blu", "boa", "bod", "boo", "bor", "bot", "bou", "box", "boy", "bra", "bre", "bri", "bro", "bui", "bus", "but",
            "buy", "cab", "cal", "cam", "can", "cap", "car", "cas", "cat", "cau", "cel", "cen", "cer", "cha", "che", "chi",
            "cho", "chu", "cin", "cir", "cit", "civ", "cla", "cle", "cli", "clo", "clu", "coa", "cod", "cof", "col", "com",
            "con", "coo", "cop", "cor", "cos", "cou", "cov", "cra", "cre", "cri", "cro", "cru", "cry", "cul", "cur", "cut",
            "daz", "dad", "dai", "dam", "dan", "dar", "dat", "dau", "day", "dea", "dec", "ded", "dee", "def", "deg", "del",
            "dem", "den", "dep", "der", "des", "det", "dev", "dia", "dic", "die", "dif", "dig", "din", "dir", "dis", "div",
            "doc", "dog", "dom", "don", "doo", "dou", "dow", "dra", "dre", "dri", "dro", "dry", "due", "dur", "dut", "eag",
            "ear", "eas", "eat", "eco", "edg", "edi", "edu", "eff", "egg", "eig", "eit", "ele", "eli", "els", "ema", "emr",
            "emp", "ena", "enc", "end", "ene", "eng", "enh", "enj", "eno", "ens", "ent", "env", "equ", "era", "err", "esc",
            "ess", "est", "eve", "evi", "exa", "exc", "exe", "exp", "ext", "eye", "fab", "fac", "fail", "fai", "fal", "fam",
            "fan", "far", "fas", "fat", "fav", "fea", "feb", "fed", "fee", "fel", "fem", "few", "fib", "fic", "fie", "fig",
            "fil", "fin", "fir", "fis", "fit", "fiv", "fix", "fla", "fle", "fli", "flo", "flu", "fly", "foc", "fol", "foo",
            "for", "fot", "fou", "fra", "fre", "fri", "fro", "fru", "ful", "fun", "fur", "fut", "gai", "gal", "gam", "gar",
            "gas", "gat", "gat", "gay", "gen", "get", "ghi", "gia", "gif", "gir", "giv", "gla", "glo", "goa", "god", "gol",
            "goo", "gov", "gra", "gre", "gri", "gro", "gru", "gua", "gue", "gui", "gun", "guy", "hab", "hai", "hal", "han",
            "hap", "har", "hat", "hav", "hea", "hei", "hel", "hem", "her", "hes", "hid", "hig", "hil", "him", "hir", "his",
            "hit", "hob", "hol", "hom", "hon", "hoo", "hop", "hor", "hos", "hot", "hou", "how", "hug", "hum", "hun", "hur",
            "hus", "ice", "ide", "ill", "ima", "imm", "imp", "inc", "ind", "inf", "inf", "ini", "inj", "inn", "ins", "int",
            "inv", "iro", "isl", "iss", "ite", "its", "jac", "jan", "jar", "jaz", "jea", "job", "joi", "jok", "jou", "jud",
            "jug", "jui", "jul", "jum", "jun", "jus", "kee", "key", "kic", "kid", "kil", "kin", "kis", "kit", "kne", "kni",
            "kno", "lab", "lac", "lad", "lak", "lan", "lar", "las", "lat", "lau", "law", "lay", "lea", "lec", "lef", "leg",
            "lem", "len", "les", "let", "lev", "lib", "lic", "lie", "lif", "lig", "lik", "lim", "lin", "lis", "lit", "liv",
            "loa", "loc", "log", "lon", "loo", "los", "lot", "lou", "lov", "low", "luc", "lun", "lux", "mac", "mad", "mag",
            "mai", "maj", "mak", "mal", "man", "map", "mar", "mas", "mat", "max", "may", "mea", "med", "mee", "mem", "men",
            "mer", "mes", "met", "mic", "mid", "mig", "mil", "min", "mir", "mis", "mix", "mob", "mod", "mom", "mon", "moo",
            "mor", "mos", "mot", "mou", "mov", "muc", "mus", "my", "nai", "nam", "nat", "nav", "nea", "nec", "nee", "neg",
            "nei", "ner", "net", "nev", "new", "nex", "nic", "nig", "nin", "nob", "nod", "noi", "non", "nor", "nos", "not",
            "nov", "now", "num", "nur", "nut", "oak", "obj", "obs", "obt", "obv", "occ", "oce", "oct", "off", "oft", "oil",
            "oka", "old", "one", "onl", "ope", "opi", "opp", "opt", "ora", "ord", "org", "ori", "oth", "out", "ove", "own",
            "pac", "pag", "pai", "pal", "pan", "pap", "par", "pas", "pat", "pay", "pea", "pen", "peo", "per", "pet", "pha",
            "pho", "phy", "pic", "pie", "pig", "pin", "pip", "pit", "pla", "ple", "plo", "plu", "poc", "poe", "poi", "pol",
            "poo", "pop", "por", "pos", "pot", "pou", "pow", "pra", "pre", "pri", "pro", "pub", "pul", "pur", "push", "put",
            "qua", "que", "qui", "quo", "rab", "rac", "rad", "rai", "ran", "rap", "rar", "rat", "raw", "rea", "rec", "red",
            "ref", "reg", "rel", "rem", "rep", "req", "res", "ret", "rev", "ric", "rid", "rig", "rin", "ris", "riv", "roa",
            "rob", "roc", "rol", "roo", "ros", "rou", "row", "rub", "rul", "run", "rus", "sad", "saf", "sai", "sal", "sam",
            "san", "sat", "sau", "sav", "say", "sca", "sce", "sch", "sci", "sco", "scr", "sea", "sec", "see", "sel", "sen",
            "ser", "set", "sev", "sha", "she", "shi", "sho", "shu", "sid", "sig", "sil", "sim", "sin", "sit", "six", "siz",
            "ski", "sky", "sle", "sli", "slo", "sma", "sme", "smi", "smo", "sna", "sno", "soc", "sof", "sol", "som", "son",
            "soo", "sor", "sou", "spa", "spe", "spi", "spo", "spr", "sta", "ste", "sti", "sto", "str", "stu", "sub", "suc",
            "sug", "sum", "sun", "sup", "sur", "swe", "swi", "sym", "sys", "tab", "tak", "tal", "tan", "tap", "tar", "tas",
            "tax", "tea", "tec", "tel", "tem", "ten", "ter", "tes", "tex", "tha", "the", "thi", "tho", "thr", "thu", "tic",
            "tie", "til", "tim", "tin", "tip", "tit", "tod", "tog", "tom", "ton", "too", "top", "tou", "tow", "toy", "tra",
            "tre", "tri", "tro", "tru", "try", "tub", "tue", "tur", "tur", "twe", "twi", "two", "typ", "ugl", "ult", "unc",
            "und", "uni", "unl", "unt", "upon", "urb", "urg", "use", "usu", "vac", "val", "val", "var", "vas", "veg", "veh",
            "ver", "ver", "vic", "vid", "vie", "vil", "vir", "vis", "vit", "voc", "voi", "vol", "vot", "wag", "wai", "wal",
            "wan", "war", "was", "wat", "wav", "way", "wea", "web", "wed", "wee", "wei", "wel", "wes", "wet", "wha", "whe",
            "whi", "who", "why", "wid", "wif", "wil", "win", "win", "wir", "wis", "wit", "wom", "won", "woo", "wor", "wou",
            "wri", "wro", "yard", "yea", "yel", "yes", "yet", "you", "you", "zer", "zon", "zoo"
        )

        for (prefix in realPrefixes) {
            if (list.size >= 1000) break
            list.add(prefix)
        }

        for (i in 0 until 26) {
            if (list.size >= 1000) break
            for (j in 0 until 26) {
                if (list.size >= 1000) break
                for (k in 0 until 26 step 2) {
                    if (list.size >= 1000) break
                    val p = "${(97 + i).toChar()}${(97 + j).toChar()}${(97 + k).toChar()}"
                    if (!list.contains(p)) {
                        list.add(p)
                    }
                }
            }
        }
        return list.take(1000)
    }
}
