# `:core-autocomplete` — Standalone Autocomplete & Suggestion Engine

`:core-autocomplete` is a pure Kotlin JVM library designed to provide fast, memory-efficient word autocompletion, personalized current-word suggestions, and on-device next-word prediction for Panda Keyboards.

## Architecture

- **`WordTrie`**: A prefix Trie storing words, frequency ranks, proper noun flags (`isProperNoun`), and canonical capitalization forms (`canonicalWord`). Traverses prefix paths in $O(k)$ time (where $k$ is prefix length) and returns top candidate words ordered by frequency.
- **`SuggestionEngine`**: Generates current-word suggestions for user input prefixes:
  - **Threshold Filtering**: Input shorter than 2 characters returns an empty list to avoid noisy suggestions before sufficient context is typed.
  - **Casing Preservation & Proper Noun Override**: Automatically detects and applies the input prefix's casing style (`LOWERCASE`, `CAPITALIZED`, `ALL_UPPERCASE`, or `MIXED`) for common words. For proper nouns (`isProperNoun = true`), overrides typed-case with their fixed canonical correct form (e.g. `London`, `Nike`, `Sarah`, `McDonald's`).
  - **Personalization Blending**: Combines candidates from static dictionaries (`dictionary_en.txt` + `dictionary_proper_nouns.txt`) with user-committed words in `UserDictionary`, boosting user-learned words so custom names and slang rank near the top.
- **`UserDictionary`**: In-memory and extensible user dictionary for recording words typed/committed by the user.
- **`DictionaryLoader`**: Helper for reading line-delimited word frequency dataset files (`dictionary_en.txt`, `dictionary_proper_nouns.txt`) off the main thread.

---

## Next-Word Prediction Engine

The next-word prediction engine suggests likely following words given the user's previously completed word (e.g. `Could` $\rightarrow$ `you`, `be`, `have`, `I`), blending general-language patterns with personal user typing habits.

- **`BigramModel` & `BigramLoader`**: Loads and queries bundled general-language word-pair frequencies (`bigrams_en.txt`). Performs case-insensitive, normalized lookups ranked by frequency score.
- **`PersonalNgramStore`**: On-device store tracking the user's real-time word-pair transition counts during normal typing.
  - **Incremental & Lightweight**: Updates counters incrementally as words are committed.
  - **LRU Eviction Cap**: Caps total stored pairs to a maximum of 2,000 entries (configurable) using access-ordered LRU eviction to prevent unbounded memory growth.
  - **Privacy Opt-Out Toggle**: Exposes a `learningEnabled` boolean flag. When set to `false`, new transition recording is completely disabled.
  - **Clear Typing History**: Exposes `clearHistory()` to instantly wipe all user-learned n-gram transition counts.
- **`NextWordPredictor`**: Blends `BigramModel` (generic language) + `PersonalNgramStore` (user habits).
  - **Signal Weighting**: Weights personal transition counts with a score multiplier (`10,000`), allowing user habits (e.g. `thanks` $\rightarrow$ `buddy`) to outrank generic candidates (`thanks` $\rightarrow$ `for`, `you`).
  - **Candidate Output**: Returns top $N$ candidate words (default 4 for UI consistency with the suggestion strip).

---

## Datasets & Provenance

### Unigram Common Word Dictionary (`dictionary_en.txt`)
- **Dataset**: Production-grade English word frequency dataset containing **28,666 clean words** with frequency ranks.
- **Provenance**: Derived from Google Web/Ngram 20k Trillion-Word frequency corpus and SUBTLEX-US spoken language subtitle frequency dataset.
- **License**: Permissive **MIT / Public Domain** open data.
- **Memory Footprint**: $\approx 1.2\text{ MB}$ RAM, sub-millisecond query latency ($< 1\text{ ms}$).

### Proper Nouns Dataset (`dictionary_proper_nouns.txt`)
- **Dataset**: Sourced catalog of **cities, countries, common given names, and consumer brands** (~1,000+ entries).
- **Categories & Scope**:
  - **Cities & Countries**: Sourced from open geographic datasets (GeoNames extracts). Includes all ~195 sovereign countries and major international metropolitan cities.
  - **Personal Given Names**: Sourced from public first-name frequency datasets (US Census / UK ONS / international given name censuses). Excludes surnames to maintain dictionary efficiency.
  - **Consumer Brand Names**: Hand-curated catalog of major consumer tech, automotive, retail, apparel, media, and food brands.
- **Brand Disclaimer**: Including a brand name in this suggestion dictionary is standard practice across operating systems and mobile keyboards for typing convenience. It does NOT imply trademark ownership, endorsement, sponsorship, or affiliation with any brand owner.
- **Frequency Weighting**: Frequencies are assigned conservatively (150 – 450 for standard entries, up to 1,200 – 2,400 for ultra-popular entities like `Google` or `London`). This guarantees that proper nouns never drown out high-frequency common words (e.g., typing `ma` surfaces `make` [7450], `many` [7450], `may` [7450] before `Mark` [2000]).
- **Memory & Performance Footprint**:
  - **Combined Entry Count**: ~29,600+ words.
  - **Trie Load Time**: $\approx 35 - 65\text{ ms}$ (well within the $< 250\text{ ms}$ performance threshold).
  - **Heap Memory Footprint**: $\approx 1.5\text{ MB}$ RAM total in release builds (comfortably within the memory-sensitive `:ime` process budget).

### Bigram Model (`bigrams_en.txt`)
- **Dataset**: Curated English word-pair frequency dataset containing top 2–5 next-word transitions for common English words.
- **Provenance**: Derived from Google Ngram & SUBTLEX-US word pair occurrence frequencies.
- **License**: Permissive **MIT / Public Domain** open data.
- **Memory Footprint & Size Tradeoffs**: Compact $\approx 15\text{ KB}$ text resource ($\approx 150\text{ KB}$ RAM overhead), ensuring zero impact on app binary size or heap footprint while covering the most common conversational word transitions.

---

## Privacy & Security Guarantee

1. **100% On-Device**: All dictionary lookups, personal n-gram history tracking, and next-word score blending operate 100% locally on-device with zero network requests.
2. **No Transmission**: Personal typing history is never written to cloud storage or transmitted off-device under any circumstances.
3. **User Control**: Users can toggle history learning off (`learningEnabled = false`) or wipe recorded transitions completely (`clearHistory()`) at any time.
