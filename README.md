# 🎵 Spotify Music Recommendation System

A university project that builds an **interactive music recommendation system** using a Spotify dataset of ~114,000 tracks. Built entirely in **Java** with a **JavaFX GUI** styled as a retro ASCII terminal.

---

## 📋 Table of Contents

- [Features](#-features)
- [How It Works](#-how-it-works)
- [Clone & Setup](#-clone--setup)
- [Installation](#-installation)
- [Usage Guide](#-usage-guide)
- [Project Structure](#-project-structure)
- [Architecture (MVC)](#-architecture-mvc)
- [OOP Principles](#-oop-principles)
- [Merge Sort](#-merge-sort)
- [Scoring Formula](#-scoring-formula)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [Tech Stack](#-tech-stack)

---

## ✨ Features

- 🔍 **Multi-Genre Filtering** — Select one or more genres to discover matching tracks
- 📊 **Popularity Filter** — Optionally set a minimum popularity threshold (0–100)
- 🎚️ **Sortable Results** — Sort by Vibe Score, Popularity, Danceability, Energy, or Valence
- 📈 **Configurable Result Count** — Show Top 5, 10, 20, or 50 results
- ⚡ **Merge Sort** — Custom O(n log n) sorting algorithm implemented from scratch
- 🖥️ **Retro ASCII Terminal UI** — White and light-blue styled GUI with monospaced fonts
- 📦 **~114K Tracks** — Full dataset loaded in memory for instant query results

---

## 🧠 How It Works

1. **At startup**, the system loads ~114,000 tracks from a CSV dataset into memory
2. **You select** one or more genres from the alphabetical list (Ctrl+Click for multiple)
3. **Optionally** set a minimum popularity number (e.g., `50` means only tracks with popularity ≥ 50)
4. **Choose** how many results to show (5, 10, 20, or 50) and how to sort them
5. **Click RECOMMEND** → the system filters tracks, sorts them, and displays results as a formatted ASCII table

**Behind the scenes:**

```
CSV File ──► TrackRepository (loads & parses)
                │
                ▼
         List<TrackRecord> (~114K objects)
                │
                ▼  ┌─ filterByGenres(selectedGenres)
     RecommendationEngine ├─ filterByPopularity(minPopularity)
                │          └─ sortBy(field) ──► MergeSort (O(n log n))
                ▼
         formatTopN(filtered, n, sortField) ──► ASCII Table String
                │
                ▼
         TerminalView displays results
```

---

## 🚀 Clone & Setup

### Option A: Clone from Git (Recommended)

If this project is hosted on GitHub or another Git remote:

```cmd
git clone <repository-url>
cd DSA_PROJECT
```

### Option B: Manual Download

1. Download the project folder as a `.zip` file
2. Extract it to a convenient location, e.g. `C:\Users\<You>\Documents\DSA_PROJECT\`
3. Open a terminal in that folder:
   ```cmd
   cd C:\Users\<You>\Documents\DSA_PROJECT
   ```

### ⚠️ Important: Dataset File

The `dataset/dataset.csv` file (~19MB) is **not** included in the Git repository or zip download due to its size. You must obtain it separately and place it at:

```
DSA_PROJECT/
└── dataset/
    └── dataset.csv    ← you must provide this file
```

The app will show an error on startup if the file is missing.

### ⚠️ Important: Do NOT Use the IDE Run Button

**Do not** use the "Play" or "Run" button in your IDE (VS Code, Zed, IntelliJ, etc.). Those will likely run old cached `.class` files or miss the JavaFX module flags.

**Always use the terminal:**

```cmd
build.bat          ← compiles
build.bat run      ← compiles and launches
```

---

## 📦 Installation

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **Java JDK** | 17+ (tested with 25) | Adoptium/Temurin recommended |
| **JavaFX SDK** | 21+ (tested with 26) | Separate download, see below |

### Step 1 — Install Java

If you don't have Java installed, download **Temurin JDK** from:
👉 https://adoptium.net/

After installing, open a **new** terminal and verify:

```cmd
javac -version
java -version
```

Both should print version 17 or higher.

### Step 2 — Install JavaFX SDK

JavaFX is **not** bundled with Java. Download it separately:

1. Go to https://gluonhq.com/products/javafx/
2. Download the **Windows SDK** zip
3. Extract it to a simple path, e.g. `C:\javafx-sdk-26\`

Verify the extraction worked:

```
C:\javafx-sdk-26\lib\javafx.controls.jar  ← this file must exist
C:\javafx-sdk-26\lib\javafx.fxml.jar      ← this too
```

### Step 3 — Configure build.bat

Open `build.bat` in any text editor (Notepad, VS Code, Zed, etc.) and set the two paths at the top:

```bat
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
set JAVAFX_LIB=C:\javafx-sdk-26\lib
```

Adjust these to match your actual installation paths.

### Step 4 — Build & Run

```cmd
build.bat          ← compiles the project
build.bat run      ← launches the application
```

A window will appear with a loading screen, then the main terminal interface.

---

## 📖 Usage Guide

### Main Window Layout

```
╔══════════════════════════════════════════════════════════╗
║          Spotify Music Recommendation System             ║
╚══════════════════════════════════════════════════════════╝

┌─── GENRES (Multi-select) ────────────────────────────────┐
│ □ acoustic                                               │
│ □ afrobeat                                               │
│ □ alternative                                            │
│ ... (114 genres total, alphabetical)                     │
└──────────────────────────────────────────────────────────┘

MIN POPULARITY: [____]   SORT BY: [VIBE_SCORE ▼]   SHOW TOP: [10 ▼]

[► RECOMMEND]  [✕ CLEAR]  [☐ SELECT ALL GENRES]

┌──────┬──────────────────────────┬────────────┬─────────┬───────┐
│ Rank │ Track                    │ Artist     │ Genre   │ Score │
├──────┼──────────────────────────┼────────────┼─────────┼───────┤
│    1 │ Comedy                   │ Gen H...   │ acoustic│  72.4 │
│    2 │ Ghost - Acoustic         │ Ben W...   │ acoustic│  65.1 │
│   ...│                          │            │         │       │
│   10 │ ...                      │            │         │       │
└──────┴──────────────────────────┴────────────┴─────────┴───────┘
```

### Controls

| Control | What it does |
|---------|-------------|
| **Genre List** | Select one or more genres with **Ctrl+Click** (individual) or **Shift+Click** (range) |
| **Min Popularity** | Optional. Type a number 0–100. Leave empty to skip this filter |
| **Sort By** | Dropdown to choose the sort field (see below) |
| **Show Top** | Choose how many results to display: 5, 10, 20, or 50 |
| **RECOMMEND** | Execute the search and display results |
| **CLEAR** | Reset all selections and results |
| **SELECT ALL GENRES** | Select every genre at once |

### Sort Options

| Sort Field | Description | Column Label |
|------------|-------------|--------------|
| **VIBE_SCORE** | Composite score (popularity + danceability + energy + valence). **Default and recommended** | `Score` |
| **POPULARITY** | Raw Spotify popularity (0–100). Most popular tracks first | `Pop.` |
| **DANCEABILITY** | Tracks best suited for dancing | `Dance.` |
| **ENERGY** | Most intense / energetic tracks | `Energy` |
| **VALENCE** | Most positive / happy-sounding tracks | `Valence` |

### Example Queries

**"I want upbeat pop music"**
1. Select `pop` and `dance` genres
2. Set Min Popularity to `60`
3. Sort by `ENERGY`
4. Show Top: `10`
5. Click RECOMMEND

**"Show me the best acoustic tracks overall"**
1. Select `acoustic`
2. Leave Min Popularity empty
3. Sort by `VIBE_SCORE`
4. Click RECOMMEND

**"I want chill vibes across multiple genres"**
1. Select `ambient`, `chill`, `lo-fi`
2. Set Min Popularity to `40`
3. Sort by `VALENCE`
4. Click RECOMMEND

---

## 📁 Project Structure

```
DSA_PROJECT/
│
├── README.md                          ← You are here
├── CONTRIBUTING.md                    ← How to contribute
├── build.bat                          ← Compile & run script
│
├── dataset/
│   └── dataset.csv                    (~19MB, 114K tracks — not in repo)
│
├── out/                               ← Compiled .class files (auto-generated)
│   └── spotify/
│       ├── Main.class
│       ├── model/
│       ├── repository/
│       ├── controller/
│       └── view/
│           └── terminal.css
│
└── src/spotify/
    ├── Main.java                      ← Application entry point
    │
    ├── model/
    │   ├── AudioEntity.java           ← Interface (Abstraction)
    │   ├── BaseEntity.java            ← Abstract class (Encapsulation + Inheritance)
    │   └── TrackRecord.java           ← Concrete class (Polymorphism)
    │
    ├── repository/
    │   └── TrackRepository.java       ← CSV loading & parsing
    │
    ├── controller/
    │   ├── MergeSorter.java           ← Generic Merge Sort from scratch
    │   └── RecommendationEngine.java  ← Filtering, scoring, sorting
    │
    └── view/
        ├── TerminalView.java          ← JavaFX UI construction
        └── terminal.css               ← Retro terminal stylesheet
```

---

## 🏗️ Architecture (MVC)

The project follows the **Model-View-Controller** pattern:

```
┌──────────────────────────────────────────────────────┐
│                    MVC Architecture                   │
├──────────┬──────────────────────┬────────────────────┤
│  MODEL   │    CONTROLLER        │       VIEW         │
│          │                      │                    │
│ Audio-   │ TrackRepository      │ TerminalView       │
│ Entity   │  └─ loadTracks()     │  └─ buildScene()   │
│          │                      │  └─ handleRec()    │
│ Base-    │ RecommendationEngine │                    │
│ Entity   │  ├─ filterByGenres() │  JavaFX components │
│          │  ├─ filterByPop()    │  ASCII table       │
│ Track-   │  ├─ sortBy() ────────►  CSS styling       │
│ Record   │  ├─ formatTopN()     │                    │
│          │  └─ getAllGenres()   │                    │
│          │                      │                    │
│          │ MergeSorter          │                    │
│          │  └─ sort()           │                    │
└──────────┴──────────────────────┴────────────────────┘
```

| Layer | Responsibility |
|-------|---------------|
| **Model** | Data classes (`AudioEntity`, `BaseEntity`, `TrackRecord`) that represent a track |
| **Controller** | Business logic: CSV loading, filtering, sorting, scoring, formatting |
| **View** | JavaFX UI: layout, controls, event handlers, ASCII table display |

---

## 🎓 OOP Principles

This project demonstrates all four core OOP principles:

| Principle | Where | How |
|-----------|-------|-----|
| **Abstraction** | `AudioEntity.java` | Interface defines *what* a track can do, not *how*. The engine sorts any `AudioEntity` without knowing its internals |
| **Encapsulation** | `BaseEntity.java` | All 13 fields are `private`. External code accesses them only through getters/setters |
| **Inheritance** | `TrackRecord extends BaseEntity` | `TrackRecord` inherits all fields and the `getCompositeScore()` method from `BaseEntity` |
| **Polymorphism** | `toAsciiRow(int rank)` | Declared as `abstract` in `BaseEntity`, implemented in `TrackRecord`. The View calls `entity.toAsciiRow()` on any `AudioEntity` reference — the correct implementation runs at runtime |

---

## 🔀 Merge Sort

### Why Merge Sort?

- **Guaranteed O(n log n)** time complexity in all cases (best, average, worst)
- **Stable sort** — equal elements preserve their original order
- Clean divide-and-conquer structure that's easy to explain and analyze

### Implementation Details

- **Generic**: Works with `List<T>` and any `Comparator<T>`
- **From scratch**: Zero use of `Collections.sort()` or `Arrays.sort()`
- **In-place**: Modifies the original list (uses auxiliary array for merging)
- **5 sort fields**: Vibe Score, Popularity, Danceability, Energy, Valence

### Complexity

```
Recurrence:  T(n) = 2·T(n/2) + O(n)

  Divide:    Split list in half         → O(1)
  Conquer:   Recursively sort halves    → 2·T(n/2)
  Combine:   Merge two sorted halves    → O(n)

Expanded:    T(n) = n + n·log₂(n)
  Worst:     O(n log n)
  Average:   O(n log n)
  Best:      O(n log n)
  Space:     O(n) auxiliary
```

---

## 📐 Scoring Formula

### Vibe Score (Equal Weights)

Each track receives a **Vibe Score** calculated as:

```
Vibe Score = (popularity + danceability×100 + energy×100 + valence×100) ÷ 4
```

**Why multiply by 100?**
- `popularity` is already 0–100
- `danceability`, `energy`, and `valence` are 0.0–1.0
- Multiplying by 100 normalizes them all to the 0–100 scale

**Example:**

| Track | Popularity | Danceability | Energy | Valence | Vibe Score |
|-------|-----------|-------------|--------|---------|------------|
| Song A | 80 | 0.75 | 0.60 | 0.80 | **73.8** |

```
(80 + 75 + 60 + 80) ÷ 4 = 73.75 ≈ 73.8
```

---

## 🛠️ Troubleshooting

### "JavaFX SDK not found"

```
[ERROR] JavaFX SDK not found at: C:\javafx-sdk-26\lib
```

**Fix:**
1. Make sure you downloaded and extracted the JavaFX SDK zip
2. Check that this file exists: `C:\javafx-sdk-26\lib\javafx.controls.jar`
3. Update `JAVAFX_LIB` in `build.bat` to the correct `lib` folder path

### "javac not found"

```
[ERROR] javac not found at: ...
```

**Fix:**
1. Install a JDK (not just a JRE). Recommended: https://adoptium.net/
2. Update `JAVA_HOME` in `build.bat` to your JDK installation path
3. Open a **new** terminal after installing Java

### Compilation errors about `javafx.application`

```
error: package javafx.application is not visible
```

**Fix:** This means the `--module-path` and `--add-modules` flags aren't being passed to `javac`. Ensure `JAVAFX_LIB` is set correctly in `build.bat` and use `build.bat` (not raw `javac`) to compile.

### App window doesn't appear

- Check the terminal for error messages
- Ensure the CSV file exists at `dataset/dataset.csv` relative to the project folder
- The dataset is ~19MB and 114K rows — loading takes 2–5 seconds. Wait for the window to appear.
- **Do not use the IDE's Run button** — use `build.bat run` only

### "No matching tracks found"

- Try selecting a different genre
- Check if your **Min Popularity** filter is too high (e.g., `95` may return very few results)
- Some genres have fewer tracks than others

---

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for details. In short:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with `build.bat run`
5. Submit a pull request

---

## 💻 Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java (Temurin)** | 25 LTS | Programming language |
| **JavaFX** | 26 | GUI framework |
| **CSV Dataset** | Spotify ~114K tracks | Music data source |

### External Dependencies

**None.** The project uses only:
- Java Standard Library (`java.io`, `java.nio`, `java.util`)
- JavaFX (`javafx.controls`, `javafx.fxml`)

No third-party libraries (no OpenCSV, no Maven, no Gradle).

---

## 📝 Dataset

The project uses a Spotify dataset CSV file with the following columns:

| Column | Type | Description |
|--------|------|-------------|
| `track_id` | String | Unique Spotify ID |
| `artists` | String | Artist name(s) |
| `album_name` | String | Album title |
| `track_name` | String | Song title |
| `popularity` | int (0–100) | Spotify popularity metric |
| `duration_ms` | long | Track length in milliseconds |
| `explicit` | boolean | Explicit content flag |
| `danceability` | double (0.0–1.0) | Danceability metric |
| `energy` | double (0.0–1.0) | Energy / intensity metric |
| `valence` | double (0.0–1.0) | Musical positiveness |
| `track_genre` | String | Single genre classification |

The dataset contains **114 unique genres** and **~114,000 tracks**.

---

## 👤 Author

University project — Data Structures & Algorithms course.

---

## 📄 License

Educational use only. Dataset sourced from Spotify for academic purposes.
