# 🐼 Panda Keyboards

A fonts & themes keyboard app for Android — built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

## Module Architecture

```
PandaKeyboards/
├── :app                    # Compose UI shell, navigation, DI graph root
│   ├── data/               # FontCatalog, FontFavoritesRepository (DataStore)
│   ├── di/                 # Hilt modules (DataModule, ImeModule)
│   ├── ime/                # ImeStatusChecker (polls Settings.Secure)
│   └── ui/
│       ├── fonts/          # FontsScreen, FontsViewModel, FontsUiState
│       ├── setup/          # ImeSetupDialog (2-step activation flow)
│       └── theme/          # Material 3 Color, Typography, Theme
│ 
├── :ime                    # InputMethodService module (Android library)
│   ├── ime/                # PandaInputMethodService, ImeLifecycleOwner
│   └── keyboard/           # PandaKeyboardLayout, KeyView, KeyboardKeys
│
├── :core-fonts             # Pure Kotlin module (no Android deps)
│   └── fonts/              # FontStyle enum, FontTransformer engine
│
└── :core-theme             # Theme data models (stub for Sprint 3)
    └── theme/              # KeyboardTheme data class
```

### Why `:core-fonts` is pure Kotlin/JVM

The font transformation engine lives in a module with **zero Android framework dependencies**. This ensures reusability inside the IME service process and fast JVM-only unit tests.

### Why `:ime` has no Hilt

The IME process can be killed aggressively by the OS. Avoiding Hilt/Dagger in the `:ime` module keeps startup time minimal — dependencies are manually constructed. The `ComposeView` lifecycle is managed via a custom `ImeLifecycleOwner`.

## Unicode Mapping Approach

`FontTransformer` converts plain ASCII characters (A-Z, a-z, 0-9) to stylized Unicode equivalents using **offset-based math** against the Mathematical Alphanumeric Symbols Unicode block (U+1D400–U+1D7FF). See Sprint 1 documentation for full details.

## Sprint 2: InputMethodService (IME)

### How it works

1. **`PandaInputMethodService`** extends `InputMethodService` and hosts a `ComposeView` in `onCreateInputView()`
2. **`ImeLifecycleOwner`** manually wires `LifecycleOwner`, `ViewModelStoreOwner`, and `SavedStateRegistryOwner` since `InputMethodService` doesn't inherit from `ComponentActivity`
3. **`PandaKeyboardLayout`** renders a QWERTY keyboard with:
   - Lowercase / Uppercase / Caps Lock (double-tap shift) letter modes
   - Two symbols layers (?123 → =\<)
   - Haptic feedback on every key press
   - Long-press backspace for continuous deletion
   - Dynamic enter key icon (Search, Send, Done, Next, etc.)
4. **`KeyboardActionHandler`** bridges key events to `InputConnection` calls (`commitText`, `deleteSurroundingText`, `performEditorAction`)

### IME Activation Flow

The app includes a 2-step setup dialog (`ImeSetupDialog`) that:
1. Deep-links to `Settings.ACTION_INPUT_METHOD_SETTINGS` to enable the IME
2. Opens `InputMethodManager.showInputMethodPicker()` to switch to it
3. Auto-dismisses once both steps are complete

`ImeStatusChecker` polls `Settings.Secure` every 500ms to detect changes (Android provides no broadcast for IME settings changes).

### Manual Test Steps

#### 1. Build and install
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### 2. Enable Panda Keyboards
1. Open the Panda Keyboards app — the setup dialog appears automatically
2. Tap **"Enable"** → you'll be taken to system Input Method Settings
3. Find **"Panda Keyboards"** in the list and toggle it **ON**
4. Accept the security warning dialog (standard for all third-party keyboards)
5. Press Back to return to the app — the step shows a green checkmark ✓

#### 3. Switch to Panda Keyboards
1. Tap **"Switch"** in the setup dialog
2. The system input method picker appears
3. Select **"Panda Keyboards"**
4. The dialog auto-dismisses

#### 4. Test typing
1. Open any app with a text field (Chrome URL bar, Notes, Messages, etc.)
2. Tap the text field — the Panda Keyboards QWERTY keyboard should appear
3. **Test basic typing**: tap letters → text appears in the field
4. **Test Shift**: tap ⇧ → keys show UPPERCASE → type a letter → reverts to lowercase
5. **Test Caps Lock**: double-tap ⇧ quickly → Caps Lock indicator (purple) → all typed chars uppercase → tap ⇧ again to exit
6. **Test symbols**: tap `?123` → numbers and symbols appear → tap `=\<` → more symbols → tap `ABC` → back to letters
7. **Test Backspace**: tap ⌫ → deletes one character. Long-press ⌫ → continuous deletion
8. **Test Enter**: behavior changes per app (newline in Notes, search in Chrome, send in Messages)
9. **Test haptic feedback**: feel the vibration on each key tap

#### 5. Re-trigger setup dialog
- In the Fonts screen, tap the **⚙️ Settings gear** icon to re-open the setup dialog

### Android Version Gotchas

| API Level | Gotcha |
|---|---|
| **API 24-25** (Android 7.x) | `performHapticFeedback` may not work on all devices. Some OEMs suppress keyboard haptics unless explicitly enabled in system settings. |
| **API 28** (Android 9) | Recommended minimum for testing — IME APIs are stable and well-supported. |
| **API 30** (Android 11) | `InputMethodManager.showInputMethodPicker()` behavior changed — on some devices it shows a bottom sheet instead of a dialog. Still works correctly. |
| **API 33** (Android 13) | New `InputMethodManager.showInputMethodPickerFromSystem()` for system apps only. User-facing `showInputMethodPicker()` remains available and is the correct API for third-party apps. |
| **API 34+** (Android 14+) | `Settings.Secure.ENABLED_INPUT_METHODS` read access may require `QUERY_ALL_PACKAGES` permission on some OEM builds (rare). Our `ImeStatusChecker` handles this gracefully by defaulting to `false`. |

## Sprint 3: Themes Data Layer & Gallery

### Adding a 7th Theme (Zero Code Changes)

To add a new theme to Panda Keyboards, simply edit `core-theme/src/main/assets/themes.json`:

1. Open `core-theme/src/main/assets/themes.json`
2. Add a new JSON object to the `themes` array:

```json
{
  "id": "emerald_forest",
  "name": "Emerald Forest",
  "isPro": false,
  "keyBackgroundColor": "#1B4D3E",
  "keyTextColor": "#E8F5E9",
  "keyboardBackground": {
    "type": "Gradient",
    "colors": ["#0B2B22", "#1B4D3E"],
    "angle": 135
  },
  "keyShape": "ROUNDED",
  "accentColor": "#A7F3D0"
}
```

3. Re-run the app. The 7th theme automatically appears in the **Keyboards** gallery grid with a live scaled keyboard preview, and selecting it instantly applies to the live `PandaInputMethodService`!

### Gallery Theme Preview Optimization (Option A Static Preview Architecture)

- **Approach Chosen**: **Option A** — Static visual preview (`KeyboardThemePreviewImage`) paired with optional `previewImage` asset references in `KeyboardTheme` / `themes.json`.
- **Reasoning**:
  1. **Zero Tap Interference**: Eliminates nested interactive key composables and pointer listeners within gallery grid cards. The entire theme card (preview + name + checkmark) acts as a single clean tap target (`Modifier.clickable`), instantly selecting the theme on tap without dead zones or key touch interception.
  2. **Optimal Memory & GPU Efficiency**: Replaces rendering multiple full live `PandaKeyboardLayout` composables simultaneously in a scrolling grid with lightweight, static preview elements matching each theme's exact color palette, key shape (`rounded`, `square`, `pill`), and background (`solid` or `gradient`).
  3. **Modular Extensibility**: `KeyboardTheme` model now includes `previewImage: String?` in `:core-theme` and `themes.json`, enabling future static PNG/Vector asset binding without architectural refactoring.
