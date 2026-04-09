package spotify.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import spotify.model.AudioEntity;
import spotify.model.TrackRecord;

/**
 * =============================================================================
 * RecommendationEngine — FILTERING, SCORING, AND SORTING ORCHESTRATOR
 * =============================================================================
 * This class contains the core business logic for the recommendation system:
 *   1. Filtering tracks by genre (multi-genre OR logic) and popularity
 *   2. Sorting filtered results using Merge Sort by any supported field
 *   3. Returning the top-N results as a formatted ASCII table
 *
 * It delegates the actual sorting to MergeSorter, keeping separation of concerns.
 * =============================================================================
 */
public class RecommendationEngine {

    /**
     * SortField — Enum defining all available sorting criteria.
     * Each enum value maps to a Comparator that extracts the correct
     * numeric property from an AudioEntity for comparison.
     */
    public enum SortField {
        VIBE_SCORE,
        POPULARITY,
        DANCEABILITY,
        ENERGY,
        VALENCE,
    }

    // ==================== FILTERING METHODS ====================

    /**
     * Filters tracks by selected genres using OR logic.
     *
     * A track is included if its genre matches ANY of the selected genres.
     * This is the MULTI-GENRE OR logic: the user selects multiple genres,
     * and any track belonging to any of those genres is kept.
     *
     * Time Complexity: O(n) — each track is checked once against the set.
     *                  (Set.contains is O(1) average case for HashSet)
     *
     * @param allTracks      The complete list of tracks
     * @param selectedGenres Set of genre names to filter by
     * @return Filtered list containing only tracks matching at least one genre
     */
    public List<TrackRecord> filterByGenres(
        List<TrackRecord> allTracks,
        Set<String> selectedGenres
    ) {
        if (selectedGenres == null || selectedGenres.isEmpty()) {
            return new ArrayList<>(); // No genres selected → no results
        }

        List<TrackRecord> filtered = new ArrayList<>();

        // O(n) linear scan — Set.contains() is O(1) average
        for (TrackRecord track : allTracks) {
            if (selectedGenres.contains(track.getGenre())) {
                filtered.add(track);
            }
        }

        return filtered;
    }

    /**
     * Filters tracks by minimum popularity threshold.
     *
     * A track is included only if its popularity >= minPopularity.
     *
     * Time Complexity: O(n) — each track is checked once.
     *
     * @param tracks         The list of tracks to filter
     * @param minPopularity  Minimum popularity score (0–100)
     * @return Filtered list containing only tracks meeting the threshold
     */
    public List<TrackRecord> filterByPopularity(
        List<TrackRecord> tracks,
        int minPopularity
    ) {
        if (minPopularity <= 0) {
            return tracks; // No filtering needed
        }

        List<TrackRecord> filtered = new ArrayList<>();

        // O(n) linear scan
        for (TrackRecord track : tracks) {
            if (track.getPopularity() >= minPopularity) {
                filtered.add(track);
            }
        }

        return filtered;
    }

    // ==================== SORTING METHODS ====================

    /**
     * Sorts the given list by the specified field using Merge Sort.
     *
     * Results are sorted in DESCENDING order (highest value first),
     * so that the most relevant/popular/energetic tracks appear at the top.
     *
     * Time Complexity: O(n log n) — guaranteed by Merge Sort
     * Space Complexity: O(n) — Merge Sort auxiliary array
     *
     * @param tracks    The list to sort (modified in-place)
     * @param sortField The field to sort by
     */
    public void sortBy(List<TrackRecord> tracks, SortField sortField) {
        if (tracks == null || tracks.size() <= 1) {
            return; // Nothing to sort
        }

        Comparator<TrackRecord> comparator = getComparator(sortField);
        MergeSorter.sort(tracks, comparator);
    }

    /**
     * Returns a Comparator for the given SortField.
     *
     * All comparators sort in DESCENDING order (higher values first)
     * by swapping the comparison order: compare(b, a) instead of compare(a, b).
     *
     * @param sortField The field to compare
     * @return A descending-order Comparator for that field
     */
    private Comparator<TrackRecord> getComparator(SortField sortField) {
        switch (sortField) {
            case VIBE_SCORE:
                // Sort by composite Vibe Score descending
                // (popularity + danceability*100 + energy*100 + valence*100) / 4
                return (a, b) ->
                    Double.compare(
                        b.getCompositeScore(),
                        a.getCompositeScore()
                    );
            case POPULARITY:
                // Sort by raw popularity descending
                return (a, b) ->
                    Integer.compare(b.getPopularity(), a.getPopularity());
            case DANCEABILITY:
                // Sort by danceability descending
                return (a, b) ->
                    Double.compare(b.getDanceability(), a.getDanceability());
            case ENERGY:
                // Sort by energy descending
                return (a, b) -> Double.compare(b.getEnergy(), a.getEnergy());
            case VALENCE:
                // Sort by valence (musical positiveness) descending
                return (a, b) -> Double.compare(b.getValence(), a.getValence());
            default:
                // Fallback: sort by Vibe Score
                return (a, b) ->
                    Double.compare(
                        b.getCompositeScore(),
                        a.getCompositeScore()
                    );
        }
    }

