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

    public List<String> getWordsWithoutPron() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        List<String> wordsWithoutPron = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Project.getAnkiDb())) {

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(WORDS_WITHOUT_PRON_QUERY);
            while (rs.next()) {
                String flds = rs.getString("flds");
                long wordId = rs.getLong("id");
                String word = extractor.extractWord(flds);

                if (word != null) {
                    System.out.printf("%-25s <- '%s'\n", word, flds);
                    wordsWithoutPron.add(word);
                    if (getMp3File(word).exists()) {
                        try (PreparedStatement wordUpdate = conn.prepareStatement(WORD_UPDATE_QUERY)) {
                            wordUpdate.setString(1, addMp3Reference(flds));
                            wordUpdate.setLong(2, wordId);
                            wordUpdate.execute();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return wordsWithoutPron;
    }

    private File getMp3File(String word) {
        return new File(Project.getDownloadDir(), word + ".mp3");
    }

    /**
     * @param flds 'flds' attribute from Anki's 'notes' table
     * @return flds field with reference to mp3 file corresponding to the german word added
     */
    private String addMp3Reference(String flds) {
        String[] fldsParts = flds.split(FIELD_SEPARATOR);
        if (!(fldsParts.length == 3 || fldsParts.length == 4)) {
            throw new IllegalStateException("Unexpected number of flds pars! " + fldsParts.length);
        }

        String mp3FileName = getMp3File(extractor.extractWord(flds)).getName();

        StringBuilder newFlds = new StringBuilder()
                .append(fldsParts[0])
                .append(FIELD_SEPARATOR)
                .append(fldsParts[1])
                .append("[sound:").append(mp3FileName).append("]")
                .append(FIELD_SEPARATOR)
                .append(fldsParts[2]);

        if (fldsParts.length == 4) {
            newFlds.append(FIELD_SEPARATOR).append(fldsParts[3]);
        }
        System.out.printf("    - updated flds : '%s'\n", newFlds);
        return newFlds.toString();
    }
}
