package cz.janhrcek.ankipron.anki;

import cz.janhrcek.ankipron.PronDownloader;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnkiDatabaseUtil {

    //Select all visible fields of notes, that contain flag wort, but don't have mp3 file associated with them
    private static final String QUERY
            = "select id,tags,flds,sfld from notes where tags like '%wort%' and flds not like '%.mp3%';";
    private static final String WORD_UPDATE = "update notes set flds=? where id=?";

    private static final String FIELD_SEPARATOR = ""; //Not a space!

    //Word of the form "e Frau (-, -en)" -> only "Frau" will be sought for
    private static final Pattern SUBSTANTIVE_WITH_ARTICLE = Pattern.compile("[res] ([^\\s]*) \\(.*\\)");

    public List<String> getWordsWithoutPron() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        List<String> wordsWithoutPron = new ArrayList<>();
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + PronDownloader.PROJECT_DIR + "collection.anki2");
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(QUERY);
            while (rs.next()) {
                String flds = rs.getString("flds");
                long wordId = rs.getLong("id");
                String word = extractWord(flds);

                System.out.printf("Processing note '%s'\n", flds);
                if (word != null) {
                    System.out.printf("    - extracted word: '%s'\n", word);
                    wordsWithoutPron.add(word);
                    if (getMp3File(word).exists()) {
                        try (PreparedStatement wordUpdate = connection.prepareStatement(WORD_UPDATE)) {
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
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e);
            }
        }
        System.out.println("----- " + wordsWithoutPron.size() + " words don't have pron associated in Anki DB");
        return wordsWithoutPron;
    }

    /**
     * @param databaseField 'flds' attribute from Anki's 'notes' table
     * @return german word that can be searched in the dictionary and whose pron we want to search
     */
    private String extractWord(String databaseField) {
        String deutsch = databaseField.split(FIELD_SEPARATOR)[1];
        Matcher matcher = null;

        if (deutsch.split(" ").length == 1) {
            // 1. Simplest case: only 1 word
            // Remove all occurences of '/', '(' and ')'
            return deutsch.replaceAll("[/\\(\\)]", "");
        } else if ((matcher = SUBSTANTIVE_WITH_ARTICLE.matcher(deutsch)).matches()) {
            //2. More complex cases, of the form <article> <wort> (-[e]s, -e)
            return matcher.group(1);
        } else {
            //TODO 3. solve most complex cases, irregular werbs etc.
            System.out.println(deutsch);
            return null;
        }
    }

    private File getMp3File(String word) {
        return new File(PronDownloader.DOWNLOAD_DIR, word + ".mp3");
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

        String mp3FileName = getMp3File(extractWord(flds)).getName();

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

    public static void main(String[] args) throws ClassNotFoundException {
        List<String> wordsWithoutPron = new AnkiDatabaseUtil().getWordsWithoutPron();
        System.out.println(wordsWithoutPron + " " + wordsWithoutPron.size());
    }
}
