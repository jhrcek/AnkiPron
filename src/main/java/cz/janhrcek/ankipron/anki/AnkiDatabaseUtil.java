package cz.janhrcek.ankipron.anki;

import cz.janhrcek.ankipron.PronDownloader;
import java.sql.Connection;
import java.sql.DriverManager;
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
            = "select tags,flds,sfld from notes where tags like '%wort%' and flds not like '%.mp3%';";

    private static final String FIELD_SEPARATOR = ""; //Not a space!

    public List<String> getWordsWithoutPron() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        List<String> wordsWithoutPron = new ArrayList<>();
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + PronDownloader.PROJECT_DIR + "collection.anki2");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery(QUERY);
            while (rs.next()) {
                String flds = rs.getString("flds");

                String deutsch = flds.split(FIELD_SEPARATOR)[1];
                Matcher matcher = null;
                // 1. Simplest case: only 1 word
                if (deutsch.split(" ").length == 1) {

                    if (deutsch.contains("/") || deutsch.contains("(") || deutsch.contains(")")) {
                        deutsch = deutsch.replaceAll("/", "").replaceAll("\\(", "").replaceAll("\\)", "");
                    }
                    wordsWithoutPron.add(deutsch);
                    //2. More complex cases, of the form <article> <wort> (-[e]s, -e)
                } else if ((matcher = SUBSTANTIVE_WITH_ARTICLE.matcher(deutsch)).matches()) {
                    wordsWithoutPron.add(matcher.group(1));
                }

                //TODO 3. solve most complex cases, irregular werbs etc.
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
        return wordsWithoutPron;
    }

    //Word of the form "e Frau (-, -en)" -> only "Frau" will be sought for
    private static final Pattern SUBSTANTIVE_WITH_ARTICLE = Pattern.compile("[res] ([^\\s]*) \\(.*\\)");
}
