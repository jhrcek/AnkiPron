package cz.janhrcek.ankipron.anki;

import static cz.janhrcek.ankipron.anki.WordExtractor.FIELD_SEPARATOR;

/**
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

    /**
     * @return flds field with reference to mp3 file corresponding to the german word added
     */
    public String getFldsWithMp3Reference() {
        String[] fields = extractor.getFields(flds);
        String mp3FileName = getWord() + ".mp3";

        StringBuilder newFlds = new StringBuilder()
                .append(fields[0]).append(FIELD_SEPARATOR)
                .append(fields[1]).append("[sound:").append(mp3FileName).append("]").append(FIELD_SEPARATOR)
                .append(fields[2]).append(FIELD_SEPARATOR)
                .append(fields[3]);

        System.out.printf("    - updated flds : '%s'%n", newFlds);
        return newFlds.toString();
    }

    @Override
    public String toString() {
        return "AnkiNote{flds='" + flds +
                "', tags='" + tags +
                "', id=" + id + "}";
    }
}
