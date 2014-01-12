package cz.janhrcek.ankipron.anki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jhrcek
 */
public class WordExtractor {

    public static final String FIELD_SEPARATOR = "\u001F"; //Not a space!

    //Word of the form "e Frau (-, -en)" -> only "Frau" will be sought for
    private static final Pattern SUBSTANTIVE_WITH_ARTICLE = Pattern.compile("[res] ([^\\s]*) \\(.*\\)");
    private static final Pattern SUBST_WITH_ARTICLE = Pattern.compile("^\\(?[res]\\)?(/e)? ([^\\s]*)");
    private static final Pattern SUBSTANTIVE_WITHOUT_DECLINATION = Pattern.compile("\\(*[res]\\)* ([^\\s]*)");
    private static final String THING_IN_PARENS = "\\([^\\)]*\\)"; //Aything surrounded by ()

    /**
     * @param databaseField 'flds' attribute from Anki's 'notes' table
     * @return german word that can be searched in the dictionary and whose pron we want to search
     */
    public String extractWord(String databaseField) {
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
}