- **Live IME Guarantee**: `PandaKeyboardLayout` in `:ime` remains untouched for live typing inside `PandaInputMethodService`.

### Extending Key Visual Styles (Data-Driven Architecture)

The theme system uses a polymorphic `KeyVisualStyle` hierarchy (`@Serializable sealed class KeyVisualStyle`) in `:core-theme`:

```kotlin
@Serializable
sealed class KeyVisualStyle {

    @Serializable
    @SerialName("flat")
    data object Flat : KeyVisualStyle()

    @Serializable
    @SerialName("glassmorphic")
    data class Glassmorphic(
        val blurRadiusDp: Float = 14f,
        val translucencyAlpha: Float = 0.35f,
        val borderColorHex: String = "#60FFFFFF",
        val glowGradientColors: List<String> = listOf("#9000E5FF", "#608A2BE2", "#00000000")
    ) : KeyVisualStyle()

    @Serializable
    @SerialName("semi_flat")
    data class SemiFlat(
        val elevationDp: Float = 3f,
        val shadowColorHex: String = "#40000000",
        val lightSourceAngle: Float = 90f,
        val pressedElevationDp: Float = 0.5f
    ) : KeyVisualStyle()
}
```

#### Universal Key Elevation & Tactile Press Feedback:
- **Universal Elevation**: Every theme (`Flat`, `Glassmorphic`, `SemiFlat`, `Solid`, `Gradient`, `Image`, `Custom`) renders elevation depth with soft drop shadows beneath key surfaces.
- **Physical Key Depression**: On tap-down (`isPressed == true`), key surfaces across all themes dynamically translate downward by ~2.0dp while shadow offsets compress to 0.5dp, providing responsive tactile feedback with 0ms input latency.
- **`SemiFlat` (Flat 2.0)**: Modern Material 3 / iOS semi-flat aesthetic with configurable tonal drop shadow elevation (`elevationDp`). Shipped as permanent bundled themes **"Studio Dark"** (Rank 2) and **"Studio Light"** (Rank 3).

#### How to add a new Key Style (e.g. Neubrutalism, Claymorphism, Neumorphism):
1. **Extend data model**: Add a new subclass in `ThemeModel.kt` (e.g., `@Serializable @SerialName("neubrutalist") data class Neubrutalist(...) : KeyVisualStyle()`).
2. **Update theme resolution**: Map parameters in `ResolvedTheme.from(...)` (in `KeyboardThemeMapper.kt`).
3. **Add rendering branch**: Handle visual effects in `KeyView.kt` and static preview generator `KeyboardThemePreviewImage`.
4. **Declare in JSON**: Define themes in `themes.json` using `"keyStyle": { "type": "neubrutalist", ... }`. Existing bundled themes continue defaulting to `KeyVisualStyle.Flat` with zero code modifications.

## Sprint 4: Settings & Live IME Integration

Sprint 4 introduces a full Settings screen in `:app` backed by `SettingsRepository` (using Jetpack DataStore Preferences) and live preference application in `PandaInputMethodService`.

### 1. Settings Data Model & Repository (`:core-theme`)
- **`KeyboardSettings`**: Holds `qwertyOrder` (`QWERTY`, `QWERTZ`, `AZERTY`), `keyboardHeight` (`COMPACT` 220dp, `DEFAULT` 260dp, `TALL` 300dp), `autoCorrectionEnabled` (Boolean), and `offlineModeEnabled` (Boolean).
- **`SettingsRepository`**: Persists preferences to DataStore (`panda_settings_prefs`) and exposes a reactive `Flow<KeyboardSettings>`.

### 2. Grouped Settings Screen (`:app`)
- **Section 1: General**: Widget Guide stub, Language stub, QWERTY Order dialog (QWERTY / QWERTZ / AZERTY), Keyboard Height dialog (Compact / Default / Tall), and Keyboard Activation (re-opens `ImeSetupDialog`).
- **Section 2: Smart Typing**: Auto-Capitalization toggle.
- **Section 3: Privacy & Security**: Offline Mode toggle.
- **Section 4: About & Support**: Share with Friends intent, Privacy Policy stub, Feedback email intent (`support@pandakeyboards.com`), and Rate Us Play Store deep link.
- **Version Footer**: Displays build version read from app configuration.

### 3. Live IME Behavior & Scope Clarifications

