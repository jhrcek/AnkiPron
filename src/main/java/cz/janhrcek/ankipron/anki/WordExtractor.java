package cz.janhrcek.ankipron.anki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jhrcek
 */
public class WordExtractor {

    public static final String FIELD_SEPARATOR = "\u001F"; //Unicode Character 'INFORMATION SEPARATOR ONE'

    //Word of the form "e Frau (-, -en)" -> only "Frau" will be sought for
    private static final Pattern SUBSTANTIVE_WITH_ARTICLE = Pattern.compile("[res] ([^\\s]*) \\(.*\\)");
    private static final Pattern SUBST_WITH_ARTICLE = Pattern.compile("^\\(?[res]\\)?(/e)? ([^\\s]*)");
    private static final Pattern SUBSTANTIVE_WITHOUT_DECLINATION = Pattern.compile("\\(*[res]\\)* ([^\\s]*)");
    private static final String THING_IN_PARENS = "\\([^\\)]*\\)"; //Aything surrounded by ()

    /**
     * @param fldsAttribute 'flds' attribute from Anki's 'notes' table, representing 4-tuple: "Czech deutsch note
     * reverse", where each of the 4 fields is separated by FIELD_SEPARATOR
     *
     * @return German word extracted from the deutsch part, that can be searched in the dictionary and whose
     * pronunciation we want to search
     */
    public String extractWord(String fldsAttribute) {
        String deutsch = getFields(fldsAttribute)[1];
        return extractFromDeutschField(deutsch);
    }

    /**
     * @param fldsAttribute
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
