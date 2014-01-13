package cz.janhrcek.ankipron.search.dwds;

import cz.janhrcek.ankipron.search.SearchResult;
import cz.janhrcek.ankipron.search.Searcher;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 *
 * @author jhrcek
 */
public class DWDSTest {

    private final Searcher dwds = new DWDS(new FirefoxDriver());

    @Test
    public void wordWithoutPron() {
        assertResutlAndPronURL("Ärger", SearchResult.PRON_NOT_AVAILABLE, null);
        assertResutlAndPronURL("Konzentrationslager", SearchResult.PRON_NOT_AVAILABLE, null);
        assertResutlAndPronURL("zweifellos", SearchResult.PRON_NOT_AVAILABLE, null);
    }

    @Test
    public void wordNotFound() {
        assertResutlAndPronURL("nonexistent", SearchResult.WORD_NOT_FOUND, null);
        assertResutlAndPronURL("abführend", SearchResult.WORD_NOT_FOUND, null);
        assertResutlAndPronURL("Aufenthaltszimmer", SearchResult.WORD_NOT_FOUND, null);
        assertResutlAndPronURL("letztendlich", SearchResult.WORD_NOT_FOUND, null);
    }

    @Test
    public void pronFound() {
        assertResutlAndPronURL("Bruder", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release/dd77b9664ee1ecfcb6170ae9a150f913.mp3");
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release/3b7763834e23ec5ea9c64a7052bf08ba.mp3");
        assertResutlAndPronURL("Abscheulichkeit", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release/d60f4985bdce5e7aedd04a634a8836ac.mp3");
    }

    @Test
    public void withouPronAfterWithPron() {
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release/3b7763834e23ec5ea9c64a7052bf08ba.mp3");
        //Make sure pron Url is nulled after successfull search
        assertResutlAndPronURL("Ärger", SearchResult.PRON_NOT_AVAILABLE, null);
    }

    @Test
    public void notFoundAfterWithPron() {
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release/3b7763834e23ec5ea9c64a7052bf08ba.mp3");
        //Make sure pron URL is nulled after successfull search
        assertResutlAndPronURL("nonexistent", SearchResult.WORD_NOT_FOUND, null);
    }

    private void assertResutlAndPronURL(String word, SearchResult searchResult, String pronUrl) {
        assertEquals(dwds.search(word), searchResult);
        assertEquals(dwds.getPronURL(), pronUrl);
    }

    @AfterClass
    public void closeDwds() {
        if (dwds != null) {
            dwds.close();
        }
    }
}
