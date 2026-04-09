package spotify.model;

/**
 * =============================================================================
 * BaseEntity — ENCAPSULATION + INHERITANCE
 * =============================================================================
 * This abstract class provides the shared data model for all audio entities.
 * It encapsulates all fields as PRIVATE, exposing them only through getters
 * and setters — demonstrating the OOP principle of ENCAPSULATION.
 *
 * It also provides a default Vibe Score calculation and declares the abstract
 * toAsciiRow() method, which concrete subclasses MUST implement — demonstrating
 * the OOP principle of INHERITANCE.
 * =============================================================================
 */
public abstract class BaseEntity implements AudioEntity {

    // ======================== ENCAPSULATED FIELDS ========================
    // All fields are private — no direct external access.

    /** Unique Spotify track identifier. */
    private String trackId;

    /** Track / song title. */
    private String trackName;

    /** Artist name(s); semicolon-separated if multiple artists. */
    private String artists;

    /** Album title. */
    private String albumName;

    /** Single genre assigned to this track. */
    private String genre;

    /** Popularity score from Spotify (0–100). */
    private int popularity;

    /** Track duration in milliseconds. */
    private long durationMs;

    /** Whether the track contains explicit content. */
    private boolean explicit;

    /** Danceability metric (0.0–1.0). */
    private double danceability;

    /** Energy / intensity metric (0.0–1.0). */
    private double energy;

    /** Valence / musical positiveness (0.0–1.0). */
    private double valence;

    // ======================== CONSTRUCTOR ========================

    /**
     * Constructs a BaseEntity with all required fields.
     * Subclasses call this via super(...).
     */
    public BaseEntity(String trackId, String trackName, String artists,
                      String albumName, String genre, int popularity,
                      long durationMs, boolean explicit,
                      double danceability, double energy, double valence) {
        this.trackId = trackId;
        this.trackName = trackName;
        this.artists = artists;
        this.albumName = albumName;
        this.genre = genre;
        this.popularity = popularity;
        this.durationMs = durationMs;
        this.explicit = explicit;
        this.danceability = danceability;
        this.energy = energy;
        this.valence = valence;
    }

    // ======================== GETTERS (Encapsulation) ========================

    @Override
    public String getTrackId() {
        return trackId;
    }

    @Override
    public String getTrackName() {
        return trackName;
    }

    @Override
    public String getArtists() {
        return artists;
    }

    public String getAlbumName() {
        return albumName;
    }

    @Override
    public String getGenre() {
        return genre;
    }

    @Override
    public int getPopularity() {
        return popularity;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public boolean isExplicit() {
        return explicit;
    }

    @Override
    public double getDanceability() {
        return danceability;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public double getValence() {
        return valence;
    }

    // ======================== SETTERS (Encapsulation) ========================

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public void setDanceability(double danceability) {
        this.danceability = danceability;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public void setValence(double valence) {
        this.valence = valence;
    }

    // ======================== VIBE SCORE CALCULATION ========================

    /**
     * Calculates the composite "Vibe Score" using EQUAL WEIGHTS across
     * four features: popularity, danceability, energy, and valence.
     *
     * Formula:
     *   vibeScore = (popularity + danceability*100 + energy*100 + valence*100) / 4
     *
     * The danceability, energy, and valence are multiplied by 100 to bring
     * them to the same 0–100 scale as popularity.
     *
     * @return The computed Vibe Score (0.0–100.0)
     */
    @Override
    public double getCompositeScore() {
        return (popularity
                + danceability * 100.0
                + energy * 100.0
                + valence * 100.0) / 4.0;
    }

    // ======================== POLYMORPHISM ========================

    /**
     * ABSTRACT METHOD — Must be implemented by every concrete subclass.
     * This is the POLYMORPHISM point: different entity types can format
     * their ASCII rows differently while the View calls the same method.
     *
     * @param rank The display rank (1-based)
     * @return A formatted ASCII table row string
     */
    @Override
    public abstract String toAsciiRow(int rank);

    // ======================== OVERRIDDEN toString ========================

    @Override
    public String toString() {
        return "TrackRecord{" +
                "trackName='" + trackName + '\'' +
                ", artists='" + artists + '\'' +
                ", genre='" + genre + '\'' +
                ", popularity=" + popularity +
                ", vibeScore=" + String.format("%.1f", getCompositeScore()) +
                '}';
    }
}
