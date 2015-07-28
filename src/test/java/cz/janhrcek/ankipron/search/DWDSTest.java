package cz.janhrcek.ankipron.search;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 *
 * @author jhrcek
 */
public class DWDSTest {

    private final Searcher dwds = SearcherFactory.newDwds();

    @Test
    public void wordWithoutPron() {
        assertResutlAndPronURL("Ärger", SearchResult.PRON_NOT_AVAILABLE, false);
        assertResutlAndPronURL("Konzentrationslager", SearchResult.PRON_NOT_AVAILABLE, false);
        assertResutlAndPronURL("zweifellos", SearchResult.PRON_NOT_AVAILABLE, false);
    }

    @Test
    public void wordNotFound() {
        assertResutlAndPronURL("nonexistent", SearchResult.WORD_NOT_FOUND, false);
        assertResutlAndPronURL("abführend", SearchResult.WORD_NOT_FOUND, false);
        assertResutlAndPronURL("Aufenthaltszimmer", SearchResult.WORD_NOT_FOUND, false);
        assertResutlAndPronURL("letztendlich", SearchResult.WORD_NOT_FOUND, false);
    }

    @Test
    public void pronFound() {
        assertResutlAndPronURL("Bruder", SearchResult.PRON_FOUND, true);
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND, true);
        assertResutlAndPronURL("Abscheulichkeit", SearchResult.PRON_FOUND, true);
    }

    @Test
    public void withouPronAfterWithPron() {
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND, true);
        //Make sure pron Url is nulled after successfull search
        assertResutlAndPronURL("Ärger", SearchResult.PRON_NOT_AVAILABLE, false);
    }

    @Test
    public void notFoundAfterWithPron() {
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND, true);
        //Make sure pron URL is nulled after successfull search
        assertResutlAndPronURL("nonexistent", SearchResult.WORD_NOT_FOUND, false);
    }

    private void assertResutlAndPronURL(String word, SearchResult searchResult, boolean urlExpected) {
        assertEquals(dwds.search(word), searchResult);
        String url = dwds.getPronURL();
        if (urlExpected) {
            assertNotNull(url);
            assertTrue(url.startsWith("http://media.dwds.de/dwds2/"));
            assertTrue(url.endsWith(".mp3"));
        } else {
            assertNull(url);
        }
    }

    @AfterClass
    public void closeDwds() {
        if (dwds != null) {
            dwds.close();
        }
    }
}
