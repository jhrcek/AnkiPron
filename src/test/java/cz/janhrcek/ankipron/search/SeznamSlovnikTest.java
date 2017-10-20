package cz.janhrcek.ankipron.search;

import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author jhrcek
 */
public class SeznamSlovnikTest {

    private final static Searcher SEZNAM = SearcherFactory.newSeznam();

    @Test
    public void pronFound() {
        assertUrlFound("Bruder", "https://slovnik.seznam.cz/sound/wav/de_cz/ge-006922.wav");
        assertUrlFound("und", "https://slovnik.seznam.cz/sound/wav/de_cz/ge-042154.wav");
        assertUrlFound("Schwester", "https://slovnik.seznam.cz/sound/wav/de_cz/ge-035742.wav");
    }

    private void assertUrlFound(String word, String pronUrl) {
        assertEquals(SearchResult.PRON_FOUND, SEZNAM.search(word));
        assertEquals(pronUrl, SEZNAM.getPronURL());
    }

    @AfterClass
    public static void closeSearcher() {
        SEZNAM.close();
    }
}
