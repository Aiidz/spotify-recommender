package spotify.repository;

import spotify.model.TrackRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * TrackRepository — DATA ACCESS LAYER
 * =============================================================================
 * Responsible for loading the Spotify dataset CSV file into in-memory
 * TrackRecord objects. Handles:
 *   - CSV line parsing (including quoted fields with commas)
 *   - Null/missing value safety
 *   - Genre field validation
 *   - Boolean and numeric parsing with graceful fallback
 *
 * Uses only java.io and java.nio — NO external libraries.
 * =============================================================================
 */
public class TrackRepository {

    /**
     * Loads all tracks from the CSV dataset file.
     *
     * The CSV format (first row = header):
     *   ,track_id,artists,album_name,track_name,popularity,duration_ms,
     *    explicit,danceability,energy,key,loudness,mode,speechiness,
     *    acousticness,instrumentalness,liveness,valence,tempo,
     *    time_signature,track_genre
     *
     * Column index mapping (0-based after splitting):
     *   0  = unnamed index (SKIP)
     *   1  = track_id
     *   2  = artists
     *   3  = album_name
     *   4  = track_name
     *   5  = popularity
     *   6  = duration_ms
     *   7  = explicit
     *   8  = danceability
     *   9  = energy
     *   10 = key (unused)
     *   11 = loudness (unused)
     *   12 = mode (unused)
     *   13 = speechiness (unused)
     *   14 = acousticness (unused)
     *   15 = instrumentalness (unused)
     *   16 = liveness (unused)
     *   17 = valence
     *   18 = tempo (unused)
     *   19 = time_signature (unused)
     *   20 = track_genre
     *
     * @param csvPath Path to the dataset CSV file
     * @return List of all successfully parsed TrackRecord objects
     * @throws IOException If the file cannot be read
     */
    public List<TrackRecord> loadTracks(Path csvPath) throws IOException {
        List<TrackRecord> tracks = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            // Read and skip the header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return tracks; // Empty file
            }

            String line;
            int lineNumber = 1; // Track line numbers for error reporting
            int skippedCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    TrackRecord track = parseLine(line, lineNumber);
                    if (track != null) {
                        tracks.add(track);
                    } else {
                        skippedCount++;
                    }
                } catch (Exception e) {
                    // Gracefully ignore any unparseable row
                    skippedCount++;
                }
            }

            if (skippedCount > 0) {
                System.out.println("[TrackRepository] Skipped " + skippedCount
                        + " unparseable rows out of " + (lineNumber - 1) + " data lines.");
            }
        }

        System.out.println("[TrackRepository] Loaded " + tracks.size() + " tracks successfully.");
        return tracks;
    }

    /**
     * Parses a single CSV line into a TrackRecord.
     * Uses a proper CSV parser that handles quoted fields containing commas.
     *
     * @param line       The raw CSV line
     * @param lineNumber The line number (for error reporting)
     * @return A TrackRecord if parsing succeeds, null otherwise
     */
    private TrackRecord parseLine(String line, int lineNumber) {
        List<String> fields = parseCsvLine(line);

        // We expect exactly 21 columns (index + 20 data columns)
        if (fields.size() < 21) {
            return null; // Malformed row — skip silently
        }

        // --- Extract fields (index 0 is unnamed, skip it) ---
        String trackId = safeTrim(fields.get(1));
        String artists = safeTrim(fields.get(2));
        String albumName = safeTrim(fields.get(3));
        String trackName = safeTrim(fields.get(4));
        String genre = safeTrim(fields.get(20));

        // Validate required fields — skip rows missing critical data
        if (trackId.isEmpty() || trackName.isEmpty() || genre.isEmpty()) {
            return null;
        }

        // Parse popularity (integer, 0–100)
        int popularity;
        try {
            popularity = Integer.parseInt(safeTrim(fields.get(5)));
        } catch (NumberFormatException e) {
            popularity = 0; // Default to 0 if unparseable
        }

        // Parse duration in milliseconds
        long durationMs;
        try {
            durationMs = Long.parseLong(safeTrim(fields.get(6)));
        } catch (NumberFormatException e) {
            durationMs = 0;
        }

        // Parse explicit flag
        boolean explicit = safeTrim(fields.get(7)).equalsIgnoreCase("true");

        // Parse danceability (0.0–1.0)
        double danceability = parseDoubleSafe(fields.get(8), 0.0);

        // Parse energy (0.0–1.0)
        double energy = parseDoubleSafe(fields.get(9), 0.0);

        // Parse valence (0.0–1.0)
        double valence = parseDoubleSafe(fields.get(17), 0.0);

        return new TrackRecord(
                trackId, trackName, artists, albumName, genre,
                popularity, durationMs, explicit,
                danceability, energy, valence
        );
    }

    /**
     * Parses a single CSV line respecting quoted fields.
     *
     * A proper CSV parser handles cases like:
     *   "Album, Vol. 2","Artist Name"
     * where commas appear inside quotes.
     *
     * @param line The raw CSV line
     * @return List of field values with quotes stripped
     */
    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // Toggle quote state (handles "field" correctly)
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                // Field delimiter — commit current field
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        // Commit the last field
        fields.add(current.toString());

        return fields;
    }

    /**
     * Safely parses a double value, returning defaultValue on failure.
     */
    private double parseDoubleSafe(String raw, double defaultValue) {
        try {
            return Double.parseDouble(safeTrim(raw));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Trims a string, returning empty string if input is null.
     */
    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
