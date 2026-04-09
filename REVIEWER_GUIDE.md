# 📘 Spotify Recommendation System — Complete Reviewer Guide

> **Purpose:** This document is a study guide for every group member. Read it thoroughly before the presentation. You should be able to explain any part of the system if the panel asks.

---

## 📋 Table of Contents

1. [System Overview](#1-system-overview)
2. [Architecture — MVC Pattern](#2-architecture--mvc-pattern)
3. [File-by-File Breakdown](#3-file-by-file-breakdown)
4. [Data Flow — Step by Step](#4-data-flow--step-by-step)
5. [Object-Oriented Principles](#5-object-oriented-principles)
6. [Merge Sort — Deep Dive](#6-merge-sort--deep-dive)
7. [Time & Space Complexity Summary](#7-time--space-complexity-summary)
8. [Scoring Formula](#8-scoring-formula)
9. [AI Utilization Strategy](#9-ai-utilization-strategy)
10. [Q&A Cheat Sheet](#10-qa-cheat-sheet)
11. [Live Demo Script](#11-live-demo-script)
12. [Quick Reference — Key Numbers](#12-quick-reference--key-numbers)

---

## 1. System Overview

### What Is It?

A Java desktop application that recommends music to users based on a dataset of **~114,000 Spotify tracks**. Users select genres, optionally set a minimum popularity, choose a sort field, and get back a ranked Top-N list formatted as an ASCII table.

### What Makes It Special?

- **Custom Merge Sort** implemented from scratch — not `Collections.sort()`
- **MVC architecture** — clean separation of data, logic, and UI
- **All 4 OOP principles** demonstrated: Abstraction, Encapsulation, Inheritance, Polymorphism
- **JavaFX GUI** styled as a retro ASCII terminal (white/light-blue, monospaced)
- **No external libraries** — only Java standard library + JavaFX

### Key Numbers to Memorize

| Metric | Value |
|--------|-------|
| Dataset size | ~114,000 tracks, 19 MB CSV |
| Number of genres | 114 unique genres |
| Columns per track | 21 (including unnamed index column) |
| Sort algorithm | Merge Sort (O(n log n) guaranteed) |
| Sort stability | Stable (equal elements preserve order) |
| Memory usage at runtime | ~100 MB (well under typical limits) |
| Query response time | < 50 ms for typical filtered results |

---

## 2. Architecture — MVC Pattern

### What Is MVC?

MVC stands for **Model-View-Controller**. It separates an application into three layers so that each has one responsibility and doesn't interfere with the others.

```
┌──────────────────────────────────────────────────────────────┐
│                         MVC Architecture                      │
├──────────────┬───────────────────────────┬───────────────────┤
│    MODEL     │      CONTROLLER           │      VIEW         │
│              │                           │                   │
│ AudioEntity  │ TrackRepository           │ TerminalView      │
│ BaseEntity   │  └─ loadTracks()          │  └─ buildScene()  │
│ TrackRecord  │                           │  └─ handleRec()   │
│              │ RecommendationEngine      │                   │
│              │  ├─ filterByGenres()      │ JavaFX components │
│              │  ├─ filterByPopularity()  │ ASCII table       │
│              │  ├─ sortBy()              │ CSS styling       │
│              │  ├─ formatTopN()          │ Event handlers    │
│              │  └─ getAllGenres()        │                   │
│              │                           │                   │
│              │ MergeSorter               │                   │
│              │  └─ sort()                │                   │
└──────────────┴───────────────────────────┴───────────────────┘
```

### How the Layers Communicate

```
User interacts with VIEW (clicks buttons, selects genres)
    │
    ▼
VIEW calls CONTROLLER methods (filterByGenres, sortBy, formatTopN)
    │
    ▼
CONTROLLER reads MODEL objects (TrackRecord), processes them, returns results
    │
    ▼
VIEW displays the results as an ASCII table
```

### Why MVC Matters

- **Separation of concerns** — the View doesn't know how sorting works. The Controller doesn't know how the UI is built. The Model doesn't know about either.
- **Testability** — you can test `RecommendationEngine` without the GUI.
- **Extensibility** — you could replace the JavaFX View with a web UI without touching the Controller or Model.

---

## 3. File-by-File Breakdown

### `Main.java` — The Ignition Key

**Package:** `spotify`
**Role:** Application entry point. Extends `javafx.application.Application`.

**What it does:**
1. Shows a loading screen with a progress bar.
2. Spawns a background thread to load the CSV (so the UI doesn't freeze).
3. Calls `TrackRepository.loadTracks()` to parse all 114K tracks.
4. Creates `RecommendationEngine` (controller) and `TerminalView` (view).
5. Wires them together and shows the main window.
6. Never runs again after startup — it's done.

**Time complexity of startup:** O(n) where n = 114,000 rows (one pass through the CSV).

---

### `AudioEntity.java` — The Contract (Abstraction)

**Package:** `spotify.model`
**Role:** Java interface. Defines what every audio entity must provide.

**Methods declared:**
| Method | Return Type | Purpose |
|--------|------------|---------|
| `getTrackId()` | String | Unique identifier |
| `getTrackName()` | String | Song title |
| `getArtists()` | String | Artist name(s) |
| `getGenre()` | String | Genre classification |
| `getPopularity()` | int | Spotify popularity (0–100) |
| `getDanceability()` | double | 0.0–1.0 |
| `getEnergy()` | double | 0.0–1.0 |
| `getValence()` | double | 0.0–1.0 |
| `getCompositeScore()` | double | Calculated Vibe Score |
| `toAsciiRow(int rank)` | String | Format as ASCII table row |
| `toAsciiRow(int rank, String columnLabel)` | String | Format with dynamic column |
| `getColumnLabel(String sortField)` | String | Get column header name |
| `getColumnValue(String sortField)` | String | Get column cell value |

**Time complexity:** N/A — it's an interface, no implementation.

**Why it matters:** This is **Abstraction**. The Controller and View work with `AudioEntity` references without knowing the concrete class. If you later add `PodcastRecord`, everything still works.

---

### `BaseEntity.java` — The Blueprint (Encapsulation + Inheritance)

**Package:** `spotify.model`
**Role:** Abstract class. Stores all shared data and provides common behavior.

**Fields (all private):**

| Field | Type | Example |
|-------|------|---------|
| `trackId` | String | `"5SuOikwiRyPMVoIQDJUgSV"` |
| `trackName` | String | `"Comedy"` |
| `artists` | String | `"Gen Hoshino"` |
| `albumName` | String | `"Comedy"` |
| `genre` | String | `"acoustic"` |
| `popularity` | int | `73` |
| `durationMs` | long | `230666` |
| `explicit` | boolean | `false` |
| `danceability` | double | `0.676` |
| `energy` | double | `0.461` |
| `valence` | double | `0.715` |

**Key methods:**
- Constructor: Takes all 13 values, assigns them to private fields.
- Getters/setters: 13 pairs — the only way to access private fields.
- `getCompositeScore()`: Calculates `(popularity + danceability×100 + energy×100 + valence×100) / 4`.
- `abstract toAsciiRow(int rank)`: Must be implemented by subclasses.
- `getColumnLabel()` / `getColumnValue()`: Dynamic column header/value logic.

**Why it matters:**
- **Encapsulation**: All fields are private. External code must go through getters/setters.
- **Inheritance**: `TrackRecord` extends this and gets all 13 fields for free via `super(...)`.

**Time complexity of `getCompositeScore()`:** O(1) — four arithmetic operations.

---

### `TrackRecord.java` — The Concrete Class (Polymorphism)

**Package:** `spotify.model`
**Role:** The actual track object. Every CSV row becomes one `TrackRecord`.

**What it adds:**
- Nothing new — it inherits all 13 fields from `BaseEntity`.
- **Overrides** `toAsciiRow(int rank)` and `toAsciiRow(int rank, String columnLabel)` — formats the track as one row in the ASCII table.
- **Overrides** `getColumnLabel(String sortField)` — returns the correct column header (e.g., "Score", "Pop.", "Dance.", "Energy", "Valence").
- **Overrides** `getColumnValue(String sortField)` — returns the correct formatted value (e.g., `"  82.8"`, `"    88"`, `"  0.75"`).
- Has a private `truncate(String input, int maxLength)` helper for shortening long song/artist names with "...".

**Example output of `toAsciiRow(1, "DANCEABILITY")`:**
```
│    1 │ Levitating               │ Dua Lipa           │ pop            │    0.85 │
```

**Why it matters:** This is **Polymorphism**. The Controller calls `entity.toAsciiRow(rank, columnLabel)` on an `AudioEntity` reference — Java resolves it to `TrackRecord`'s implementation at runtime.

**Time complexity of `toAsciiRow()`:** O(1) — string formatting of fixed-width fields.

---

### `TrackRepository.java` — The CSV Reader

**Package:** `spotify.repository`
**Role:** The only file that touches the dataset on disk.

**What it does:**
1. Opens `dataset/dataset.csv` with a `BufferedReader` (streaming, not loading the whole file into memory at once).
2. Reads and discards the header line.
3. For each of the ~114,000 data lines:
   - Parses the CSV line into 21 fields (handles quoted fields with commas inside).
   - Validates required fields (`trackId`, `trackName`, `genre`).
   - Skips malformed rows silently.
   - Parses `popularity` as int, `danceability`/`energy`/`valence` as double.
   - Creates a new `TrackRecord` object.
4. Returns an `ArrayList<TrackRecord>` with all successfully parsed tracks.

**CSV column mapping (0-based index):**
| Index | Column | Used? |
|-------|--------|-------|
| 0 | Unnamed index | ❌ Skipped |
| 1 | `track_id` | ✅ |
| 2 | `artists` | ✅ |
| 3 | `album_name` | ✅ (stored, not displayed) |
| 4 | `track_name` | ✅ |
| 5 | `popularity` | ✅ |
| 6 | `duration_ms` | ✅ (stored, not displayed) |
| 7 | `explicit` | ✅ (stored, not used in filters yet) |
| 8 | `danceability` | ✅ |
| 9 | `energy` | ✅ |
| 10 | `key` | ❌ Unused |
| 11 | `loudness` | ❌ Unused |
| 12 | `mode` | ❌ Unused |
| 13 | `speechiness` | ❌ Unused |
| 14 | `acousticness` | ❌ Unused |
| 15 | `instrumentalness` | ❌ Unused |
| 16 | `liveness` | ❌ Unused |
| 17 | `valence` | ✅ |
| 18 | `tempo` | ❌ Unused |
| 19 | `time_signature` | ❌ Unused |
| 20 | `track_genre` | ✅ |

**Time complexity:** O(n) where n = number of CSV lines. One pass through the file.
**Space complexity:** O(n) for the resulting ArrayList.

---

### `MergeSorter.java` — The Sorting Algorithm

**Package:** `spotify.controller`
**Role:** Generic Merge Sort from scratch. No library sort methods used.

**Algorithm: Divide and Conquer**

```
Step 1: DIVIDE — Split the list in half recursively until each sub-list has 1 element.
Step 2: CONQUER — Each single-element list is trivially sorted.
Step 3: COMBINE — Merge pairs of sorted sub-lists back together, comparing elements one by one.
```

**Visual example with 8 elements:**
```
Input: [8, 3, 7, 1, 5, 2, 9, 4]

DIVIDE:
[8, 3, 7, 1, 5, 2, 9, 4]
[8, 3, 7, 1]        [5, 2, 9, 4]
[8, 3]  [7, 1]      [5, 2]  [9, 4]
[8][3]  [7][1]      [5][2]  [9][4]    ← 8 lists of 1

CONQUER & MERGE:
[3, 8]  [1, 7]      [2, 5]  [4, 9]    ← merge pairs of 1
[1, 3, 7, 8]        [2, 4, 5, 9]      ← merge pairs of 2
[1, 2, 3, 4, 5, 7, 8, 9]              ← merge pairs of 4 → DONE
```

**Key implementation details:**
- **Generic**: Works with `List<T>` and any `Comparator<T>`.
- **Stable**: When two elements compare equal, the left one goes first (uses `<=`).
- **In-place**: Modifies the original ArrayList. Uses an auxiliary `Object[]` array for the merge step.
- **Recursive**: `mergeSortRecursive()` splits, then `merge()` combines.

**Time complexity (the mathematical proof):**

```
Recurrence:  T(n) = 2·T(n/2) + O(n)

Where:
  2·T(n/2)  = two recursive calls on halves of size n/2
  O(n)      = the merge step scans every element once

Expansion (tree method):
  Level 0:          n          ← 1 problem of size n
  Level 1:     n/2      n/2    ← 2 problems of size n/2
  Level 2:  n/4  n/4  n/4  n/4 ← 4 problems of size n/4
  ...
  Level k:  1  1  1  1  1  1  1  1  ← n problems of size 1

  Height of the tree = log₂(n)   (because we halve each level)
  Work at each level = O(n)      (every element touched once)
  Total work = O(n) × log₂(n) = O(n log n)

  Worst case:  O(n log n)  ← always this, never worse
  Best case:   O(n log n)  ← no early exit, always this
  Average:     O(n log n)  ← same
```

**Space complexity:** O(n) — the auxiliary `Object[]` array holds n references. The recursion stack adds O(log n) frames. Total: O(n).

**Why we didn't use `Collections.sort()`:**
1. This is a DSA course — the point is to demonstrate the algorithm.
2. `Collections.sort()` uses TimSort (a Merge Sort + Insertion Sort hybrid). By writing Merge Sort ourselves, we prove we understand the recurrence, the merge step, and the divide-and-conquer principle.

---

### `RecommendationEngine.java` — The Brain

**Package:** `spotify.controller`
**Role:** All business logic — filtering, scoring, sorting, formatting.

**Inner enum: `SortField`**
```java
enum SortField { VIBE_SCORE, POPULARITY, DANCEABILITY, ENERGY, VALENCE }
```

**Methods:**

| Method | Input | Output | Time Complexity |
|--------|-------|--------|-----------------|
| `filterByGenres(List, Set<String>)` | All tracks + selected genres | Filtered list | O(n) — Set lookup is O(1) |
| `filterByPopularity(List, int)` | Filtered list + min popularity | Smaller list | O(m) where m = filtered size |
| `sortBy(List, SortField)` | Filtered list + sort field | Same list, rearranged | O(m log m) via MergeSorter |
| `formatTopN(List, int, SortField)` | Sorted list + count + sort field | ASCII table string | O(k) where k = N (top count) |
| `getAllGenres(List)` | All tracks | Sorted list of genre names | O(n) — one pass + TreeSet sort |

**Filter pipeline (executed in order when user clicks RECOMMEND):**
```
allTracks (114K)
    │
    ▼ filterByGenres(selectedGenres)
~5K tracks (depends on genre selection)
    │
    ▼ filterByPopularity(minPopularity)
~3K tracks (depends on popularity threshold)
    │
    ▼ sortBy(sortField) — Merge Sort O(m log m)
~3K tracks, sorted descending
    │
    ▼ formatTopN(list, topN, sortField)
ASCII table string (Top 5/10/20/50)
```

**Comparator creation in `getComparator()`:**
Each `SortField` maps to a `Comparator<TrackRecord>` that extracts the right numeric value and compares in descending order:

```java
case VIBE_SCORE:
    return (a, b) -> Double.compare(b.getCompositeScore(), a.getCompositeScore());
case POPULARITY:
    return (a, b) -> Integer.compare(b.getPopularity(), a.getPopularity());
// ... etc.
```

Note: `b` vs `a` (swapped order) = descending sort.

**Time complexity of the full pipeline:**
```
T(total) = O(n) [genre filter] + O(m) [popularity filter] + O(m log m) [sort] + O(k) [format]
         = O(n + m log m)
         where n = 114K, m = filtered size, k = top N count

For a typical query (m ≈ 3000):
  O(114000 + 3000 × 12) ≈ O(150000 operations) → < 50 ms
```

---

### `TerminalView.java` — The Window (View Layer)

**Package:** `spotify.view`
**Role:** Builds the entire JavaFX GUI. Handles user input and displays results.

**UI Components it holds:**

| Field | Type | Purpose |
|-------|------|---------|
| `allTracks` | `List<TrackRecord>` | Reference to all 114K tracks (never modified) |
| `engine` | `RecommendationEngine` | Reference to the controller |
| `genreListView` | `ListView<String>` | Genre selection list (114 genres, alphabetical) |
| `popularityField` | `TextField` | Min popularity input |
| `sortComboBox` | `ComboBox<SortField>` | Sort field dropdown |
| `topNComboBox` | `ComboBox<Integer>` | Result count dropdown (5, 10, 20, 50) |
| `resultsArea` | `TextArea` | Read-only area that displays the ASCII table |
| `statusLabel` | `Label` | Status bar at the bottom |

**What `handleRecommend()` does (the RECOMMEND button handler):**
1. Gets selected genres from `genreListView` → copies to `HashSet<String>`.
2. Validates: at least one genre must be selected.
3. Reads `popularityField` text → parses to `int` (defaults to 0 if empty/invalid).
4. Reads `sortComboBox` → gets the `SortField` enum (defaults to `VIBE_SCORE`).
5. Reads `topNComboBox` → gets the result count (defaults to 10).
6. Calls `engine.filterByGenres(allTracks, selectedGenres)`.
7. Calls `engine.filterByPopularity(filtered, minPopularity)`.
8. Calls `engine.sortBy(filtered, sortField)`.
9. Calls `engine.formatTopN(filtered, topN, sortField)`.
10. Displays the result string in `resultsArea`.
11. Updates `statusLabel` with summary info.

**Time complexity of `handleRecommend()`:** Same as the engine pipeline — O(n + m log m).

---

### `terminal.css` — The Styling

**Package:** `spotify.view`
**Role:** Stylesheet that makes the JavaFX window look like a retro ASCII terminal.

**Color palette:**
| Element | Color | Hex Code |
|---------|-------|----------|
| Window background | White | `#FFFFFF` |
| Terminal area bg | Light Blue | `#E8F4FD` |
| Text color | Dark Blue | `#1A5276` |
| Accent | Medium Blue | `#2E86C1` |
| Borders | Steel Blue | `#5DADE2` |
| Button | Blue | `#2E86C1` |
| Button hover | Bright Blue | `#3498DB` |

**Font:** Consolas / Courier New, 13px (monospaced).

---

## 4. Data Flow — Step by Step

This is the complete journey of data from file to screen:

```
┌─────────────────────────────────────────────────────────────────┐
│ PHASE 1: STARTUP                                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  dataset/dataset.csv (raw text, 19MB, 114K lines)               │
│       │                                                         │
│       │  BufferedReader.readLine() — one line at a time          │
│       ▼                                                         │
│  TrackRepository.parseLine() — splits into 21 fields            │
│       │                                                         │
│       │  new TrackRecord(fields...)                             │
│       ▼                                                         │
│  ArrayList<TrackRecord> — 114,000 TrackRecord objects           │
│       │                                                         │
│       │  passed to TerminalView constructor                     │
│       ▼                                                         │
│  TerminalView.allTracks — stored as a field reference           │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│ PHASE 2: USER INTERACTION (clicks RECOMMEND)                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  User selects genres in ListView → HashSet<String> {"pop", ...} │
│  User types "50" in popularity field → int minPopularity = 50   │
│  User picks "ENERGY" in sort combo → SortField.ENERGY           │
│  User picks "10" in top-N combo → int topN = 10                 │
│       │                                                         │
│       │  engine.filterByGenres(allTracks, selectedGenres)        │
│       ▼                                                         │
│  Loop 114K tracks: if (genres.contains(track.getGenre()))       │
│       → add to new ArrayList                                    │
│       │                                                         │
│       ▼ ~5K TrackRecord references (same objects, new list)     │
│       │                                                         │
│       │  engine.filterByPopularity(filtered, minPopularity)      │
│       ▼                                                         │
│  Loop 5K tracks: if (track.getPopularity() >= 50)               │
│       → add to new ArrayList                                    │
│       │                                                         │
│       ▼ ~3K TrackRecord references (same objects, new list)     │
│       │                                                         │
│       │  engine.sortBy(filtered, SortField.ENERGY)               │
│       ▼                                                         │
│  MergeSorter.sort(filtered, comparator)                         │
│    → Create Object[] aux array of size 3K                       │
│    → Recursive divide: [0..2999] → [0..1499], [1500..2999] →.. │
│    → Merge with comparator: b.getEnergy() vs a.getEnergy()      │
│    → ArrayList is now sorted descending by energy               │
│       │                                                         │
│       ▼ ~3K TrackRecord references, rearranged in-place          │
│       │                                                         │
│       │  engine.formatTopN(filtered, 10, SortField.ENERGY)       │
│       ▼                                                         │
│  StringBuilder appends:                                         │
│    - Header: "│ Rank │ Track │ Artist │ Genre │ Energy │"       │
│    - Rows: track.toAsciiRow(i+1, "ENERGY") × 10                 │
│    - Footer: "└──────┴──────────┴────────┴────────┴─────────┘"  │
│       │                                                         │
│       ▼ String (the full ASCII table)                            │
│       │                                                         │
│       │  resultsArea.setText(tableString)                        │
│       ▼                                                         │
│  USER SEES RESULTS ON SCREEN                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Key Insight: Object References, Not Copies

When filtering creates a new `ArrayList`, it does **NOT** copy the `TrackRecord` objects. It copies the **references** to the same objects. So in memory:

```
allTracks (ArrayList) ──► [TrackRecord@A1, TrackRecord@A2, TrackRecord@A3, ...]
                               ↑                    ↑
filtered (ArrayList) ────┘     │                    │
                               │                    │
filteredByPop (ArrayList) ─────┘                    │
                                                    │
sorted (same ArrayList as filteredByPop, rearranged)┘
```

All three ArrayLists point to the same `TrackRecord` objects in the Java heap. Only the order and which references are included changes. This is why filtering is memory-efficient.

---

## 5. Object-Oriented Principles

### 1. Abstraction — `AudioEntity` (Interface)

**Definition:** Hiding implementation details behind a clean contract.

**Where:** `AudioEntity.java`

**How:**
```java
public interface AudioEntity {
    int getPopularity();
    double getDanceability();
    double getCompositeScore();
    String toAsciiRow(int rank);
    // ... more methods
}
```

The interface says **what** an audio entity can do. It says nothing about **how** the data is stored or how the score is calculated. The Controller and View interact with `AudioEntity` references — they don't need to know about `TrackRecord` internals.

**Real-world analogy:** A car's steering wheel is an abstraction. You turn it left/right without knowing how the power steering pump and rack-and-pinion work.

---

### 2. Encapsulation — `BaseEntity` (Private Fields + Getters/Setters)

**Definition:** Bundling data with the methods that operate on it, and restricting direct access.

**Where:** `BaseEntity.java` — all 13 fields are `private`.

**How:**
```java
public abstract class BaseEntity implements AudioEntity {
    private String trackId;
    private int popularity;
    private double danceability;
    // ... 10 more private fields

    public int getPopularity() { return popularity; }  // controlled read
    public void setPopularity(int p) { this.popularity = p; }  // controlled write
}
```

**Why it matters:**
- No external class can do `track.popularity = -500`. The setter can be extended with validation later.
- The internal representation can change (e.g., store popularity as a `double` internally) without breaking external code.
- This is the "data hiding" principle.

---

### 3. Inheritance — `TrackRecord extends BaseEntity`

**Definition:** A new class derives properties and behavior from an existing class.

**Where:** `TrackRecord.java` extends `BaseEntity`.

**How:**
```java
public class TrackRecord extends BaseEntity {
    public TrackRecord(String trackId, ...) {
        super(trackId, ...);  // calls BaseEntity's constructor
    }
    // TrackRecord adds NO new fields — it inherits all 13 from BaseEntity
}
```

**What TrackRecord gets for free from BaseEntity:**
- All 13 private fields
- All 13 getters and 13 setters
- `getCompositeScore()` method
- The `AudioEntity` interface implementation

**What TrackRecord contributes:**
- The specific implementation of `toAsciiRow()` — how a **track** formats itself as ASCII.

**Real-world analogy:** A "Sedan" inherits from "Vehicle." It gets wheels, engine, and steering from Vehicle. It adds its own trunk and seating configuration.

---

### 4. Polymorphism — `toAsciiRow(int rank, String columnLabel)`

**Definition:** The ability of different objects to respond to the same method call in their own way.

**Where:** Declared in `AudioEntity`, abstract in `BaseEntity`, implemented in `TrackRecord`.

**How it works in the code:**
```java
// In RecommendationEngine.formatTopN():
for (int i = 0; i < limit; i++) {
    AudioEntity track = tracks.get(i);  // ← type is INTERFACE, not TrackRecord
    sb.append(track.toAsciiRow(i + 1, columnLabel));  // ← POLYMORPHIC CALL
}
```

At compile time, Java knows `track` is an `AudioEntity`. At **runtime**, Java resolves it to `TrackRecord.toAsciiRow()` — the actual implementation.

**Why it matters:**
- If we add `PodcastRecord extends BaseEntity` tomorrow, this exact same `formatTopN()` method works without any changes.
- The calling code is decoupled from the concrete type.
- This is runtime polymorphism (dynamic method dispatch).

---

## 6. Merge Sort — Deep Dive

### The Algorithm in Pseudocode

```
function mergeSort(list, left, right):
    if left >= right:
        return                    // base case: 1 element = already sorted

    mid = (left + right) / 2      // find midpoint

    mergeSort(list, left, mid)    // sort left half
    mergeSort(list, mid+1, right) // sort right half

    merge(list, left, mid, right) // merge the two sorted halves

function merge(list, left, mid, right):
    create temporary array temp[]
    i = left       // pointer for left half
    j = mid + 1    // pointer for right half
    k = left       // pointer for temp array

    while i <= mid AND j <= right:
        if comparator.compare(list[i], list[j]) <= 0:
            temp[k] = list[i]; i++; k++
        else:
            temp[k] = list[j]; j++; k++

    // copy remaining elements (only one of these loops runs)
    while i <= mid:
        temp[k] = list[i]; i++; k++
    while j <= right:
        temp[k] = list[j]; j++; k++

    // copy merged result back to original list
    for idx from left to right:
        list[idx] = temp[idx]
```

### Why Stable Sort Matters

Stability means: if two elements compare as equal, their original relative order is preserved.

**Example in our app:**
```
Track A: "Song 1" — Energy = 0.80, appeared at index 5 in input
Track B: "Song 2" — Energy = 0.80, appeared at index 12 in input

After Merge Sort by Energy:
  Track A is still before Track B. (stable)

After Quick Sort by Energy:
  Track B might be before Track A. (unstable — order is unpredictable)
```

**Why our code is stable:** The merge step uses `<=` (less than or equal). When elements are equal, it picks from the **left** half first, preserving the original order.

### Recursive Call Tree (for n = 8)

```
mergeSort(0, 7)
├── mergeSort(0, 3)
│   ├── mergeSort(0, 1)
│   │   ├── mergeSort(0, 0)  ← base case, returns immediately
│   │   ├── mergeSort(1, 1)  ← base case
│   │   └── merge(0, 0, 1)   ← merges 2 elements
│   ├── mergeSort(2, 2)      ← base case
│   └── merge(0, 1, 3)       ← merges 4 elements
├── mergeSort(4, 6)
│   ├── mergeSort(4, 5)
│   │   ├── mergeSort(4, 4)
│   │   ├── mergeSort(5, 5)
│   │   └── merge(4, 4, 5)
│   ├── mergeSort(6, 6)
│   └── merge(4, 5, 6)
├── mergeSort(7, 7)          ← base case
└── merge(0, 3, 7)           ← merges all 8 elements
```

Total merge calls for n=8: 7 (= n-1). Each merge call processes a subset of the total elements, and across all calls at one level, every element is touched exactly once.

### Comparison with Other Sorts

| Property | Merge Sort | Quick Sort | Bubble Sort | Insertion Sort |
|----------|-----------|------------|-------------|----------------|
| Best case | O(n log n) | O(n log n) | O(n) | O(n) |
| Average | O(n log n) | O(n log n) | O(n²) | O(n²) |
| Worst case | **O(n log n)** | **O(n²)** | O(n²) | O(n²) |
| Space | O(n) | O(log n) | O(1) | O(1) |
| Stable | **Yes** | No | Yes | Yes |
| In-place | No | Yes | Yes | Yes |
| Predictable | **Yes** | No (depends on pivot) | Yes | Yes |

**Key takeaway:** Merge Sort is the only one with guaranteed O(n log n) in **all** cases while also being stable. Quick Sort can degrade to O(n²) with bad pivot choices (e.g., already sorted data with first-element pivot).

---

## 7. Time & Space Complexity Summary

### Per-Operation Complexity

| Operation | Time Complexity | Space Complexity | Explanation |
|-----------|----------------|-----------------|-------------|
| CSV loading | O(n) | O(n) | One pass through n lines, store n objects |
| Genre filtering | O(n) | O(m) | Check each of n tracks against HashSet (O(1) lookup) |
| Popularity filtering | O(m) | O(p) | Check each of m filtered tracks |
| Merge Sort | **O(m log m)** | **O(m)** | Guaranteed by recurrence T(n) = 2T(n/2) + O(n) |
| ASCII formatting | O(k) | O(k) | k = number of results to display (5, 10, 20, 50) |
| Genre enumeration | O(n) | O(g) | One pass, store g unique genres in TreeSet |

Where:
- n = 114,000 (total tracks)
- m = filtered size after genre filter (~5,000 typical)
- p = filtered size after popularity filter (~3,000 typical)
- k = top N count (5–50)
- g = number of unique genres (114)

### Total Query Complexity

```
T(query) = O(n) [genre] + O(m) [popularity] + O(m log m) [sort] + O(k) [format]
         = O(n + m log m)

For typical values (n=114K, m=3K):
  = O(114,000 + 3,000 × 12)
  = O(150,000) operations
  ≈ 10-50 milliseconds on modern hardware
```

### Memory Usage

| Component | Approximate Size |
|-----------|-----------------|
| 114,000 TrackRecord objects | ~23 MB |
| ArrayList backing array (114K references) | ~456 KB |
| Filtered ArrayList (3K references) | ~12 KB |
| Merge Sort auxiliary array (3K references) | ~12 KB |
| JavaFX UI objects | ~5-10 MB |
| JVM overhead | ~20-30 MB |
| **Total** | **~50-70 MB** |

This is well within any modern machine's memory (8+ GB RAM is typical).

---

## 8. Scoring Formula

### Vibe Score (Equal Weights)

```
Vibe Score = (popularity + danceability × 100 + energy × 100 + valence × 100) ÷ 4
```

### Why Multiply by 100?

The four features are on different scales:

| Feature | Range | After ×100 |
|---------|-------|-----------|
| popularity | 0–100 | 0–100 (unchanged) |
| danceability | 0.0–1.0 | 0–100 |
| energy | 0.0–1.0 | 0–100 |
| valence | 0.0–1.0 | 0–100 |

Multiplying by 100 normalizes everything to the same 0–100 scale so no single feature dominates.

### Worked Example

| Track | Popularity | Danceability | Energy | Valence | Calculation | Vibe Score |
|-------|-----------|-------------|--------|---------|-------------|------------|
| "Levitating" | 88 | 0.70 | 0.82 | 0.91 | (88+70+82+91)/4 | **82.75** |
| "Comedy" | 73 | 0.68 | 0.46 | 0.72 | (73+68+46+72)/4 | **64.75** |
| "Ghost" | 55 | 0.42 | 0.17 | 0.27 | (55+42+17+27)/4 | **35.25** |

### Dynamic Column Values

When the user sorts by a field other than Vibe Score, the last column shows that field's raw value instead:

| Sort Field | Column Header | Value Example | Format |
|------------|--------------|---------------|--------|
| VIBE_SCORE | `Score` | `  82.8` | 1 decimal place |
| POPULARITY | `Pop.` | `    88` | Integer, right-aligned |
| DANCEABILITY | `Dance.` | `  0.70` | 2 decimal places |
| ENERGY | `Energy` | `  0.82` | 2 decimal places |
| VALENCE | `Valence` | `  0.91` | 2 decimal places |

### Why Equal Weights?

If we weighted popularity at 40% and the others at 20% each:
```
Score = (popularity × 0.4 + danceability×100 × 0.2 + energy×100 × 0.2 + valence×100 × 0.2)
```
The Vibe Score would be nearly identical to popularity alone, making the audio features (danceability, energy, valence) nearly irrelevant. Equal weights ensure all four features contribute meaningfully.

---

## 9. AI Utilization Strategy

### Our 4-Phase Prompt Pattern

We did **not** say "write me a music recommendation app." We used a structured engineering approach:

**Phase 1: Dataset Analysis**
- Asked AI to read the CSV header and first rows
- Identified all 21 columns, their types, and which are useful
- Discovered there is NO year column (so year filtering is impossible)
- Identified 114 unique genres, all single-value (not multi-tagged)

**Phase 2: Risk Assessment**
- 114K rows → memory concern → decided on in-memory ArrayList (acceptable at ~100MB)
- No year column → removed year filtering from requirements
- 0-popularity tracks exist → decided to treat 0 as valid data, not an error
- Large dataset → chose streaming CSV parsing (BufferedReader) over loading entire file into a String

**Phase 3: Interactive Q&A (12 targeted questions)**
- Year handling → decided to drop it entirely
- Scoring → weighted composite with equal weights
- Genre selection → multi-genre with OR logic
- Sort fields → user-selectable (5 options)
- GUI framework → JavaFX
- Data loading → load all at startup
- Architecture → MVC
- Output → Top N ASCII table with configurable count

**Phase 4: Architecture Approval**
- AI presented a complete file-by-file plan with 10 files
- We reviewed and approved before any code was written
- No blind generation — every file had a documented purpose

### The AI Prompt Pattern (memorize this)

```
1. ANALYZE data first (don't assume column names or structure)
2. IDENTIFY risks and constraints (missing data, performance, edge cases)
3. ASK clarifying questions before designing anything
4. PRESENT a detailed plan for human approval
5. ONLY THEN implement, file by file
```

### How to Answer "Did AI Write This?"

> "The AI generated the code, but we architected every decision. Before any code was written, we analyzed the dataset structure, identified missing columns, asked 12 specific design questions, and approved a file-by-file plan. The AI is the typist; we are the architects. We can explain every class, every method, every algorithm."

---

## 10. Q&A Cheat Sheet

### About OOP

**Q: Why use both an interface AND an abstract class? Isn't that redundant?**

A: No. The interface (`AudioEntity`) defines the **contract** — what operations any audio entity must support. The abstract class (`BaseEntity`) provides **shared implementation** — the 13 private fields, getters, setters, and the composite score formula. If I only used the interface, every class would duplicate field declarations. If I only used the abstract class, I couldn't have a class that extends something else AND implements AudioEntity. Together they give maximum flexibility.

**Q: Where exactly is polymorphism used in the code?**

A: In `RecommendationEngine.formatTopN()`. The method iterates over `List<TrackRecord>` but calls `track.toAsciiRow(rank, columnLabel)` through the `AudioEntity` interface reference. If we later added `PodcastRecord extends BaseEntity`, the same `formatTopN()` method would work without any changes. The calling code doesn't need to know the concrete type — that's polymorphism.

**Q: Is encapsulation just making fields private?**

A: Making fields private is the mechanism. The purpose is **controlled access**. With getters/setters, we can add validation — for example, `setPopularity()` could reject values outside 0–100. If the field were public, any code could set it to -500 with no guard. Getters/setters are the door — you can put a bouncer behind it.

---

### About Merge Sort

**Q: Why not just use `Collections.sort()`?**

A: Two reasons. First, this is a data structures course — the point is to demonstrate understanding of the algorithm. Second, `Collections.sort()` uses TimSort, which is a Merge Sort + Insertion Sort hybrid. By implementing Merge Sort from scratch, we prove we understand the divide-and-conquer principle, the recurrence relation T(n) = 2T(n/2) + O(n), and the merge step.

**Q: What happens if two tracks have the same score?**

A: Merge Sort is **stable** — equal elements preserve their original order. In our code, the `merge()` method uses `<=` when comparing, which prefers the left element on ties. So if Track A and Track B both have Energy = 0.80, and A appeared before B in the input, A stays before B after sorting. This matters for reproducibility.

**Q: What's the space complexity? Is O(n) memory a problem for 114K tracks?**

A: The auxiliary `Object[]` array holds n references, not n copies of the objects. For 114K tracks, that's 114K × 4 bytes ≈ 456 KB on a 64-bit JVM with compressed oops. The TrackRecord objects already exist in memory — the aux array just holds references. Even sorting the full 114K list uses under 1 MB of extra memory. Negligible.

**Q: Could you implement it iteratively?**

A: Yes. Bottom-up Merge Sort starts by merging pairs of 1 element, then pairs of 2, then 4, 8, 16... doubling each iteration. No recursion, no call stack. Same O(n log n) time, same O(n) space. I chose recursion because it directly mirrors the mathematical recurrence and is easier to explain and defend.

---

### About Data Structures

**Q: Why ArrayList instead of LinkedList?**

A: Two reasons. First, Merge Sort accesses elements by index (`list.get(i)`) during the merge step. On ArrayList, `get(i)` is O(1). On LinkedList, `get(i)` is O(n) — it walks the chain from the head. Using LinkedList would turn the merge step from O(n) to O(n²), destroying the algorithm. Second, ArrayList has better cache locality — the backing array is contiguous in memory, so CPU prefetching works efficiently.

**Q: How much memory do 114K TrackRecord objects use?**

A: Each TrackRecord has ~13 fields. On a 64-bit JVM with compressed oops: 13 × 4 bytes = 52 bytes + 16 bytes object header + String reference overhead ≈ 150–200 bytes per track. For 114K: 114,000 × 200 = ~23 MB. Plus ArrayList (~456 KB), JVM overhead, JavaFX UI. Total: 50–70 MB. Trivial for modern machines.

**Q: What if the dataset had 10 million rows?**

A: Loading 10 million objects would use ~2 GB RAM — possible but wasteful. For that scale, I'd switch to: (1) building a genre-indexed `HashMap<String, List<Integer>>` where the integers are file offsets into the CSV, (2) loading only matching genres on demand, or (3) using a SQLite database with indexed queries. The current in-memory approach is appropriate for 114K but doesn't scale to millions.

---

### About AI Utilization

**Q: What would you change if you did this project again?**

A: Three things. (1) Add unit tests — JUnit tests for Merge Sort correctness, filter logic, and Vibe Score calculation. (2) Add lazy loading — build a genre index at startup instead of loading all 114K tracks, for faster startup. (3) Add a mood preset feature — e.g., "Workout" mode that auto-selects high-energy genres and sorts by energy.

---

### About the Scoring Formula

**Q: Why equal weights? Isn't popularity more important?**

A: Equal weights are a design choice for fairness. Popularity is a social signal (how many people listen). Valence is an audio feature (how positive the music sounds). They're fundamentally different. Equal weights mean the Vibe Score is a true average. If popularity were 40%, the Vibe Score would be nearly identical to popularity alone, making the audio features irrelevant.

**Q: How would you validate the Vibe Score actually works?**

A: A/B test. Show one group recommendations sorted by raw popularity, another sorted by Vibe Score. Measure which group skips fewer tracks and listens longer. If Vibe Score works, users stay engaged longer because recommendations match not just what's popular, but what has the right energy and mood.

---

## 11. Live Demo Script

### Pre-Demo Checklist

- [ ] Terminal is open and `cd`'d to `DSA_PROJECT`
- [ ] `build.bat` has been run successfully
- [ ] Dataset file exists at `dataset/dataset.csv`
- [ ] Genre list in the UI is populated (114 genres visible)

### Step-by-Step

| Step | Action | What to Say |
|------|--------|-------------|
| 1 | Run `build.bat run` | "Launching the application. It's loading 114,000 tracks from the CSV — you can see the loading screen." |
| 2 | Window appears | "Here's the main interface — styled like a retro ASCII terminal with white and light-blue theming and monospaced fonts." |
| 3 | Ctrl+Click `pop` and `dance` | "I'm selecting multiple genres using Ctrl+Click. This is our multi-genre OR logic — any track in pop OR dance is included." |
| 4 | Type `50` in Min Popularity | "Setting minimum popularity to 50. This filters out obscure or unknown tracks." |
| 5 | Change Sort By to `ENERGY` | "I'll sort by Energy instead of the default Vibe Score. Watch how the column header changes to 'Energy' in the results." |
| 6 | Change Show Top to `10` | "Showing the top 10 results. I could also show 5, 20, or 50." |
| 7 | Click **RECOMMEND** | "Results appear in under 50 milliseconds. The status bar shows 3,000 matching tracks, sorted by ENERGY. Notice the last column shows actual energy values like 0.92, 0.89, etc. — not Vibe Scores." |
| 8 | Point at the ASCII table | "The table uses Unicode box-drawing characters for clean borders. Each row is formatted by the TrackRecord object itself — that's polymorphism in action." |
| 9 | Click **CLEAR**, then **RECOMMEND** again with different settings | "Let me try a different query — select `acoustic`, leave popularity empty, sort by `VALENCE`. Now the column shows valence values, and the happiest-sounding acoustic tracks are on top." |

---

## 12. Quick Reference — Key Numbers

Memorize these for the presentation:

| Fact | Number |
|------|--------|
| Total tracks | **~114,000** |
| CSV file size | **~19 MB** |
| Number of genres | **114** |
| Columns per CSV row | **21** (including unnamed index) |
| Columns we actually use | **11** (track_id, artists, album_name, track_name, popularity, duration_ms, explicit, danceability, energy, valence, track_genre) |
| Vibe Score formula | `(pop + dance×100 + energy×100 + valence×100) / 4` |
| Sort algorithm | **Merge Sort** |
| Sort time complexity | **O(n log n)** — guaranteed |
| Sort space complexity | **O(n)** auxiliary |
| Sort stability | **Stable** |
| Typical query time | **< 50 ms** |
| Memory at runtime | **~50–70 MB** |
| Java version | **25 LTS** |
| JavaFX version | **26** |
| Number of source files | **8** (.java files) |
| Number of OOP principles | **4** (Abstraction, Encapsulation, Inheritance, Polymorphism) |
| Number of MVC layers | **3** (Model, View, Controller) |
| Available sort fields | **5** (Vibe Score, Popularity, Danceability, Energy, Valence) |
| Available result counts | **4** (5, 10, 20, 50) |

---

> **Final tip:** If you don't know an answer, say: "That's a great question. Based on our implementation, the answer is..." — then reason through it. Showing you can think on your feet is worth more than a memorized answer.
