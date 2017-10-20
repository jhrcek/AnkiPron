package cz.janhrcek.ankipron.anki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jhrcek
 */
class WordExtractor {

    public static final String FIELD_SEPARATOR = "\u001F"; //Unicode Character 'INFORMATION SEPARATOR ONE'

    //Word of the form "e Frau" -> only "Frau" will be sought for
    private static final Pattern SUBST_WITH_ARTICLE = Pattern.compile("^\\(?[res]\\)?(/e)? ([^\\s]*)");
    private static final String THING_IN_PARENS = "\\([^\\)]*\\)"; //Anything surrounded by ()
    private static final String PRON_REFERENCE = "\\[sound:.*\\.mp3\\]"; //Reference to pron mp3 file
    private static final String PART_AFTER_DASH = " - .*";

    /**
     * @param fldsAttribute 'flds' attribute from Anki's 'notes' table, representing 4-tuple: "Czech deutsch note
     *                      reverse", where each of the 4 fields is separated by FIELD_SEPARATOR
     * @return German word extracted from the deutsch part, that can be searched in the dictionary and whose
     * pronunciation we want to search
     */
    public String extractWord(String fldsAttribute) {
        String deutsch = getFields(fldsAttribute)[1];
        return extractFromDeutschField(deutsch);
    }

    /**
     * @param fldsAttribute Value of "flds" attribute
     * @return the 4 fields of the fldsAttribute
     * @throws IllegalArgumentException when fldsAttribute does not have 4 fields separated by FIELD_SEPARATOR
     */
    public String[] getFields(String fldsAttribute) {
        String[] fields = fldsAttribute.split(FIELD_SEPARATOR);
        if (fields.length != 4) {
            throw new IllegalArgumentException("flds had " + fields.length + " instead of the expected 4: "
                    + fldsAttribute);
        }
        return fields;
    }

    /**
     * @param deutsch field extracted from flds attribute, which represents German word with some associated info
     * @return the word we can search in the dictionary
     */
    private String extractFromDeutschField(String deutsch) {
        // Remove already existing pron references
        deutsch = deutsch.replaceAll(PRON_REFERENCE, "").trim();

        //When deutsch contains more things separated by space
        if (deutsch.split(" ").length != 1) {
            //Remove all things in parenthesis
            deutsch = deutsch.replaceAll(THING_IN_PARENS, "").trim();

            //Remove everything after dash (if present)
            deutsch = deutsch.replaceAll(PART_AFTER_DASH, "");

            //Remove article if present
            Matcher matcher;
            if ((matcher = SUBST_WITH_ARTICLE.matcher(deutsch)).matches()) {
                deutsch = matcher.group(2);
            }

            if (deutsch.split(" ").length != 1) {
                System.out.println("WARNING - can't extract word: '" + deutsch + "'");
                return null;
            }
        }

        // Remove all occurences of '/', '(' and ')'
        return deutsch.replaceAll("[/\\(\\)]", "").trim();
    }
}
