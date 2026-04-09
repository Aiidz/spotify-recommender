# 🤝 Contributing Guide

Thank you for your interest in this project. This guide explains how to set up a development environment, make changes, and submit them.

---

## 📋 Table of Contents

- [Getting Started](#-getting-started)
- [Development Workflow](#-development-workflow)
- [Coding Standards](#-coding-standards)
- [Testing Your Changes](#-testing-your-changes)
- [Submitting Changes](#-submitting-changes)
- [Reporting Issues](#-reporting-issues)
- [Project Conventions](#-project-conventions)

---

## 🚀 Getting Started

### 1. Fork and Clone

```cmd
git clone <your-fork-url>
cd DSA_PROJECT
```

### 2. Set Up Your Environment

- Install **Java JDK 17+** (https://adoptium.net/)
- Install **JavaFX SDK 21+** (https://gluonhq.com/products/javafx/)
- Place the dataset at `dataset/dataset.csv` (~19MB, not included in the repo)
- Edit `build.bat` with your Java and JavaFX paths:

```bat
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
set JAVAFX_LIB=C:\javafx-sdk-26\lib
```

### 3. Verify It Builds

```cmd
build.bat
```

If you see **BUILD SUCCESSFUL**, you're ready.

### 4. Run the App

```cmd
build.bat run
```

---

## 🔀 Development Workflow

### Branch Naming

Create a new branch for every change. Use this naming convention:

```
feature/description       ← new features
fix/description           ← bug fixes
docs/description          ← documentation changes
refactor/description      ← code restructuring
```

Examples:
```
git checkout -b feature/add-mood-presets
git checkout -b fix/merge-sort-stability
git checkout -b docs/update-readme
```

### Make Your Changes

- Edit the relevant `.java` files in `src/spotify/`
- Edit `terminal.css` for visual changes
- Edit `build.bat` only if you're changing the build process
- **Do not** commit files in `out/` — that folder is auto-generated

### Build and Test

```cmd
rmdir /s /q out
build.bat run
```

Always test with a clean rebuild. Do not rely on your IDE's run button.

### Commit

Write clear, imperative commit messages:

```cmd
git add src/
git commit -m "fix: align ASCII table column borders correctly"
```

---

## 📝 Coding Standards

### Naming

| Thing | Convention | Example |
|-------|-----------|---------|
| Classes | `PascalCase` | `TrackRecord`, `MergeSorter` |
| Methods | `camelCase` | `filterByGenres()`, `toAsciiRow()` |
| Fields/Variables | `camelCase` | `allTracks`, `minPopularity` |
| Constants | `UPPER_SNAKE_CASE` | `CSV_FILENAME` |
| Packages | `lowercase` | `spotify.model`, `spotify.controller` |

### Formatting

- Use **4 spaces** for indentation (no tabs)
- Opening braces on the **same line** as the declaration
- One blank line between methods
- Maximum line length: ~100 characters

### Comments

- Add **Javadoc** (`/** ... */`) for every public method
- Use `//` for inline explanations
- Add section dividers for readability:

```java
// ==================== FILTERING METHODS ====================

/**
 * Filters tracks by selected genres using OR logic.
 * ...
 */
public List<TrackRecord> filterByGenres(...) {
```

### OOP Principles

Every contribution must maintain the four OOP principles:

1. **Abstraction** — New entity types should implement `AudioEntity`
2. **Encapsulation** — Fields must be `private`, accessed via getters/setters
3. **Inheritance** — Extend `BaseEntity` for new track types
4. **Polymorphism** — Override `toAsciiRow()` and `getColumnValue()` in subclasses

### No External Libraries

- Only `java.*` (standard library) and `javafx.*` are allowed
- No Maven, no Gradle, no third-party JARs
- CSV parsing must use `BufferedReader` (manual parsing)

---

## ✅ Testing Your Changes

### Manual Testing Checklist

- [ ] `build.bat` compiles with zero errors
- [ ] `build.bat run` launches the window successfully
- [ ] The loading screen appears and transitions to the main UI
- [ ] Genre selection works (single and multi-select)
- [ ] Min Popularity filter works (valid numbers, empty, invalid input)
- [ ] Sort By dropdown changes the sort order correctly
- [ ] Show Top dropdown changes the number of displayed results (5, 10, 20, 50)
- [ ] The ASCII table borders are perfectly aligned
- [ ] The last column header matches the chosen sort field
- [ ] The CLEAR button resets everything
- [ ] SELECT ALL GENRES selects every genre
- [ ] No console errors or warnings (except JavaFX native access warnings, which are normal)

### Edge Cases to Test

- Select **one genre** → should return results
- Select **all genres** → should return all 114K tracks sorted
- Set Min Popularity to **100** → may return zero results (that's expected)
- Set Min Popularity to **abc** → should show a warning, not crash
- Sort by each field → verify descending order and correct column values
- Resize the window → UI should adapt gracefully

---

## 📤 Submitting Changes

### 1. Push Your Branch

```cmd
git push origin feature/your-feature-name
```

### 2. Open a Pull Request

- Go to the repository on GitHub (or your Git host)
- Click **"New Pull Request"**
- Select your branch
- Fill in the PR template (see below)

### 3. PR Template

When submitting a pull request, include:

```markdown
## What does this change do?
Brief description of the change.

## Why is this change needed?
Explain the problem or improvement.

## How to test it
Step-by-step instructions for reviewers.
1. Checkout this branch
2. Run `build.bat run`
3. Do X, then Y
4. Expect Z

## Screenshots (if UI change)
Attach before/after images if applicable.
```

### 4. Respond to Feedback

A maintainer will review your code and may request changes. Be responsive and make any requested adjustments.

---

## 🐛 Reporting Issues

If you find a bug or have a suggestion:

1. **Search existing issues** first — someone may have already reported it
2. **Open a new issue** with:
   - A clear title
   - Steps to reproduce (for bugs)
   - Your Java version and JavaFX version
   - Screenshot of the error (if applicable)

### Bug Report Template

```markdown
**Title:** [Bug] Short description

**Environment:**
- Java: (e.g., Temurin 25.0.2)
- JavaFX: (e.g., 26)
- OS: (e.g., Windows 11)

**Steps to Reproduce:**
1. Open the app
2. Select genre: pop
3. Set Min Popularity: 50
4. Click RECOMMEND

**Expected:** Top 10 pop tracks sorted by Vibe Score
**Actual:** [what happened instead]

**Error Output:**
[paste any console errors here]
```

---

## 📐 Project Conventions

### File Structure

```
src/spotify/
├── Main.java                ← Only file that extends javafx.application.Application
├── model/                   ← Only data classes and interfaces
├── repository/              ← Only data access logic (file I/O, CSV parsing)
├── controller/              ← Only business logic (filtering, sorting, scoring)
└── view/                    ← Only UI logic (JavaFX components, event handlers)
```

### What Goes Where

| Change | File to Edit |
|--------|-------------|
| Add a new track field | `BaseEntity.java` + `TrackRepository.java` |
| Change Vibe Score formula | `BaseEntity.java` (`getCompositeScore()`) |
| Add a new sort field | `RecommendationEngine.SortField` + `TrackRecord.getColumnValue()` |
| Change the UI layout | `TerminalView.java` |
| Change colors/fonts | `terminal.css` |
| Change CSV parsing | `TrackRepository.java` |
| Change sorting algorithm | `MergeSorter.java` |
| Change filter logic | `RecommendationEngine.java` |

### What NOT to Commit

- ❌ `out/` folder (auto-generated, regenerated on every build)
- ❌ `.class` files (auto-generated)
- ❌ `dataset/dataset.csv` (too large, not part of the repo)
- ❌ IDE-specific config files (`.idea/`, `.vscode/`)
- ❌ Personal `build.bat` path changes (keep the repo version generic or document yours in a PR description)

---

## ❓ Questions?

If you're unsure about something:
1. Check the [README.md](README.md) first
2. Look at existing code for patterns
3. Open an issue with your question

Happy coding! 🎵
