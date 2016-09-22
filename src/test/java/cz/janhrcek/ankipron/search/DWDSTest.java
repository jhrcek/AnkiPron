package cz.janhrcek.ankipron.search;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jhrcek
 */
public class DWDSTest {

    private static Searcher dwds;

    @Test
    public void wordWithoutPron() {
        assertResultAndPronURL("Ärger", SearchResult.PRON_NOT_AVAILABLE, false);
        assertResultAndPronURL("Konzentrationslager", SearchResult.PRON_NOT_AVAILABLE, false);
        assertResultAndPronURL("zweifellos", SearchResult.PRON_NOT_AVAILABLE, false);
    }

    @Test
    public void wordNotFound() {
        assertResultAndPronURL("nonexistent", SearchResult.WORD_NOT_FOUND, false);
        assertResultAndPronURL("Aufenthaltszimmer", SearchResult.WORD_NOT_FOUND, false);
        assertResultAndPronURL("letztendlich", SearchResult.WORD_NOT_FOUND, false);
    }

    @Test
    public void pronFound() {
        assertResultAndPronURL("Bruder", SearchResult.PRON_FOUND, true);
        assertResultAndPronURL("und", SearchResult.PRON_FOUND, true);
        assertResultAndPronURL("Abscheulichkeit", SearchResult.PRON_FOUND, true);
    }

    @Test
    public void withoutPronAfterWithPron() {
        assertResultAndPronURL("und", SearchResult.PRON_FOUND, true);
        //Make sure pron Url is nulled after successfull search
        assertResultAndPronURL("Ärger", SearchResult.PRON_NOT_AVAILABLE, false);
    }

    @Test
    public void notFoundAfterWithPron() {
        assertResultAndPronURL("und", SearchResult.PRON_FOUND, true);
        //Make sure pron URL is nulled after successfull search
        assertResultAndPronURL("nonexistent", SearchResult.WORD_NOT_FOUND, false);
    }

    private void assertResultAndPronURL(String word, SearchResult searchResult, boolean urlExpected) {
        assertEquals(searchResult, dwds.search(word));
        String url = dwds.getPronURL();
        if (urlExpected) {
            assertNotNull(url);
            assertTrue(url.startsWith("http://media.dwds.de/dwds2/"));
            assertTrue(url.endsWith(".mp3"));
        } else {
            assertNull(url);
        }
    }

    @BeforeClass
    public static void initDwds() {
        dwds = SearcherFactory.newDwds();
    }

    @AfterClass
    public static void closeDwds() {
        if (dwds != null) {
            dwds.close();
        }
    }
}
