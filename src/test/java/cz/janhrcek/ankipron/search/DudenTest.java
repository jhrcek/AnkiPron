package cz.janhrcek.ankipron.search;

import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jhrcek
 */
public class DudenTest {

    private static Searcher DUDEN;

    @Test
    public void wordWithoutPron() {
        assertResutlAndPronURL("Ökumene", SearchResult.PRON_NOT_AVAILABLE, null);
        assertResutlAndPronURL("Konzentrationslager", SearchResult.PRON_NOT_AVAILABLE, null);
    }

    @Test
    public void wordNotFound() {
        assertResutlAndPronURL("nonExistentNonSense", SearchResult.WORD_NOT_FOUND, null);
        assertResutlAndPronURL("otherNonexistent", SearchResult.WORD_NOT_FOUND, null);
    }

    @Test
    public void pronFound() {
        assertResutlAndPronURL("Bruder", SearchResult.PRON_FOUND,
                "https://www.duden.de/_media_/audio/ID4113233_375377226.mp3");
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "https://www.duden.de/_media_/audio/ID4108567_408496814.mp3");
        assertResutlAndPronURL("Schwester", SearchResult.PRON_FOUND,
                "https://www.duden.de/_media_/audio/ID4113088_151622151.mp3");
    }

    private void assertResutlAndPronURL(String word, SearchResult searchResult, String pronUrl) {
        assertEquals(searchResult, DUDEN.search(word));
        assertEquals(pronUrl, DUDEN.getPronURL());
    }

    @BeforeClass
    public static void initSearcher() {
        DUDEN = SearcherFactory.newDuden();
    }

    @AfterClass
    public static void closeSearcher() {
        if (DUDEN != null) {
            DUDEN.close();
        }
    }
}