    // ==================== RESULT FORMATTING ====================

    /**
     * Returns the top N tracks from the sorted list as a formatted ASCII table.
     *
     * The table header's last column is dynamic — it reflects the chosen sort field
     * (e.g. "Score", "Pop.", "Dance.", "Energy", "Valence").
     *
     * Each row is produced by calling track.toAsciiRow(rank, columnLabel) — POLYMORPHISM
     * in action: the engine doesn't know how the row is formatted, it just
     * calls the method on the AudioEntity interface.
     *
     * @param tracks    The sorted list of tracks
     * @param n         Number of top results to include
     * @param sortField The field used for sorting (determines last column header)
     * @return Formatted ASCII table string
     */
    public String formatTopN(
        List<TrackRecord> tracks,
        int n,
        SortField sortField
    ) {
        if (tracks == null || tracks.isEmpty()) {
            return buildEmptyTable();
        }

        int limit = Math.min(n, tracks.size());

        // Determine the dynamic column label and the sort field key
        AudioEntity sampleTrack = tracks.get(0);
        String columnLabel = sampleTrack.getColumnLabel(sortField.name());
        String sortFieldKey = sortField.name(); // e.g. "DANCEABILITY" for value lookup

        // Build the header and footer with the dynamic column label
        String header = buildHeader(columnLabel);
        String footer = buildFooter();

        StringBuilder sb = new StringBuilder();
        sb.append(header);

        // Table rows — POLYMORPHISM: each track formats its own row
        for (int i = 0; i < limit; i++) {
            AudioEntity track = tracks.get(i);
            sb.append(track.toAsciiRow(i + 1, sortFieldKey)).append("\n");
        }

        // Table footer
        sb.append(footer);
        sb
            .append("\n  Showing ")
            .append(limit)
            .append(" of ")
            .append(tracks.size())
            .append(" matching tracks.\n");

        return sb.toString();
    }

    /**
     * Builds the ASCII table header with a dynamic last-column label.
     * The last column is 9 chars wide (between ││), so label is padded to 9.
     */
    private String buildHeader(String columnLabel) {
        String label = padCenter(columnLabel, 9);
        return (
            "┌──────┬──────────────────────────┬────────────────────┬────────────────┬─────────┐\n" +
            "│ Rank │ Track                    │ Artist             │ Genre          │" +
            label +
            "│\n" +
            "├──────┼──────────────────────────┼────────────────────┼────────────────┼─────────┤\n"
        );
    }

    /**
     * Builds the ASCII table footer.
     */
    private String buildFooter() {
        return "└──────┴──────────────────────────┴────────────────────┴────────────────┴─────────┘";
    }

    /**
     * Pads a string to a fixed width, centering it.
     */
    private String padCenter(String text, int width) {
        if (text.length() >= width) return text;
        int padding = width - text.length();
        int left = padding / 2;
        int right = padding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    /**
     * Builds an ASCII table for the case when no results are found.
     */
    private String buildEmptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(
            "┌──────┬──────────────────────────┬────────────────────┬────────────────┬─────────┐\n"
        );
        sb.append(
            "│ Rank │ Track                    │ Artist             │ Genre          │  Score  │\n"
        );
        sb.append(
            "├──────┼──────────────────────────┼────────────────────┼────────────────┼─────────┤\n"
        );
        sb.append(
            "│      │   No matching tracks found.                    │                  │         │\n"
        );
        sb.append(
            "│      │   Try selecting different genres or filters.   │                  │         │\n"
        );
        sb.append(
            "└──────┴──────────────────────────┴────────────────────┴────────────────┴─────────┘\n"
        );
        return sb.toString();
    }

    // ==================== GENRE ENUMERATION ====================

    /**
     * Extracts all unique genre names from the full track list.
     *
     * Time Complexity: O(n) — scans each track once
     * Space Complexity: O(g) — where g is the number of unique genres
     *
     * @param allTracks The complete list of tracks
     * @return A sorted list of unique genre names (alphabetical)
     */
    public List<String> getAllGenres(List<TrackRecord> allTracks) {
        // Use a java.util.TreeSet for automatic alphabetical sorting
        java.util.Set<String> genreSet = new java.util.TreeSet<>();

        for (TrackRecord track : allTracks) {
            String genre = track.getGenre();
            if (genre != null && !genre.isEmpty()) {
                genreSet.add(genre);
            }
        }

        return new ArrayList<>(genreSet);
    }
}
