package spotify.model;

/**
 * =============================================================================
 * TrackRecord — CONCRETE CLASS (POLYMORPHISM)
 * =============================================================================
 * This is the concrete implementation representing a single Spotify track.
 * It extends BaseEntity (Inheritance) and overrides toAsciiRow() to provide
 * its own ASCII formatting — demonstrating POLYMORPHISM.
 *
 * The View layer calls toAsciiRow() on any AudioEntity reference, and the
 * correct implementation is invoked at runtime — classic polymorphic behavior.
 * =============================================================================
 */
public class TrackRecord extends BaseEntity {

    /**
     * Constructs a TrackRecord with all fields from the dataset.
     *
     * @param trackId       Unique Spotify identifier
     * @param trackName     Song title
     * @param artists       Artist name(s)
     * @param albumName     Album title
     * @param genre         Single genre classification
     * @param popularity    Spotify popularity (0–100)
     * @param durationMs    Duration in milliseconds
     * @param explicit      Explicit content flag
     * @param danceability  Danceability metric (0.0–1.0)
     * @param energy        Energy metric (0.0–1.0)
     * @param valence       Valence / positiveness metric (0.0–1.0)
     */
    public TrackRecord(
        String trackId,
        String trackName,
        String artists,
        String albumName,
        String genre,
        int popularity,
        long durationMs,
        boolean explicit,
        double danceability,
        double energy,
        double valence
    ) {
        super(
            trackId,
            trackName,
            artists,
            albumName,
            genre,
            popularity,
            durationMs,
            explicit,
            danceability,
            energy,
            valence
        );
    }

    /**
     * =================================================================
     * POLYMORPHISM: Overrides BaseEntity.toAsciiRow(int)
     * =================================================================
     * Formats this track as a single row in the ASCII results table.
     * The View layer knows nothing about TrackRecord internals — it
     * simply calls audioEntity.toAsciiRow(rank) on any AudioEntity.
     *
     * @param rank The 1-based display rank
     * @return Formatted ASCII table row
     */
    @Override
    public String toAsciiRow(int rank) {
        return toAsciiRow(rank, "Score");
    }

    /**
     * POLYMORPHISM with dynamic column header.
     * Formats this track as an ASCII table row, with the last column
     * showing the value that matches the user's chosen sort field.
     *
     * @param rank        The 1-based display rank
     * @param columnLabel The label for the last column (e.g. "Score", "Pop.", "Energy")
     * @return Formatted ASCII table row
     */
    public String toAsciiRow(int rank, String columnLabel) {
        String track = truncate(getTrackName(), 24);
        String artist = truncate(getArtists(), 18);
        String genre = truncate(getGenre(), 14);
        String value = getColumnValue(columnLabel);

        return String.format(
            "│ %4d │ %-24s │ %-18s │ %-14s │ %7s │",
            rank,
            track,
            artist,
            genre,
            value
        );
    }

    /**
     * Returns the display label for the last column based on sort field.
     */
    @Override
    public String getColumnLabel(String sortField) {
        if (sortField == null) return "Score";
        switch (sortField) {
            case "VIBE_SCORE":
                return "Score";
            case "POPULARITY":
                return "Pop.";
            case "DANCEABILITY":
                return "Dance.";
            case "ENERGY":
                return "Energy";
            case "VALENCE":
                return "Valence";
            default:
                return "Score";
        }
    }

    /**
     * Returns the formatted value for the last column based on sort field.
     */
    @Override
    public String getColumnValue(String sortField) {
        if (sortField == null) return String.format(
            "%6.1f",
            getCompositeScore()
        );
        switch (sortField) {
            case "VIBE_SCORE":
                return String.format("%6.1f", getCompositeScore());
            case "POPULARITY":
                return String.format("%6d", getPopularity());
            case "DANCEABILITY":
                return String.format("%6.2f", getDanceability());
            case "ENERGY":
                return String.format("%6.2f", getEnergy());
            case "VALENCE":
                return String.format("%6.2f", getValence());
            default:
                return String.format("%6.1f", getCompositeScore());
        }
    }

    /**
     * Truncates a string to maxLength characters, appending "..." if truncated.
     *
     * @param input     The string to truncate
     * @param maxLength Maximum desired length (before adding "...")
     * @return Truncated string with ellipsis if needed
     */
    private String truncate(String input, int maxLength) {
        if (input == null || input.isEmpty()) {
            return "Unknown";
        }
        if (input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength - 3) + "...";
    }
}
