package spotify.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import spotify.controller.RecommendationEngine;
import spotify.controller.RecommendationEngine.SortField;
import spotify.model.TrackRecord;

/**
 * =============================================================================
 * TerminalView — JavaFX UI (VIEW layer of MVC)
 * =============================================================================
 * Builds the JavaFX window with a retro ASCII terminal aesthetic:
 *   - White/light-blue color scheme
 *   - Monospaced fonts throughout
 *   - Genre multi-select ListView
 *   - Popularity filter TextField
 *   - Sort-by ComboBox dropdown
 *   - Read-only TextArea for ASCII table output
 *
 * This class knows NOTHING about data loading or sorting internals.
 * It simply gathers user input, delegates to the controller, and displays results.
 * =============================================================================
 */
public class TerminalView {

    // ---- UI Components ----
    private final Stage primaryStage;
    private final List<TrackRecord> allTracks;
    private final RecommendationEngine engine;

    private ListView<String> genreListView;
    private TextField popularityField;
    private ComboBox<SortField> sortComboBox;
    private ComboBox<Integer> topNComboBox;
    private TextArea resultsArea;
    private Label statusLabel;

    /**
     * Constructs the view and builds the entire UI.
     *
     * @param stage  The primary JavaFX Stage
     * @param allTracks All loaded tracks from the dataset
     * @param engine The recommendation engine for processing queries
     */
    public TerminalView(
        Stage primaryStage,
        List<TrackRecord> allTracks,
        RecommendationEngine engine
    ) {
        this.primaryStage = primaryStage;
        this.allTracks = allTracks;
        this.engine = engine;
        buildScene();
    }

    /**
     * Builds the complete scene with all UI components and styling.
     */
    private void buildScene() {
        primaryStage.setTitle("Spotify Music Recommendation System");

        // ---- Main layout: BorderPane ----
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-panel");

        // ---- TOP: Title banner ----
        VBox topPanel = buildTopPanel();
        BorderPane.setAlignment(topPanel, javafx.geometry.Pos.CENTER);
        root.setTop(topPanel);

        // ---- CENTER: Controls + Results ----
        VBox centerPanel = buildCenterPanel();
        root.setCenter(centerPanel);

        // ---- BOTTOM: Status bar ----
        statusLabel = new Label(
            "Ready. Select genres and click [RECOMMEND] to begin."
        );
        statusLabel.getStyleClass().add("status-bar");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        root.setBottom(statusLabel);

        // ---- Load CSS ----
        Scene scene = new Scene(root, 900, 720);
        scene
            .getStylesheets()
            .add(getClass().getResource("terminal.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
    }

    /**
     * Builds the title banner at the top of the window.
     */
    private VBox buildTopPanel() {
        VBox vbox = new VBox(6);
        vbox.setPadding(new Insets(10, 15, 5, 15));

        // ASCII art title
        Label titleLabel = new Label(
            "╔══════════════════════════════════════════════════╗\n" +
                "║         ____              _   _  __              ║\n" +
                "║        / ___| _ __   ___ | |_(_)/ _|_   _        ║\n" +
                "║        \\___ \\| '_ \\ / _ \\| __| | |_| | | |       ║\n" +
                "║         ___) | |_) | (_) | |_| |  _| |_| |       ║\n" +
                "║        |____/| .__/ \\___/ \\__|_|_|  \\__, |       ║\n" +
                "║              |_|                    |___/        ║\n" +
                "║                                                  ║\n" +
                "║             Music Streaming Platform             ║\n" +
                "╚══════════════════════════════════════════════════╝"
        );
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setWrapText(true);

        Label subtitleLabel = new Label(
            "Powered by Merge Sort  •  Filter by Genre  •  Weighted Vibe Score"
        );
        subtitleLabel.getStyleClass().add("subtitle-label");

        vbox.getChildren().addAll(titleLabel, subtitleLabel);
        return vbox;
    }

    /**
     * Builds the center panel containing controls and results area.
     */
    private VBox buildCenterPanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 15, 10, 15));

        // --- Controls panel ---
        VBox controlsPanel = buildControlsPanel();
        controlsPanel.getStyleClass().add("control-panel");

