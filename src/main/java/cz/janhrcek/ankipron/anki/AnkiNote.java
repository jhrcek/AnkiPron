package cz.janhrcek.ankipron.anki;

/**
 *
 * @author jhrcek
 */
public class AnkiNote {

    private static final WordExtractor extractor = new WordExtractor();
    private final long id;
    private final String flds;
    private final String tags;

    public AnkiNote(long id, String flds, String tags) {
        this.id = id;
        this.flds = flds;
        this.tags = tags;
    }

    public long getId() {
        return id;
    }

    public String getFlds() {
        return flds;
    }

    public String getTags() {
        return tags;
    }

    public String getWord() {
        return extractor.extractWord(flds);
    }

    public String getDeutsch() {
        return extractor.getFields(flds)[1];
    }

    @Override
    public String toString() {
        return "AnkiNote{flds='" + flds + "', tags='" + tags + "', id=" + id + "}";
    }
}
