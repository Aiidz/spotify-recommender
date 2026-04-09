package spotify.model;

/**
 * =============================================================================
 * AudioEntity — ABSTRACTION
 * =============================================================================
 * This interface defines the contract that every audio entity in the system
 * must fulfill. It abstracts away the concrete representation of a track,
 * allowing the controller and view layers to operate on any implementation
 * without knowing its internal details.
 *
 * OOP Principle: ABSTRACTION
 *   - Hides implementation complexity behind a clean API contract.
 *   - The RecommendationEngine sorts ANY AudioEntity without knowing
 *     whether it is a TrackRecord, a future PlaylistItem, etc.
 * =============================================================================
 */
public interface AudioEntity {
    /** Returns the unique identifier for this track. */
    String getTrackId();

    /** Returns the name/title of this track. */
    String getTrackName();

    /** Returns the artist(s) for this track. */
    String getArtists();

    /** Returns the genre of this track. */
    String getGenre();

    /** Returns the popularity score (0–100). */
    int getPopularity();

    /** Returns the danceability score (0.0–1.0). */
    double getDanceability();

    /** Returns the energy score (0.0–1.0). */
    double getEnergy();

    /** Returns the valence / musical positiveness (0.0–1.0). */
    double getValence();

    /**
     * Returns the composite "Vibe Score" — a weighted average of
     * popularity, danceability, energy, and valence (equal weights).
     */
    double getCompositeScore();

    /**
     * POLYMORPHISM HOOK
     * Each concrete implementation formats its own ASCII table row.
     * The view layer simply calls this method without knowing how
     * the formatting is done.
     *
     * @param rank The display rank (1-based)
     * @return A formatted ASCII table row string
     */
    String toAsciiRow(int rank);

    /**
     * POLYMORPHISM HOOK with dynamic column label.
     *
     * @param rank        The display rank (1-based)
     * @param columnLabel The label for the last column (e.g. "Score", "Pop.")
     * @return A formatted ASCII table row string
     */
    String toAsciiRow(int rank, String columnLabel);

    /**
     * Returns the display label for the last column header based on
     * the chosen sort field (e.g. "Score", "Popularity", "Energy").
     */
    String getColumnLabel(String sortField);

    /**
     * Returns the formatted value for the last column based on
     * the chosen sort field.
     */
    String getColumnValue(String sortField);
}