        // --- Results area ---
        resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setWrapText(false);
        resultsArea.getStyleClass().add("terminal-textarea");
        resultsArea.setFont(Font.font("Consolas", 13));
        resultsArea.setPromptText(
            "  Results will appear here after you click [RECOMMEND]..."
        );

        vbox.getChildren().addAll(controlsPanel, resultsArea);
        VBox.setVgrow(resultsArea, javafx.scene.layout.Priority.ALWAYS);

        return vbox;
    }

    /**
     * Builds the controls panel with genre selection, popularity filter,
     * sort dropdown, and action buttons.
     */
    private VBox buildControlsPanel() {
        VBox controlsBox = new VBox(10);

        // ---- Row 1: Genre selection ----
        Label genreLabel = new Label(
            "GENRES (Multi-select with Ctrl+Click / Shift+Click):"
        );
        genreLabel.getStyleClass().add("section-label");

        genreListView = new ListView<>();
        genreListView
            .getSelectionModel()
            .setSelectionMode(SelectionMode.MULTIPLE);
        genreListView.getStyleClass().add("genre-list-view");

        // Populate genres alphabetically
        List<String> genres = engine.getAllGenres(allTracks);
        genreListView.getItems().addAll(genres);

        VBox.setVgrow(genreListView, javafx.scene.layout.Priority.SOMETIMES);

        // ---- Row 2: Popularity filter + Sort by ----
        HBox filterRow = new HBox(20);
        filterRow.getStyleClass().add("controls-row");

        // Popularity filter
        VBox popBox = new VBox(4);
        Label popLabel = new Label("MIN POPULARITY (0-100, optional):");
        popLabel.getStyleClass().add("section-label");
        popularityField = new TextField();
        popularityField.getStyleClass().add("filter-text-field");
        popularityField.setPromptText("e.g. 50");
        popularityField.setPrefWidth(80);
        popBox.getChildren().addAll(popLabel, popularityField);

        // Sort by dropdown
        VBox sortBox = new VBox(4);
        Label sortLabel = new Label("SORT BY:");
        sortLabel.getStyleClass().add("section-label");
        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll(SortField.values());
        sortComboBox.setValue(SortField.VIBE_SCORE); // Default sort
        sortComboBox.getStyleClass().add("filter-combo-box");
        sortBox.getChildren().addAll(sortLabel, sortComboBox);

        // Show Top N dropdown
        VBox topNBox = new VBox(4);
        Label topNLabel = new Label("SHOW TOP:");
        topNLabel.getStyleClass().add("section-label");
        topNComboBox = new ComboBox<>();
        topNComboBox.getItems().addAll(5, 10, 20, 50);
        topNComboBox.setValue(10); // Default
        topNComboBox.getStyleClass().add("filter-combo-box");
        topNBox.getChildren().addAll(topNLabel, topNComboBox);

        filterRow.getChildren().addAll(popBox, sortBox, topNBox);

        // ---- Row 3: Action buttons ----
        HBox buttonRow = new HBox(15);
        buttonRow.getStyleClass().add("controls-row");

        Button recommendBtn = new Button("►  RECOMMEND");
        recommendBtn.getStyleClass().add("btn-recommend");
        recommendBtn.setOnAction(e -> handleRecommend());

        Button clearBtn = new Button("✕  CLEAR");
        clearBtn.getStyleClass().add("btn-clear");
        clearBtn.setOnAction(e -> handleClear());

        Button selectAllBtn = new Button("☐ SELECT ALL GENRES");
        selectAllBtn.getStyleClass().add("btn-clear");
        selectAllBtn.setOnAction(e -> handleSelectAllGenres());

        buttonRow.getChildren().addAll(recommendBtn, clearBtn, selectAllBtn);

        controlsBox
            .getChildren()
            .addAll(genreLabel, genreListView, filterRow, buttonRow);
        return controlsBox;
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Handles the RECOMMEND button click.
     * 1. Validates that at least one genre is selected.
     * 2. Parses optional popularity filter.
     * 3. Applies filters via the engine.
     * 4. Sorts results via Merge Sort.
     * 5. Formats and displays the top 10 as an ASCII table.
     */
    private void handleRecommend() {
        // Step 1: Get selected genres
        Set<String> selectedGenres = new HashSet<>(
            genreListView.getSelectionModel().getSelectedItems()
        );

        if (selectedGenres.isEmpty()) {
            statusLabel.setText("⚠ Please select at least one genre.");
            resultsArea.setText(
                "┌─────────────────────────────────────────────────────────┐\n" +
                    "│  ⚠  No genres selected!                                 │\n" +
                    "│                                                         │\n" +
                    "│  Please select one or more genres from the list above   │\n" +
                    "│  using Ctrl+Click or Shift+Click, then try again.       │\n" +
                    "└─────────────────────────────────────────────────────────┘"
            );
            return;
        }

        // Step 2: Parse optional popularity threshold
        int minPopularity = 0;
        String popText = popularityField.getText().trim();
        if (!popText.isEmpty()) {
            try {
                minPopularity = Integer.parseInt(popText);
                minPopularity = Math.max(0, Math.min(100, minPopularity)); // Clamp 0-100
            } catch (NumberFormatException e) {
                statusLabel.setText(
                    "⚠ Invalid popularity value. Using 0 (no filter)."
                );
                minPopularity = 0;
            }
        }

        // Step 3: Get sort field
        SortField sortField = sortComboBox.getValue();
        if (sortField == null) {
            sortField = SortField.VIBE_SCORE;
        }

        // Step 3b: Get how many results to show
        Integer topNValue = topNComboBox.getValue();
        int topN = (topNValue == null) ? 10 : topNValue;

        // Step 4: Execute recommendation pipeline
        long startTime = System.currentTimeMillis();

        // Filter by genres (multi-genre OR logic)
        List<TrackRecord> filtered = engine.filterByGenres(
            allTracks,
            selectedGenres
        );

        // Filter by minimum popularity
        filtered = engine.filterByPopularity(filtered, minPopularity);

        // Sort using Merge Sort (O(n log n))
        engine.sortBy(filtered, sortField);

        long elapsed = System.currentTimeMillis() - startTime;

        // Step 5: Format and display top N results
        String table = engine.formatTopN(filtered, topN, sortField);

        // Add header info
        StringBuilder fullOutput = new StringBuilder();
        fullOutput
            .append("  Sort: ")
            .append(sortField)
            .append("  |  Genres: ")
            .append(selectedGenres.size())
            .append("  |  Filtered: ")
            .append(filtered.size())
            .append("  |  Time: ")
            .append(elapsed)
            .append("ms\n\n");
        fullOutput.append(table);

        resultsArea.setText(fullOutput.toString());
        resultsArea.positionCaret(0); // Scroll to top

        // Update status bar
        String genreSummary = buildGenreSummary(selectedGenres);
        statusLabel.setText(
            "✓ Found " +
                filtered.size() +
                " tracks | Sorted by " +
                sortField +
                " | " +
                genreSummary
        );
    }

    /**
     * Handles the CLEAR button click — resets all filters and results.
     */
    private void handleClear() {
        genreListView.getSelectionModel().clearSelection();
        popularityField.clear();
        sortComboBox.setValue(SortField.VIBE_SCORE);
        topNComboBox.setValue(10);
        resultsArea.clear();
        statusLabel.setText(
            "Ready. Select genres and click [RECOMMEND] to begin."
        );
    }

    /**
     * Handles the SELECT ALL GENRES button — selects every genre in the list.
     */
    private void handleSelectAllGenres() {
        genreListView.getSelectionModel().selectAll();
    }

    /**
     * Builds a short summary string of selected genres for the status bar.
     */
    private String buildGenreSummary(Set<String> selectedGenres) {
        if (selectedGenres.size() <= 3) {
            return String.join(", ", selectedGenres);
        }
        List<String> sorted = new ArrayList<>(selectedGenres);
        sorted.sort(String::compareTo);
        return (
            sorted.get(0) +
            ", " +
            sorted.get(1) +
            ", + " +
            (sorted.size() - 2) +
            " more"
        );
    }

    /**
     * Shows the stage (window).
     */
    public void show() {
        primaryStage.show();
    }
}