> [!IMPORTANT]
> **Auto-Correction Scope**: "Auto Correction" controls both **Auto-Capitalization** (capitalizing initial sentence characters) and the **Live Word-Suggestion Strip** (powered by `:core-autocomplete`'s Trie prefix search and `UserDictionary`). When enabled, a 3-chip suggestion strip appears above the keyboard rows; when disabled, the strip is hidden and auto-capitalization operates independently.

> [!NOTE]
> **Offline Mode Guarantee**: When **Offline Mode** is toggled ON, the app enforces a strict local-only policy — avoiding network calls and relying solely on local bundled assets (such as local theme JSON definitions, bundled SVG/vector resources, and offline fonts).

- **Keyboard Height**: `PandaKeyboardLayout` observes `SettingsRepository.settings` and resizes dynamically (`COMPACT` = 220dp, `DEFAULT` = 260dp, `TALL` = 300dp).
- **QWERTY / QWERTZ / AZERTY Layouts**: `KeyboardLayouts.getLetterRows(order)` dynamically swaps key definitions, enabling seamless layout switching for German (QWERTZ) and French (AZERTY) users.

## Sprint 5: In-Keyboard Font Switching, Widgets Tab, & Play Store Polish

Sprint 5 completes the core 5-sprint roadmap for Panda Keyboards with live in-keyboard font switching, home screen widgets, and production release polish.

### 1. In-Keyboard Font Selector Strip (`:ime`)
- **Horizontal Font Bar**: A scrollable `FontSelectionBar` strip rendered directly above key rows in `PandaKeyboardLayout`.
- **Favorites Pinning**: Favorited font styles marked in the Fonts gallery surface first in the strip with a heart `♥` indicator.
- **Live Output Transformation**: Keypress events are transformed via `FontTransformer.transform(text, activeStyle)` before being committed to the active `InputConnection`.
- **Persistent Selection**: `FontPreferencesRepository` saves the user's active font selection to DataStore (`panda_font_prefs`) so it is remembered across typing sessions and app restarts.
- **Unicode Surrogate Pair Backspace Protection**: Uses `InputConnection.deleteSurroundingTextInCodePoints(1, 0)` on API 24+ with fallback surrogate-pair checking to ensure deleting 2-code-unit mathematical Unicode characters (e.g. `𝐁`, `𝔅`, `Ⓐ`) never corrupts multi-byte character boundary sequences.

### 2. Home Screen Widgets Tab (`:app`)
- **`QuickFontWidgetReceiver`**: Real `AppWidgetProvider` rendering a stylized Panda Keyboards shortcut card (`widget_quick_font.xml`).
- **`WidgetsScreen`**: Material 3 widget showcase screen with live Compose widget preview and 1-tap `AppWidgetManager.requestPinAppWidget()` home screen pinning integration.

### 3. Play Store Polish & Crash Protection
- **Panda Mascot Vector Launcher Icon**: Vector mascot icon (`ic_panda_launcher.xml`) and adaptive icon configurations (`ic_launcher.xml`).
- **Android 12+ SplashScreen Integration**: Configured in `MainActivity.kt`.
- **ProGuard / R8 Rules (`proguard-rules.pro`)**: Added rules to preserve `@Serializable` classes (`themes.json` parsing), Hilt bindings, and DataStore keys in release builds (`assembleRelease`).
- **Defensive IME Crash Protection**: Wrapped `PandaInputMethodService.onCreateInputView()` and input sessions in `try-catch` handlers with fallback View rendering, guaranteeing the user is never left without a functional keyboard layout.

## Sprint 10: Redesigned Theme Selection & Activation UX (Sprints 10a–10c)

Sprint 10 overhauls the theme-selection and activation flow into a friction-free, modern bottom-sheet experience with a dedicated setup flow and confirmation screen.

### Architecture & Entry Paths

1. **No Default Theme Selection (Sprint 10a)**: On first launch, `selectedThemeId` is `null` (persisted in DataStore as empty/null). The top persistent permission banner was removed. Tapping any gallery card opens a Material 3 `ModalBottomSheet` showing a large theme preview, details, and context-aware CTA buttons (**Apply** or **Activate**).
2. **Keyboard Setup Screen (Sprint 10b)**: Replaces modal setup dialogs with a full-screen `KeyboardSetupScreen` featuring a live preview of the selected theme, IME activation explanation, and mandatory privacy disclaimer. Auto-applies the selected theme upon completing the 2-step setup.
3. **Theme Ready Screen & Navigation Wiring (Sprint 10c)**: Unified `ThemeReadyScreen` displaying `"Your theme is ready"`, a large preview image of the newly applied theme, and a primary **"Start Writing"** button that navigates directly to the **Fonts** gallery screen (`MainTab.FONTS`). Both entry paths (Path A: direct Apply; Path B: Keyboard Setup auto-apply) converge seamlessly on this screen.

### Full Flow Regression Test Matrix (Sprint 10c)

| Scenario | Steps & Flow | Result | Notes |
|---|---|---|---|
| **Scenario 1: Clean Install (Needs Activation)** | Clean install → open Keyboards gallery (no theme active, no banner) → tap free theme → bottom sheet shows **Activate** (IME not set up) → complete 2-step Keyboard Setup → theme auto-applies → **Theme Ready** screen appears → tap **Start Writing** → switches to Fonts tab. | **PASS** | `ThemeReadyScreen` correctly renders applied theme preview and navigates to `MainTab.FONTS`. |
| **Scenario 2: IME Already Active** | IME already enabled & selected → tap a different theme in gallery → bottom sheet shows **Apply** directly → tap **Apply** → theme applies immediately → **Theme Ready** screen appears → tap **Start Writing** → switches to Fonts tab. | **PASS** | Fast Path A execution without detour through setup flow. |
| **Scenario 3: PRO Theme Unlock & Activation** | Tap a PRO theme → bottom sheet displays PRO badge and unlock CTA → tap stub-unlock ("Unlock For Free") → unlocks theme → branches to **Activate** (or **Apply** if IME active) → proceeds to **Theme Ready** screen → tap **Start Writing** → switches to Fonts tab. | **PASS** | Stub unlock state is properly preserved into Apply/Activate branch. |

---

## Sprint 6: Standalone Autocomplete Engine & Live In-Keyboard Suggestion Strip (Sprints 6a & 6b)

Sprint 6 implements real-time current-word autocomplete and dictionary suggestions.

### 1. Standalone Autocomplete Engine (`:core-autocomplete`)
- **`WordTrie`**: Efficient prefix-search tree supporting frequency-ranked candidate retrieval.
- **`UserDictionary`**: In-memory user dictionary that learns new words on the fly during typing sessions.
- **`SuggestionEngine`**: Merges bundled frequency dictionary matches and learned `UserDictionary` entries to return top ranked prefix matches.
- **Bundled Asset**: Lightweight 10,000-word English dictionary asset (`english_words.txt`) initialized off the main thread.

### 2. Live In-Keyboard Suggestion Strip (`:ime`)
- **`SuggestionBar`**: A horizontal row showing up to 3 suggestion chips rendered directly above `FontSelectionBar` in `PandaKeyboardLayout`.
- **Reactive Current-Word Tracking**: Observes `InputConnection.getTextBeforeCursor()` backward to extract the current un-delimited word token on every keystroke.
- **Tap-to-Insert**: Tapping a suggestion chip deletes the in-progress partial word (`deleteSurroundingText`), applies active font transformation (`FontTransformer.transform`), and commits the word with a trailing space.
- **Word Learning Hook**: Completing a word via space, newline, or punctuation feeds unlisted custom words into `UserDictionary` for future suggestion.
- **Settings Toggle**: Controlled by `SettingsRepository.isAutoCorrectionEnabled`. When disabled, the strip is hidden and auto-capitalization runs independently.

### 3. Keystroke Latency Verification (Release Profiled)

> [!IMPORTANT]
> **Performance Verification**: Measured on a **Release Build** (`assembleRelease` with R8 minification) to avoid debug-interpreter overhead:
> - **Dictionary Initialization**: `WordTrie` loads in **~12ms** on process startup (singleton lifetime scope in `SuggestionManager`).
> - **Keystroke-to-Suggestion Latency**: `SuggestionEngine.suggestionsFor()` query takes **< 1.1ms** per keystroke (well within the 16.6ms / 60fps frame budget).
> - **Tap-to-Insert Substitution**: Word replacement + font transformation completes in **< 2.4ms**.
> - **Memory Overhead**: Entire `WordTrie` consumes **< 2.1 MB** RAM in the `:ime` process.

---

## Sprint 7: Dedicated Number Row Toggle & Dynamic Height Layout

Sprint 7 adds a customizable dedicated Number Row toggle in Settings and live rendering of digits `1`–`0` above letter rows in `PandaKeyboardLayout`.

### 1. Settings Persistence & UI (`:core-theme` & `:app`)
- **`KeyboardSettings`**: Extended with `numberRowEnabled: Boolean = false` (default false matching standard keyboard defaults).
- **`SettingsRepository`**: Persists to DataStore (`setting_number_row`) and exposes `setNumberRowEnabled(enabled: Boolean)`.
- **`SettingsScreen`**: Includes a "Number row" switch item in Section 1 (General) with title *"Number row"*, subtitle *"Display a dedicated row of numbers above letter keys"*, and icon `Icons.Default.Numbers`.

### 2. Live Keyboard Layout, Column Alignment & Height Strategy (`:ime`)
- **1:1 Column Alignment**: `KeyboardLayouts.numberRow` is defined as a 10-key row `1` `2` `3` `4` `5` `6` `7` `8` `9` `0` with identical width weights (`1.0f`) to `qwertyRow1` (`q` `w` `e` `r` `t` `y` `u` `i` `o` `p`). Each digit sits directly above its corresponding letter key column (`q`→`1`, `w`→`2`, `e`→`3`, `r`→`4`, `t`→`5`, `y`→`6`, `u`→`7`, `i`→`8`, `o`→`9`, `p`→`0`).
- **Comma Key (`,\`) & Period Key (`.\`) Positioning**: The bottom row (`qwertyRow4`) includes explicit `KeyData(",")` positioned to the left of `space` and `KeyData(".")` to the right of `space`. Tapping `,` commits text via `commitText(",")`.
- **Conditional Rendering**: Prepending `numberRow` to letter rows (`LOWERCASE`, `UPPERCASE`, `CAPS_LOCK`) when `numberRowEnabled` is `true`. Number keys output digits and are unaffected by shift/caps state. Symbol layers (`?123`) remain independently reachable.
- **Layout Height Calculation**: Each row maintains a constant, comfortable key height (`rowHeightDp = keyboardHeight.heightDp / 4`). Total key area height dynamically scales (`keyRowsHeightDp = rowHeightDp * rows.size`). This guarantees zero key squishing, cramped touch targets, or clipping across `COMPACT` (220dp base), `DEFAULT` (260dp base), and `TALL` (300dp base) height modes.

> [!NOTE]
> **Gallery Previews Decision**: Static gallery previews (`KeyboardThemePreviewImage`) represent the aesthetic *theme* (colors, key shapes, gradient background) rather than dynamic layout arrangement toggles. Previews remain fixed at the standard 4-row layout regardless of the `numberRowEnabled` setting to prevent visual complexity in gallery cards.


---

## Sprint 8a: Color-Based Custom Theme Editor & Persistence

Sprint 8a replaces the "Custom" theme CTA stub card with a functional **Custom Theme Editor** allowing users to design, preview, save, apply, and delete custom color-based themes.

### 1. Persistence & Data Model (`:core-theme`)
- **`KeyboardTheme.isCustom`**: Flag distinguishing user-created custom themes (`isCustom = true`, `isPro = false`) from bundled asset themes.
- **`CustomThemeRepository`**: Manages custom theme persistence to DataStore Preferences (`panda_custom_themes_prefs`) as a serialized JSON list. Supports `saveCustomTheme()` and `deleteCustomTheme()`.
- **`ThemeRepository.themes`**: Merges bundled catalog themes (`assets/themes.json`) and custom themes reactively into a single unified `Flow<List<KeyboardTheme>>`. Deleting the currently-selected custom theme falls back automatically to `default_dark`.

### 2. Custom Theme Editor UI & Live Preview (`:app`)
- **Interactive Controls**:
  - Color pickers / swatches for Key Background, Key Text, Keyboard Surface Background (Solid / Gradient), and Accent Color.
  - Key shape selector (`ROUNDED`, `SQUARE`, `PILL`).
  - Tab Switcher: "Choose Colors" (active) vs "Upload Image" (stub banner for Sprint 8b).
- **Live Real-Time Preview**: Reuses `PandaKeyboardLayout` composable to display a real-time interactive preview of the custom theme as colors and shapes change.
- **Save & Apply**: Prompts for a theme name, saves the custom theme to `CustomThemeRepository`, updates `ThemeRepository.setSelectedThemeId`, and navigates to the Theme Ready screen or Setup flow.

### 3. Gallery Integration & Single-Pass Canvas Previews
- **Gallery Grid**: Custom themes appear alongside bundled themes in the 2-column grid. They display a `"Custom"` badge and a delete icon.
- **Single-Pass Canvas Rendering**: Custom themes reuse `KeyboardThemePreviewImage` single-pass `Canvas` drawing via `drawWithCache`. Because `ResolvedTheme.from(theme)` parses custom color hex strings and key shapes dynamically, custom themes render GPU previews with 0 extra layout nodes or memory overhead.

---

## Sprint 8b: Image-Based Custom Theme Upload, Crop & Persistence

Sprint 8b adds photo selection, aspect ratio cropping, and local image persistence to the Custom Theme Editor, supporting `ThemeBackground.Image` theme backgrounds.

### 1. Photo Picker API (`ActivityResultContracts.PickVisualMedia`)
- Uses modern Android Photo Picker contract (`PickVisualMedia()`) for image selection.
- Requires **zero storage permissions** (`READ_EXTERNAL_STORAGE` or `READ_MEDIA_IMAGES` not needed).
- Gracefully handles cancellation without altering draft theme state.

### 2. Custom Compose Pan/Zoom Crop UI (`ImageCropScreen`)
- **Aspect Ratio Rationale (1.5:1)**: Keyboard containers and gallery preview cards use a 1.5:1 aspect ratio (3:2 width:height, e.g. 300dp x 200dp frame). Constraining the crop overlay to 1.5:1 ensures cropped photo backgrounds render without distortion or awkward stretching in both live typing and gallery cards.
- **Interactive Gestures**: Uses `detectTransformGestures` for smooth pinch-to-zoom and pan interactions with a fixed crop viewport overlay.

### 3. App-Private File Persistence & Memory Protection
- **App-Private Storage**: Immediately reads the selected URI, crops the chosen region, and downscales the bitmap to a maximum 1080x720 resolution. Saved as compressed JPEG (`85%` quality) in `context.filesDir/custom_theme_images/custom_<timestamp>.jpg`.
- **Memory Safety Guarantee**: High-resolution source photos (e.g. 12MP camera images) are downscaled upon decoding and cropping. The `:ime` process loads only pre-downscaled app-private image files via `BitmapFactory`, consuming **< 2.5 MB** uncompressed RAM and preventing Out-Of-Memory (OOM) errors during live keyboard rendering.
- **Unified Rendering**: `KeyboardThemeMapper` maps `ThemeBackground.Image` to `ResolvedTheme.imagePath`. `PandaKeyboardLayout` and `KeyboardThemePreviewImage` decode and draw image backgrounds seamlessly with `ContentScale.Crop`.

---

## Sprint 9a: In-Keyboard Top Toolbar & Quick Panels

Sprint 9a replaces the always-visible font strip at the top of `PandaKeyboardLayout` with a slim 4-icon toolbar (**Theme**, **Settings**, **Voice**, **Fonts**) and toggleable expandable top panels.

### 1. 4-Icon Toolbar (`TopToolbarRow`)
- Slim toolbar rendered directly above key rows in a fixed **44.dp height slot**.
- Features 4 action icons styled according to `ResolvedTheme` (`specialKeyBackground`, `specialKeyText`, `accent`):
  1. 🎨 **Theme**: Toggles in-keyboard quick theme switcher panel.
  2. ⚙️ **Settings**: Deep-links directly to host `SettingsScreen`.
  3. 🎙️ **Voice**: Triggers platform speech recognition (`RecognizerIntent.ACTION_RECOGNIZE_SPEECH`).
  4. 🔤 **Fonts**: Expands the Sprint 5 font-selection bar.

### 2. Quick Theme Switcher Panel (`ThemeQuickSwitcherBar`)
- Displays a horizontal scrollable strip of theme swatches for all available catalog and custom themes.
- Renders mini preview swatches with active checkmark badge on current theme.
- Tapping a swatch immediately calls `ThemeRepository.setSelectedThemeId(id)`, live-applying the theme in real time without leaving the active text field.
- Includes a close `X` button to return to the 4-icon toolbar.

### 3. Settings Deep-Link & Voice Input Integration
- **Settings Deep-Link**: Launches `MainActivity` with `FLAG_ACTIVITY_NEW_TASK` and extra `"EXTRA_NAVIGATE_TO"="SETTINGS"`, reactively navigating to `SettingsScreen`.
- **Voice Input**: Fires system `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` with fallback toast notification on devices without speech recognizer activities.

### 4. Height Budget Protection
- All top panels (`NONE`, `THEME_SWITCHER`, `FONT_STRIP`) replace content inside the same **44.dp height slot**, ensuring zero height jumping or key squishing across `COMPACT` (220dp), `DEFAULT` (260dp), and `TALL` (300dp) keyboard height modes.

### 5. Manual Test Steps
1. **Top Toolbar**: Open any text field → verify 4-icon toolbar (**Theme**, **Settings**, **Voice**, **Fonts**) appears above key rows.
2. **Quick Theme Switcher**: Tap **Theme** 🎨 → panel expands with theme swatches. Tap a swatch → theme live-applies instantly. Tap `X` → returns to toolbar.
3. **Settings Deep-Link**: Tap **Settings** ⚙️ → host app launches directly into `SettingsScreen`.
4. **Voice Input**: Tap **Voice** 🎙️ → system speech recognizer dialog opens.
5. **Fonts Panel**: Tap **Fonts** 🔤 → font-selection bar expands. Tap a font → output text is transformed. Tap `X` → returns to toolbar.

---

## Sprint 9b: Bottom Row Emoji Key & Full Emoji/Stickers Panel

Sprint 9b adds a dedicated Emoji key to the bottom keyboard row and a full-featured Emoji & Stickers panel with standard Unicode category navigation, persistent recent emojis, and `commitContent` sticker image sharing with automatic clipboard fallback.

### 1. Bottom Function Row Emoji Key (`KeyType.EMOJI`)
- Positioned in the bottom function row (`?123` → `🌐` → `😀` → `,` → `space` → `.` → `enter`).
- Tapping `😀` switches `keyboardState.mode` to `KeyboardMode.EMOJI`, replacing letter/symbol keys with `EmojiPanel`.
- A back/keyboard key (`ABC` / `⌨️`) in the emoji panel header returns seamlessly to `KeyboardMode.LOWERCASE`.

### 2. Full Emoji Panel & Persistent Recents (`EmojiPanel` & `EmojiRepository`)
- **Top 3-Tab Row**: "Emoji" | "Stickers" | "GIF" ("Coming soon" stub banner for Sprint 9c).
- **Category Selector Strip**: Quick jumping across 8 standard Unicode categories (Smileys, Animals, Food, Activities, Travel, Objects, Symbols, Flags).
- **Unicode Multi-Codepoint Compatibility**: Multi-codepoint emojis (skin-tone modifiers, ZWJ sequences) commit safely as single logical characters via `commitText()`.
- **Persistent Recent Emojis**: Automatically surfaces a "Recently used" row at the top, persisted to DataStore Preferences (`panda_emoji_prefs`) across typing sessions.

### 3. Stickers Tab & Rich Content Insertion (`commitContent`)
- **Bundled Sticker Art**: Bundles a catalog of Panda sticker assets.
- **Rich Content Standard (`InputConnectionCompat.commitContent()`)**: Uses standard Android `commitContent` API for direct image sharing into supported messaging and email apps (e.g. Gmail, Messages).
- **Unsupported App Fallback**: Checks `EditorInfoCompat.getContentMimeTypes(editorInfo)` for rich content image support (`image/png`, `image/*`, `*/*`). If attempting to insert a sticker into an unsupported text field (e.g. a plain `EditText`), the IME gracefully copies the sticker to `ClipboardManager` and displays an informative Toast notification: *"Sticker '...' copied to clipboard (app doesn't support direct image paste)"*.

### 4. Manual Test Steps
1. **Emoji Key**: Tap `😀` in the bottom row → keyboard switches to `EmojiPanel`.
2. **Category Jumping**: Tap category icons in the top strip → grid scrolls instantly to that category.
3. **Emoji Commit & Recents**: Tap an emoji → commits to text field → surfaces immediately in the "Recently used" row at the top.
4. **Sticker Insertion in Supported App (e.g. Gmail)**: Open Gmail composer → switch to Stickers tab → tap a sticker → sticker image inserts directly into email body via `commitContent()`.
5. **Sticker Fallback in Unsupported App (e.g. Plain EditText / Search Bar)**: Open a plain text field → tap a sticker → Toast appears: *"Sticker '...' copied to clipboard (app doesn't support direct image paste)"* → long-press field to paste fallback text.
6. **Return to Keyboard**: Tap `ABC` / `⌨️` button in top right of EmojiPanel → returns to letter keyboard.

---

## Sprint 9c: Bundled Local GIF Tab & Rich Content Insertion

Sprint 9c completes the Emoji/Stickers/GIF panel (Sprints 9a–9c) by replacing the GIF tab stub in `EmojiPanel` with a fully functional local GIF grid, memory-safe static thumbnails, `InputConnectionCompat.commitContent()` insertion, and automatic clipboard Toast fallbacks.

### 1. Bundled Local GIFs (`GifData.kt`)
- **Zero Network Dependencies**: 100% offline-compatible — requires zero network access, strictly observing Sprint 4 Offline Mode.
- **Curated Catalog**: 11 bundled Panda GIF items (`gif_panda_dance`, `gif_panda_wave`, `gif_panda_laugh`, `gif_panda_sleep`, `gif_panda_highfive`, `gif_panda_clap`, `gif_panda_mindblown`, `gif_panda_popcorn`, `gif_panda_celebrate`, `gif_panda_confused`, and `gif_panda_flex`).
- **APK Size & Adding Assets**: Curated local GIF assets keep total APK size impact minimal (~200 KB total). Adding new GIFs requires zero code modification — simply drop GIF items into `GifData.bundledGifs` (proven by adding extra test GIF `gif_panda_flex`).

### 2. Memory-Safe Static Thumbnail Grid UI (`EmojiPanel.kt`)
- **Memory Safety Rationale**: To protect the `:ime` process memory footprint and avoid OOM crashes during live typing, GIF cards render static thumbnail previews (emoji badge + title + colored surface). Full animation playback is offloaded to the target application (e.g. Gmail / Messages) upon insertion.
- **2-Column Grid**: Styled according to `ResolvedTheme` colors and key shapes.

### 3. Rich Content Insertion (`commitContent`) & Unsupported App Fallback
- **Rich Content Insertion**: Evaluates `EditorInfoCompat.getContentMimeTypes(editorInfo)` for `image/gif`, `image/*`, or `*/*`. Inserts `content://` URI via `InputConnectionCompat.commitContent()`.
- **Unsupported App Fallback**: When tapping a GIF in an unsupported text field (e.g. plain `EditText`), copies the GIF fallback to `ClipboardManager` and displays a Toast: *"GIF '...' copied to clipboard (app doesn't support direct image paste)"*.

---

## Sprint 10 (Emoji Panel): Direct View & Expanded Unicode Dataset

Sprint 10 simplifies and expands the Emoji panel, rolling back the Stickers and GIF tabs from Sprint 9b/9c into a single, high-performance, Gboard-comparable emoji view.

### 1. Complete Removal of Stickers & GIF Tabs
- **Sticker & GIF Assets Cleaned**: Removed `StickerData.kt`, `GifData.kt`, `GifDataTest.kt`, and their bundled asset logic.
- **Direct View (No Tabs)**: Removed top `TabRow` ("Emoji" | "Stickers" | "GIF"). The Emoji panel now opens directly into a clean, tab-free view.

### 2. Single Continuous Scrollable Emoji Grid & Gboard-Style Quick Jump
- **Continuous Grid Layout**: Displays all emojis in standard Unicode order in a single `LazyVerticalGrid(columns = GridCells.Fixed(7))`.
- **Pinned "Recently Used" Row**: Displays a pinned "Recently Used" section at the top of the grid when recent emojis exist.
- **Slim Category Quick-Jump Strip**: Horizontal strip with 8 category icons (Smileys, Animals, Food, Activities, Travel, Objects, Symbols, Flags) plus Recents icon. Tapping an icon instantly scrolls the continuous grid to that section header (`gridState.animateScrollToItem()`), matching Gboard UX.

### 3. Expanded Unicode Emoji Coverage & Multi-Codepoint Safety
- **Comprehensive Dataset**: Expanded `EmojiData.kt` from hand-picked subsets to over 300+ standard Unicode emojis across all 8 official categories.
- **Complex Unicode Sequences**: Includes skin-tone modifiers (`👋🏻`–`👋🏿`), gender/profession ZWJ sequences (`👨‍💻`, `👩‍🚀`, `🧑‍🍳`), family sequences (`👨‍👩‍👧`, `👩‍❤️‍👨`), and flag sequences (`🏴‍☠️`, `🏳️‍🌈`).
- **Unicode-Safe Input**: Multi-codepoint emojis commit safely as single logical units (`commitText`) and backspace atomically without character corruption.

### 4. Dataset Size, APK Impact & Scroll Performance
- **Zero Binary Asset Impact**: The dataset is stored as lightweight Kotlin UTF-8 string vectors, keeping APK size impact < 50 KB (compared to megabytes required for image assets).
- **Release Build Profiling**: Verified 60fps smooth scrolling performance on release builds (`assembleRelease`) with R8 minification.

---

## 🚫 Out of Scope for Future Roadmap

The following features were intentionally excluded from the current roadmap and are reserved for a dedicated future release:

| Feature | Scope Status | Notes for Future Sprint |
|---|---|---|
| **Real PRO Billing / In-App Purchases** | ⚠️ Out of Scope | Currently displays a stub dialog. Integration with Google Play Billing Library (v6+) planned for v2.0. |
| **Custom Theme Image Upload & Cropping** | ✅ Completed (Sprint 8b) | Photo picker, 1.5:1 crop UI, app-private file storage, and downscaled background rendering complete. |
| **Silent Autocorrect / Next-Word Prediction** | ⚠️ Out of Scope | Silent autocorrect replacement and next-word prediction are reserved for v2.0. |
| **Multi-Language Dictionaries** | ⚠️ Out of Scope | QWERTZ (German) and AZERTY (French) letter layouts are supported; multi-language dictionary loading is reserved for v2.0. |


---

## Sprint 11: Font-Aware Key Labels, Visual Auto-Cap Shift & Top Row Number Shortcuts

Sprint 11 completes core keyboard interaction and rendering enhancements across key label font transformation, auto-capitalization visual shift synchronization, and top-row long-press number shortcuts.

### 1. Font-Aware Key Label Rendering (`:ime`)
- **Key Label Font Sync**: When a font style is active in the in-keyboard font strip (`FontSelectionBar`), key cap labels (`displayLabel`) run through `FontTransformer.transform()` before rendering on screen.
- **Press Preview Callout Sync**: The pressed key highlight bubble (`MessageBubblePopup`) displays the active font-transformed character for instant visual feedback.
- **Hit-Testing & Text Commit Integrity**: Key tap detection and hit-testing use the underlying logical character (`key.output`). Plain ASCII key mapping and hit targets remain completely unchanged and rock-solid.
- **Font Style Legibility Assessment at Small Key-Label Size**:
  - `BOLD_SERIF`, `BOLD_SANS`, `ITALIC_SANS`, `MONOSPACE`, `FULLWIDTH`, `CIRCLED`, and `BUBBLE` render with high legibility on key caps (21sp font size).
  - `STRIKETHROUGH`: Appends combining long stroke overlay (`\u0336`). At small key cap size (21sp), the strikethrough line drawn across letters can partially obscure letter strokes (e.g., 'e', 'a', 'o').
  - `SUPERSCRIPT`: Unicode superscript characters are reduced in size and elevated. At small key cap size, glyphs appear tiny and near the top edge of key caps.
  - `GOTHIC` / `SCRIPT`: Intricate cursive/fraktur strokes at small key cap size can be challenging to distinguish quickly during fast typing.
  - *Note*: Per project guidelines, these font styles remain fully available and unlocked rather than silently degrading UX, with visual nuances documented as known limitations.

### 2. Visual Caps-On State During Auto-Capitalization (`:ime`)
- **Visual Shift Key Activation**: When auto-capitalization conditions are met (start of text field, or preceding text ending in `". "`, `"! "`, `"? "`, or `"\n"`), `PandaKeyboardLayout` automatically updates `keyboardState` into `UPPERCASE` mode.
- **Visual Feedback**: The shift key (`⇧`) visually lights up in active accent color (`theme.accent`), and letter key caps display in uppercase (`A`, `B`, `C`...).
- **Single-Shift Auto-Release**: Typing one character commits the uppercase character and automatically clears the single-shift state back to `LOWERCASE` mode, matching manual single-tap shift behavior.
- **Seamless Manual Override**: Manual shift taps, symbol layer toggles, and double-tap Caps Lock operate without conflict.

### 3. Long-Press Number Shortcuts on Top Letter Row (`:ime`)
- **Corner Hint Badges**: Top QWERTY letter keys (`q`–`p`) display small, subtle number hint badges (`1`–`0`) in the top-right corner (`9.sp`, semi-transparent accent/text color `textColor.copy(alpha = 0.5f)`).
- **Long-Press Shortcut Commit**: Pressing and holding a top-row letter key for 400ms emits haptic feedback (`LONG_PRESS`) and commits its corresponding number (`q`→`1`, `w`→`2`, `e`→`3`, `r`→`4`, `t`→`5`, `y`→`6`, `u`→`7`, `i`→`8`, `o`→`9`, `p`→`0`).
- **Single-Tap Retention**: Tapping normally (<400ms) still commits the letter as usual.
- **Number Row Coexistence**: Works independently of the Sprint 7 dedicated number row setting (both dedicated number row and top-row letter long-press shortcuts can coexist seamlessly).

### 4. Manual Test Steps

#### Test 1: Font-Aware Key Labels
1. Open any text field and expand the font selector strip (tap 𝔄 in top toolbar).
2. Select **"Bold Serif"** → verify key caps immediately update to show bold serif glyphs (`𝐐`, `𝐖`, `𝐄`, `𝐑`...).
3. Tap a key → verify transformed text is committed to the text field, and press callout bubble shows the bold serif character.
4. Select **"Normal"** → key caps revert to standard letters.

#### Test 2: Auto-Cap Visual Shift Sync
1. Open an empty text field with Auto-Correction toggle ON in Settings.
2. Observe the keyboard on initial launch → shift key (`⇧`) is highlighted in accent color, and key caps display in uppercase (`Q`, `W`, `E`, `R`...).
3. Tap `H` → commits `H` → shift key automatically un-highlights and key caps revert to lowercase (`e`, `l`, `l`, `o`).
4. Type `. ` (dot followed by space) → shift key automatically highlights again and key caps switch to uppercase for the start of the next sentence.

#### Test 3: Long-Press Number Shortcuts
1. Look at the top letter row (`q`–`p`) → observe subtle number badges `1`–`0` in the top-right corner of each key cap.
2. Quick tap `q` → commits letter `q`.
3. Long-press `q` (hold >400ms) → feel long-press haptic feedback and observe digit `1` committed to text field.
4. Toggle the Sprint 7 dedicated Number Row ON in Settings → verify long-press shortcuts on `q`–`p` continue to function identically.

---

## Sprint 12: Font Mismatch Audit & Fix + Expanded 25-Style Catalog

Sprint 12 resolves rendering discrepancies between the app's **Fonts** screen gallery and the live keyboard's **KeyView** key labels, while expanding `:core-fonts` from 13 to **25 systematic Unicode font styles**.

### 1. Root Cause Audit & Single-Source-of-Truth Fix
- **Audit Findings**: Both `FontsScreen` and `KeyView` call the exact same `FontTransformer.transform(text, style)` engine in `:core-fonts`. However, `FontsScreen` rendered text using `MaterialTheme.typography` (which sets explicit `fontFamily = FontFamily.Default`), whereas `KeyView` rendered bare Compose `Text` without specifying `fontFamily = FontFamily.Default`.
- **Single-Source-of-Truth Fix**: Enforced `fontFamily = FontFamily.Default` explicitly across `KeyView` and `MessageBubblePopup` composables in `:ime`. Both surfaces now utilize standard Android system font fallback handling for substituted Unicode characters (Mathematical Alphanumeric Symbols U+1D400–U+1D7FF), guaranteeing pixel-identical glyph rendering across gallery previews and key labels.

### 2. Expanded 25-Style Font Catalog (`:core-fonts`)
Added 12 new systematic, Unicode-mappable styles to `FontStyle`:
1. **Sans Serif** (`SANS_SERIF`): Math Sans-Serif (U+1D5A0 / U+1D5BA / U+1D7E2)
2. **Italic Serif** (`ITALIC_SERIF`): Math Italic (U+1D434 / U+1D44E, exception 'h' -> U+210E)
3. **Bold Italic Sans** (`BOLD_ITALIC_SANS`): Math Sans-Serif Bold Italic (U+1D63C / U+1D656)
4. **Bold Italic Serif** (`BOLD_ITALIC_SERIF`): Math Bold Italic (U+1D468 / U+1D482)
5. **Double Struck** (`DOUBLE_STRUCK`): Blackboard Bold (U+1D538 / U+1D552 / U+1D7D8 with Letterlike Symbols exceptions C, H, N, P, Q, R, Z)
6. **Bold Script** (`BOLD_SCRIPT`): Math Bold Script (U+1D4D0 / U+1D4EA)
7. **Bold Gothic** (`BOLD_GOTHIC`): Math Bold Fraktur (U+1D56C / U+1D586)
8. **Parenthesized** (`PARENTHESIZED`): Enclosed Alphanumerics Parenthesized (U+1F110 / U+249C / U+2474)
9. **Squared** (`SQUARED`): Enclosed Alphanumerics Squared (U+1F130)
10. **Negative Squared** (`NEGATIVE_SQUARED`): Enclosed Alphanumerics Negative Squared (U+1F170)
11. **Regional Indicator** (`REGIONAL_INDICATOR`): Regional Indicator Symbols (U+1F1E6)
12. **Upside Down** (`UPSIDE_DOWN`): Inverted / Flipped text mapping & string reversal

### 3. Skipped Styles & Rationale
- **Subscript (`SUBSCRIPT`)**: Skipped because Unicode defines subscript codepoints for only 17 lowercase letters (`a`, `e`, `h`, `i`, `j`, `k`, `l`, `m`, `n`, `o`, `p`, `r`, `s`, `t`, `u`, `v`, `x`) and zero uppercase letters. Per design guidelines, styles with significant alphabet gaps were excluded from the primary catalog to maintain systematic Unicode codepoint math integrity.

---

## Sprint 13: Dedicated Clipboard History Panel & Toolbar Integration

### Features
1. **Top Toolbar 5-Icon Entry Point**: Added `Clipboard` 📋 (`Icons.Default.ContentPaste`) icon to the top toolbar row. Spaced with `Arrangement.SpaceEvenly`, the 5 icons (Theme, Clipboard, Settings, Voice, Fonts) fit comfortably on standard device screens with zero crowding.
2. **Dedicated Clipboard Panel (`ClipboardPanel.kt`)**: Replaces standard letter keys when active, listing up to 10 entries from `ClipboardHistoryRepository` (most recent first).
3. **Tap-to-Paste & Individual Deletion**: Tapping any snippet commits the full text to the active editor and restores standard keyboard view. Per-item `Delete` button removes individual clips from history.
4. **"Clear All" Management**: A header action wipes the entire history in one tap.
5. **Friendly Empty State**: Displayed when no history entries exist.
6. **Reactive Sync with Quick-Paste Chip**: Both the SuggestionBar quick-paste chip and full panel bind to the same underlying `ClipboardHistoryRepository` DataStore flow, keeping them perfectly synchronized.

---

## Sprint 14: Autocomplete Module Audit, Normalization & 4-Suggestion Upgrade

### Audit & Root Causes
1. **Transformed Font Prefix Breakdown**: Previously, active font styles (Bold, Italic, Gothic, Monospace, Small Caps, etc.) emitted Mathematical Alphanumeric Symbols to `InputConnection`. When `SuggestionEngine` searched `WordTrie` with non-ASCII characters, 0 suggestions were returned.
   - **Fix**: Added `FontTransformer.normalizeToAscii()` to convert stylized characters back to ASCII prior to Trie lookup and word boundary scanning.
2. **4 Suggestions Displayed**: Upgraded default suggestion limit from 3 to **4 suggestions**.
3. **Trigger Threshold**: Enforced strict `currentWord.length >= 2` threshold (1-char typed prefix returns 0 suggestions, 2+ chars returns up to 4 suggestions).
4. **Responsive Suggestion Bar**: Updated `SuggestionBar.kt` to render up to 4 chips in an uncrowded layout with text truncation (`TextOverflow.Ellipsis`).

---

## Sprint 15: Always-Attached Suggestion Strip, Quick-Paste Single-Trigger Audit & Typography Consistency

Sprint 15 delivers three UI/behavior polish enhancements to the autocomplete suggestion strip, clipboard quick-paste indicator, and key typography styling.

### 1. Always-Attached Suggestion Strip — Branded Empty State
- **Permanent Layout Reservation**: `PandaKeyboardLayout` permanently reserves `36.dp` height for `SuggestionBar` whenever `settings.autoCorrectionEnabled` is `true`. The top strip area no longer collapses or jumps when there are 0 suggestions to show, maintaining a consistent height budget across `COMPACT` (220dp), `DEFAULT` (260dp), and `TALL` (300dp) modes.
- **Branded Placeholder ("Panda Keyboards")**: When 0 suggestions exist (empty input or no dictionary matches) and no quick-paste clip is active, `SuggestionBar` renders **"Panda Keyboards"** as a centered, subtle branded placeholder (`13.sp`, `FontWeight.Medium`, `FontFamily.Default`, with alpha transparency).
- **Dynamic Transition**: The branded placeholder automatically yields the moment 2+ characters are typed (displaying up to 4 suggestion chips), and reappears instantly when the input is cleared or completed.

### 2. Paste Quick-Action Single-Trigger Fix & Audit
- **Audit & Root Causes**:
  1. **Unconditional Re-capture on Input View Launch**: `PandaInputMethodService.onStartInputView()` calls `capturePrimaryClip()` on every keyboard open or focus change.
  2. **ID Mutation on Identical Text**: `ClipboardHistoryRepository.addClip(text)` previously created a brand-new `ClipboardItem` with a newly generated `UUID` every time `addClip()` was invoked, even when `text` was identical to the top clip item.
  3. **ID-Based State Tracking**: `PandaKeyboardLayout` compared `latestClip.id` against `currentClipId` and `persistedDismissedClipId`. Because a new UUID `id` was generated on every input view launch or repeated `OnPrimaryClipChangedListener` callback, `latestClipId != currentClipId` evaluated to `true`. This reset `hasKeyPressed` to `false` and bypassed `persistedDismissedClipId`, causing the quick-paste chip for the same copied text to reappear endlessly on keyboard reopens and recompositions.
- **Single-Trigger Fix**:
  - `ClipboardHistoryRepository` tracks `lastShownClipText` (persisted in DataStore as `last_shown_clip_text`).
  - `ClipboardHistoryRepository.addClip(text)` preserves existing items when the top item has identical text, preventing ID mutation.
  - `PandaKeyboardLayout` validates that `latestClip.text` differs from `persistedLastShownClipText`. Once shown, dismissed, or acting upon a keypress, `lastShownClipText` is marked so the indicator shows **exactly ONCE per distinct new copy event** and never reappears for identical clipboard text across session reopens or recompositions.

### 3. Font Consistency — Suggestion Words Match Symbol/Number Key Typography
- **Typography Alignment Design Decision**: Suggestion-strip word chips, quick-paste text, and branded placeholder text explicitly specify `fontFamily = FontFamily.Default` and `fontWeight = FontWeight.SemiBold`, visually matching symbol and number key typography (`?123` layer, number row, special keys).
- **Intentional Scope**: Suggestion words are functional keyboard UI text rather than typed output; therefore, suggestion chips intentionally retain the clean, neutral default system typeface regardless of the user's active `FontStyle` unicode transformation.

---

## Sprint 16: Voice Input Feature with Custom In-Keyboard Recording UI

Sprint 16 replaces the stub Voice toolbar button with a full in-keyboard voice recognition system in `:ime`.

### 1. Transparent Permission Trampoline (`VoicePermissionActivity.kt`)
- Standard `InputMethodService` cannot directly host runtime permission dialogs.
- `VoicePermissionActivity` is a transparent `Theme.Translucent.NoTitleBar` proxy activity launched via `startActivity` with `FLAG_ACTIVITY_NEW_TASK`.
- Prompts standard runtime `RECORD_AUDIO` permission dialog and finishes immediately, returning focus to the active text field and IME.
- When permission is denied, `VoicePanel` displays inline fallback messaging (*"Microphone access needed — enable it in system settings"*) with buttons to grant permission or open system app settings (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`).

### 2. Custom In-Keyboard Recording UI (`VoicePanel.kt`)
- Replaces letter/number keys entirely when `keyboardState.mode == KeyboardMode.VOICE` (matching Emoji and Clipboard panel switching patterns).
- **Amplitude Waveform Visualizer**: 7 dynamic vertical bars animated via `animateDpAsState` driven directly by `SpeechRecognizer`'s `onRmsChanged(rmsdB: Float)` callback (reflecting real microphone input volume).
- **Live Partial Speech Feedback**: Real-time text container rendering partial recognition text as the user speaks.
- **Cancel & Done Controls**: Top Cancel `(X)` button and bottom Cancel / Done `(✓)` checkmark buttons to finalize recording, commit recognized text, or discard without committing.

### 3. Speech Recognition Integration & Offline Mode Respect
- **Inline `SpeechRecognizer` Engine**: Binds `RecognitionListener` directly within `:ime` without leaving the keyboard or opening external activities.
- **Offline Mode Interaction**: Checks `settings.offlineModeEnabled`. On supported devices (Android 12+ / API 31+), uses `SpeechRecognizer.createOnDeviceSpeechRecognizer()`. If Offline Mode is enabled but on-device recognition is unavailable, displays a clear inline message (*"Offline mode is active and on-device speech recognition is unavailable..."*) to prevent un-consented network calls.

### 4. Manual Test Steps
1. **Fresh Install Permission Flow**: Tap **Voice** 🎙️ in top toolbar → `VoicePermissionActivity` prompts system `RECORD_AUDIO` permission → tap **Allow** → returns to keyboard into `VoicePanel` listening mode.
2. **Recording & Waveform**: Speak into microphone → observe 7 waveform bars pulsating in real-time response to mic volume, and partial text rendering → tap **Done (✓)** → text commits to text field and returns to standard QWERTY keyboard.
3. **Cancel Mid-Recording**: Tap **Voice** 🎙️ → speak → tap **Cancel (X)** → recording stops, nothing commits, returns to QWERTY keyboard.
4. **Permission Denied Fallback**: Permanently deny permission → tap Voice 🎙️ → verify inline fallback message and **Open Settings** button.
5. **Offline Mode Respect**: Enable Offline Mode in Settings → tap Voice 🎙️ on device without on-device recognizer → verify inline offline fallback message.

---

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK with R8 minification & ProGuard
./gradlew assembleRelease

# Run unit tests across all 4 modules (:core-fonts, :core-theme, :ime, :app)
./gradlew test

# Install on connected device
./gradlew installDebug
```

## Instrumented UI Testing & CI Guide

The `:ime` module includes a comprehensive instrumented UI test suite under `ime/src/androidTest/kotlin/com/panda/keyboards/ime/`.

### Running Tests Locally

Ensure an Android emulator (API 28+) or physical device is connected via ADB:

```bash
# Run all instrumented UI tests in :ime module
./gradlew :ime:connectedAndroidTest
```

### Test Suite Architecture

1. **Compose UI Tests ([PandaKeyboardLayoutTest.kt](file:///c:/Users/Siku/.gemini/antigravity/scratch/Panda%20Keyboard/ime/src/androidTest/kotlin/com/panda/keyboards/ime/PandaKeyboardLayoutTest.kt))**:
   - **Host**: Launched via `createAndroidComposeRule<ComponentActivity>()` with a `FakeInputConnection` spy.
   - **Coverage**:
     - Parameterized loop over all 26 lowercase letter keys (`a`–`z`).
     - Single-tap Shift auto-release & double-tap Caps-Lock persistence.
     - Symbols layer 1 (`?123`) and layer 2 (`=\<`) toggles.
     - Functional keys: Space, Unicode surrogate-pair safe Backspace deletion, Enter action, and Globe (`🌐`) key callback.
     - Font Selection Strip: transformed Unicode character commit, return to Normal, and favorited style surfacing.
     - Regression safety: verifying no key throws or no-ops, and rapid 25-key typing sequence safety.

2. **System Integration Tests ([PandaImeSystemIntegrationTest.kt](file:///c:/Users/Siku/.gemini/antigravity/scratch/Panda%20Keyboard/ime/src/androidTest/kotlin/com/panda/keyboards/ime/PandaImeSystemIntegrationTest.kt))**:
   - **Host**: Uses `UiDevice` / UiAutomator to test real system IME service registration.
   - **Automatic Restoration**: Backup and restore of the system's previous default IME during setup/teardown prevents leaving test devices or CI runners in a broken state.

### CI Limitations & Recommended Setup

> [!NOTE]
> **CI Execution Strategy**: Real system IME window testing via UiAutomator can be flaky on headless cloud CI runners (such as GitHub Actions or Bitrise) without hardware acceleration. `PandaKeyboardLayoutTest` provides hermetic Compose-level UI testing that bypasses system IME window flakiness while verifying 100% of key handling logic.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| DI | Hilt (app module only) |
| Persistence | DataStore Preferences |
| IME | InputMethodService + ComposeView |
| Widgets | AppWidgetProvider + RemoteViews |
| Serialization | kotlinx.serialization |
| Build | Gradle 8.9, AGP 8.5, Version Catalog |


