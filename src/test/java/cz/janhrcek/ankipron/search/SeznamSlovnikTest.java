package cz.janhrcek.ankipron.search;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 *
 * @author jhrcek
 */
public class SeznamSlovnikTest {

    private final Searcher seznam = SearcherFactory.newSeznam();

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
        assertEquals(seznam.search(word), searchResult);
        assertEquals(seznam.getPronURL(), pronUrl);
    }

    @AfterClass
    public void closeSearcher() {
        if (seznam != null) {
            seznam.close();
        }
    }
}
