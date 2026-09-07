# `:core-autocomplete` — Standalone Autocomplete & Suggestion Engine

`:core-autocomplete` is a pure Kotlin JVM library designed to provide fast, memory-efficient word autocompletion and personalized suggestion generation for Panda Keyboards.

## Architecture

- **`WordTrie`**: A prefix Trie storing words and their frequency ranks. Traverses prefix paths in $O(k)$ time (where $k$ is prefix length) and returns top candidate words ordered by frequency.
- **`SuggestionEngine`**: Generates suggestions for user input prefixes:
  - **Threshold Filtering**: Input shorter than 2 characters returns an empty list to avoid noisy suggestions before sufficient context is typed.
  - **Casing Preservation**: Automatically detects and applies the input prefix's casing style (`LOWERCASE`, `CAPITALIZED`, `ALL_UPPERCASE`, or `MIXED`).
  - **Personalization Blending**: Combines candidates from the static dictionary with user-committed words in `UserDictionary`, boosting user-learned words so custom names and slang rank near the top.
- **`UserDictionary`**: In-memory and extensible user dictionary for recording words typed/committed by the user.
- **`DictionaryLoader`**: Helper for reading line-delimited word frequency dataset files (`dictionary_en.txt`) off the main thread.

## Dictionary Dataset & Size Tradeoffs

- **Dataset**: Bundled English frequency wordlist (`dictionary_en.txt`) covering top ~10,000 common English words ranked by natural frequency.
- **Memory Footprint**: Loading 10,000 words into `WordTrie` consumes approximately **~500 KB of RAM** with instantaneous lookup latency ($< 1\text{ ms}$).
- **Tradeoff Rationale**: Scoping the initial bundled dictionary to ~10k-20k words balances high completion accuracy for 95%+ of daily typing with minimal memory footprint inside the memory-sensitive `:ime` process.

> **Note for Sprint 6a**: This module contains the standalone engine and unit tests only. Keyboard UI integration (suggestion bar above `:ime` layout and Settings auto-correction toggle wiring) will take place in **Sprint 6b**.
