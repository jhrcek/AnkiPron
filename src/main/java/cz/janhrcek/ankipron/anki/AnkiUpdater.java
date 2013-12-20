package cz.janhrcek.ankipron.anki;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AnkiUpdater {

    //Select all visible fields of notes, that contain flag wort, but don't have mp3 file associated with them
    private static final String QUERY
            = "select tags,flds,sfld from notes where tags like '%wort%' and flds not like '%.mp3%';";

    private static final String FIELD_SEPARATOR = ""; //Not a space!

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        int counter = 0;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:/home/jhrcek/Temp/AnkiDeutschPron/collection.anki2");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery(QUERY);
            while (rs.next()) {
                String flds = rs.getString("flds");

                String deutsch = flds.split(FIELD_SEPARATOR)[1];

                if (deutsch.split(" ").length == 1) {
                    System.out.println("TYPE 1 Wort " + counter++ + " = '" + deutsch + "'");
                } else if (deutsch.contains("(") && deutsch.contains(")")) {
                    System.out.println("TYPE 2 Wort " + counter++ + " = '" + deutsch + "'");
                } else {
                    System.out.println("TYPE 3 Wort " + counter++ + " = '" + deutsch + "'");
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
    }
}
