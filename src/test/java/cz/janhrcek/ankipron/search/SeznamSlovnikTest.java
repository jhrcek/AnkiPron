package cz.janhrcek.ankipron.search;

import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jhrcek
 */
public class SeznamSlovnikTest {

    private final static Searcher SEZNAM = SearcherFactory.newSeznam();

    @Test
    public void wordWithoutPron() {
    }

    @Test
    public void wordNotFound() {

    }

    @Test
    public void pronFound() {
        assertResutlAndPronURL("Bruder", SearchResult.PRON_FOUND,
                "http://slovnik.seznam.cz/sound/wav/de_cz/ge-006922.wav");
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "http://slovnik.seznam.cz/sound/wav/de_cz/ge-042154.wav");
        assertResutlAndPronURL("Schwester", SearchResult.PRON_FOUND,
                "http://slovnik.seznam.cz/sound/wav/de_cz/ge-035742.wav");
    }

    private void assertResutlAndPronURL(String word, SearchResult searchResult, String pronUrl) {
        assertEquals(searchResult, SEZNAM.search(word));
        assertEquals(pronUrl, SEZNAM.getPronURL());
    }

    @AfterClass
    public static void closeSearcher() {
        if (SEZNAM != null) {
            SEZNAM.close();
        }
    }
}
