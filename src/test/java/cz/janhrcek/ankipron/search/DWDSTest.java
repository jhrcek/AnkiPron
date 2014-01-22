package cz.janhrcek.ankipron.search;

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
                "http://media.dwds.de/dwds2/release_new/b95beb60f8f6ca5f28cdc0685c469319.mp3");
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release_new/5496cb3673a0e6f630ad7389c224971d.mp3");
        assertResutlAndPronURL("Abscheulichkeit", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release_new/eadba7dfcc02656fa72fb8f9f31085a7.mp3");
    }

    @Test
    public void withouPronAfterWithPron() {
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release_new/5496cb3673a0e6f630ad7389c224971d.mp3");
        //Make sure pron Url is nulled after successfull search
        assertResutlAndPronURL("Ärger", SearchResult.PRON_NOT_AVAILABLE, null);
    }

    @Test
    public void notFoundAfterWithPron() {
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "http://media.dwds.de/dwds2/release_new/5496cb3673a0e6f630ad7389c224971d.mp3");
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
