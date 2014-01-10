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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnkiDatabase {

    //Select all visible fields of notes, that contain flag wort, but don't have mp3 file associated with them
    private static final String WORDS_WITHOUT_PRON_QUERY
            = "select id,tags,flds,sfld from notes where tags like '%wort%' and flds not like '%.mp3%';";
    private static final String WORD_UPDATE_QUERY = "update notes set flds=? where id=?";

    private static final String FIELD_SEPARATOR = ""; //Not a space!

    //Word of the form "e Frau (-, -en)" -> only "Frau" will be sought for
    private static final Pattern SUBSTANTIVE_WITH_ARTICLE = Pattern.compile("[res] ([^\\s]*) \\(.*\\)");
    private static final Pattern SUBST_WITH_ARTICLE = Pattern.compile("^\\(?[res]\\)?(/e)? ([^\\s]*)");
    private static final Pattern SUBSTANTIVE_WITHOUT_DECLINATION = Pattern.compile("\\(*[res]\\)* ([^\\s]*)");

    private static final String THING_IN_PARENS = "\\([^\\)]*\\)"; //Aything surrounded by ()

    public List<String> getWordsWithoutPron() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        List<String> wordsWithoutPron = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Project.getAnkiDb())) {

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(WORDS_WITHOUT_PRON_QUERY);
            while (rs.next()) {
                String flds = rs.getString("flds");
                long wordId = rs.getLong("id");
                String word = extractWord(flds);

                System.out.printf("Processing note '%s'\n", flds);
                if (word != null) {
                    System.out.printf("    - extracted word: '%s'\n", word);
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

    /**
     * @param databaseField 'flds' attribute from Anki's 'notes' table
     * @return german word that can be searched in the dictionary and whose pron we want to search
     */
    private String extractWord(String databaseField) {
        String deutsch = databaseField.split(FIELD_SEPARATOR)[1];
        Matcher matcher = null;

        if (deutsch.split(" ").length == 1) {
            // 1. Simplest case: only 1 word
            return deutsch.replaceAll("[/\\(\\)]", ""); // Remove all occurences of '/', '(' and ')'
        } else if ((matcher = SUBSTANTIVE_WITH_ARTICLE.matcher(deutsch)).matches()) {
            //2. More complex cases of the form <article> <wort> (-[e]s, -e)
            return matcher.group(1);
        } else if ((matcher = SUBSTANTIVE_WITHOUT_DECLINATION.matcher(deutsch)).matches()) {
            //3. More complex cases of the form <article> <wort>
            return matcher.group(1);
        } else {
            //TODO 3. solve most complex cases, irregular cerbs etc.
            String wort = deutsch.replaceAll(THING_IN_PARENS, "").trim();
            if (wort.contains(" - ")) {
                //Take only the part before '-'
                wort = wort.substring(0, wort.indexOf(" - ")).trim();
            }
            if ((matcher = SUBST_WITH_ARTICLE.matcher(wort)).matches()) {
                wort = matcher.group(2);
            }
            wort = wort.replaceAll("/", "");

            if (wort.split(" ").length == 1) {
                return wort;
            } else {
                //TODO - edge cases probably not worth processing
                return null;
            }
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
        List<String> wordsWithoutPron = new AnkiDatabase().getWordsWithoutPron();
        System.out.println(wordsWithoutPron + " " + wordsWithoutPron.size());
    }
}
