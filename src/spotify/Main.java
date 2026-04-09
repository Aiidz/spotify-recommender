package spotify;

import spotify.controller.RecommendationEngine;
import spotify.model.TrackRecord;
import spotify.repository.TrackRepository;
import spotify.view.TerminalView;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * =============================================================================
 * Main — APPLICATION ENTRY POINT (JavaFX)
 * =============================================================================
 * This class bootstraps the entire application:
 *   1. Displays a loading screen while the dataset is parsed
 *   2. Loads all ~114k tracks into memory via TrackRepository
 *   3. Creates the RecommendationEngine (controller)
 *   4. Creates and displays the TerminalView (view)
 *
 * MVC Wiring:
 *   Model    = TrackRecord (data objects loaded from CSV)
 *   View     = TerminalView (JavaFX UI)
 *   Controller = RecommendationEngine + TrackRepository
 *
 * No external libraries are used — only JavaFX and the Java standard library.
 * =============================================================================
 */
public class Main extends Application {

    /** Path to the dataset CSV relative to the project root. */
    private static final String CSV_FILENAME = "dataset/dataset.csv";

    /**
     * JavaFX entry point.
     * Called automatically by the JavaFX launcher.
     *
     * @param primaryStage The main application window
     */
    @Override
    public void start(Stage primaryStage) {
        // ---- Show loading screen first ----
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);

        Label loadingLabel = new Label("Loading Spotify Dataset...");
        loadingLabel.setFont(Font.font("Consolas", 18));
        loadingLabel.setStyle("-fx-text-fill: #1A5276;");

        Label fileLabel = new Label("Reading: " + CSV_FILENAME);
        fileLabel.setFont(Font.font("Consolas", 12));
        fileLabel.setStyle("-fx-text-fill: #2E86C1;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setStyle(
                "-fx-accent: #2E86C1;"
                + "-fx-background-color: #E8F4FD;"
        );

        loadingBox.getChildren().addAll(loadingLabel, fileLabel, progressBar);

        Scene loadingScene = new Scene(loadingBox, 450, 200);
        loadingScene.getStylesheets().add(
                getClass().getResource("view/terminal.css").toExternalForm()
        );

        primaryStage.setTitle("Spotify Music Recommendation System — Loading...");
        primaryStage.setScene(loadingScene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // ---- Load data asynchronously, then switch to main UI ----
        new Thread(() -> {
            List<TrackRecord> allTracks = loadDataset();

            // Switch back to JavaFX Application thread
            javafx.application.Platform.runLater(() -> {
                if (allTracks == null || allTracks.isEmpty()) {
                    // Show error if loading failed
                    showErrorScreen(primaryStage, "Failed to load dataset. Check the file path.");
                    return;
                }

                // Build the MVC application
                launchMainApp(primaryStage, allTracks);
            });
        }).start();
    }

    /**
     * Loads the dataset from the CSV file using TrackRepository.
     *
     * @return List of all loaded TrackRecord objects, or null on failure
     */
    private List<TrackRecord> loadDataset() {
        try {
            // Resolve the CSV path relative to the current working directory
            Path csvPath = Paths.get(CSV_FILENAME);

            if (!java.nio.file.Files.exists(csvPath)) {
                System.err.println("[Main] ERROR: CSV file not found at: " + csvPath.toAbsolutePath());
                return null;
            }

            TrackRepository repository = new TrackRepository();
            return repository.loadTracks(csvPath);

        } catch (IOException e) {
            System.err.println("[Main] ERROR: Failed to read CSV file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Wires up the MVC components and launches the main application window.
     *
     * @param stage     The primary stage
     * @param allTracks The loaded track data
     */
    private void launchMainApp(Stage stage, List<TrackRecord> allTracks) {
        // ---- CONTROLLER ----
        RecommendationEngine engine = new RecommendationEngine();

        // ---- VIEW (receives model data + controller reference) ----
        TerminalView view = new TerminalView(stage, allTracks, engine);

        // ---- Show the main window ----
        stage.setTitle("Spotify Music Recommendation System");
        stage.setWidth(950);
        stage.setHeight(750);
        view.show();

        System.out.println("[Main] Application started with " + allTracks.size() + " tracks loaded.");
    }

    /**
     * Displays an error screen when the dataset fails to load.
     */
    private void showErrorScreen(Stage stage, String message) {
        VBox errorBox = new VBox(15);
        errorBox.setAlignment(Pos.CENTER);

        Label errorLabel = new Label("ERROR");
        errorLabel.setFont(Font.font("Consolas", 24));
        errorLabel.setStyle("-fx-text-fill: #C0392B;");

        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Consolas", 12));
        msgLabel.setStyle("-fx-text-fill: #2E86C1;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(350);

        Label hintLabel = new Label(
                "Ensure the file exists at: " + CSV_FILENAME
        );
        hintLabel.setFont(Font.font("Consolas", 11));
        hintLabel.setStyle("-fx-text-fill: #85929E;");

        errorBox.getChildren().addAll(errorLabel, msgLabel, hintLabel);

        Scene errorScene = new Scene(errorBox, 450, 250);
        stage.setScene(errorScene);
        stage.setTitle("Spotify Music Recommendation System — Error");
    }

    /**
     * Standard Java main method — launches the JavaFX application.
     *
     * @param args Command-line arguments (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
