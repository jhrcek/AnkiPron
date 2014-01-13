package cz.janhrcek.ankipron.anki;

import cz.janhrcek.ankipron.Project;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static cz.janhrcek.ankipron.anki.WordExtractor.FIELD_SEPARATOR;

public class AnkiDatabase {

    //Select all visible fields of notes, that contain flag wort, but don't have mp3 file associated with them
    private static final String WORDS_WITHOUT_PRON_QUERY
            = "select id,tags,flds,sfld from notes where tags like '%wort%' and flds not like '%.mp3%';";
    private static final String WORD_UPDATE_QUERY = "update notes set flds=? where id=?";
    private final WordExtractor extractor = new WordExtractor();

    public AnkiDatabase() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
    }

    public List<AnkiNote> getNotesWithoutPron() {
        List<AnkiNote> notesWithoutPron = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Project.getAnkiDb())) {

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(WORDS_WITHOUT_PRON_QUERY);
            while (rs.next()) {
                long wordId = rs.getLong("id");
                String flds = rs.getString("flds");
                String tags = rs.getString("tags");

                AnkiNote note = new AnkiNote(wordId, flds, tags);
                //System.out.printf("%25s <- %s\n", note.getWord(), note.getFlds());
                notesWithoutPron.add(note);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return notesWithoutPron;
    }

    public List<String> getWordsToSearch() throws ClassNotFoundException {
        List<AnkiNote> notesWithoutPron = getNotesWithoutPron();
        List<String> wordsToSearch = new ArrayList<>();

        for (AnkiNote note : notesWithoutPron) {
            String word = note.getWord();
            if (word != null) {
                wordsToSearch.add(word);
            }
        }
        return wordsToSearch;
    }

    public void addMp3ReferencesToAnkiDb(List<AnkiNote> downloadedProns) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Project.getAnkiDb())) {
            for (AnkiNote note : downloadedProns) {
                if (getMp3File(note.getWord()).exists()) {
                    try (PreparedStatement wordUpdate = conn.prepareStatement(WORD_UPDATE_QUERY)) {
                        wordUpdate.setString(1, addMp3Reference(note.getFlds()));
                        wordUpdate.setLong(2, note.getId());
                        wordUpdate.execute();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private File getMp3File(String word) {
        return new File(Project.getDownloadDir(), word + ".mp3");
    }

    /**
     * @param flds 'flds' attribute from Anki's 'notes' table
     * @return flds field with reference to mp3 file corresponding to the german word added
     */
    private String addMp3Reference(String flds) {
        String[] fields = extractor.getFields(flds);
        String mp3FileName = getMp3File(extractor.extractWord(flds)).getName();

        StringBuilder newFlds = new StringBuilder()
                .append(fields[0])
                .append(FIELD_SEPARATOR).append(fields[1])
                .append("[sound:").append(mp3FileName).append("]")
                .append(FIELD_SEPARATOR).append(fields[2])
                .append(FIELD_SEPARATOR).append(fields[3]);

        System.out.printf("    - updated flds : '%s'\n", newFlds);
        return newFlds.toString();
    }
}
