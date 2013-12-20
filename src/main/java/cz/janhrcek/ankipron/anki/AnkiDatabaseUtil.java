package cz.janhrcek.ankipron.anki;

import cz.janhrcek.ankipron.PronDownloader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

                if (deutsch.split(" ").length == 1) { // 1. Simplest case: only 1 word
                    if (deutsch.contains("/") || deutsch.contains("(") || deutsch.contains(")")) {
                        deutsch = deutsch.replaceAll("/", "").replaceAll("\\(", "").replaceAll("\\)", "");
                    }
                    wordsWithoutPron.add(deutsch);
                }
                //TODO 2. solve more complex cases, of the form <article> <wort> (-[e]s, -e)
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
}
