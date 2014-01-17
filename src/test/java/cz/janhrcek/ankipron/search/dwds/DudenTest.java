package cz.janhrcek.ankipron.search.dwds;

import cz.janhrcek.ankipron.search.SearchResult;
import cz.janhrcek.ankipron.search.Searcher;
import cz.janhrcek.ankipron.search.Duden;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 *
 * @author jhrcek
 */
public class DudenTest {

    private final Searcher duden = new Duden(new FirefoxDriver());

    @Test
    public void wordWithoutPron() {
        assertResutlAndPronURL("Ökumene", SearchResult.PRON_NOT_AVAILABLE, null);
        assertResutlAndPronURL("Konzentrationslager", SearchResult.PRON_NOT_AVAILABLE, null);
    }

    @Test
    public void wordNotFound() {
        assertResutlAndPronURL("nonexistent", SearchResult.WORD_NOT_FOUND, null);
        assertResutlAndPronURL("otherNonexistent", SearchResult.WORD_NOT_FOUND, null);
    }

    @Test
    public void pronFound() {
        assertResutlAndPronURL("Bruder", SearchResult.PRON_FOUND,
                "http://www.duden.de/_media_/audio/ID4113233_375377226.mp3");
        assertResutlAndPronURL("und", SearchResult.PRON_FOUND,
                "http://www.duden.de/_media_/audio/ID4108567_408496814.mp3");
        assertResutlAndPronURL("Schwester", SearchResult.PRON_FOUND,
                "http://www.duden.de/_media_/audio/ID4113088_151622151.mp3");
    }

    private void assertResutlAndPronURL(String word, SearchResult searchResult, String pronUrl) {
        assertEquals(duden.search(word), searchResult);
        assertEquals(duden.getPronURL(), pronUrl);
    }

    @AfterClass
    public void closeDwds() {
        if (duden != null) {
            duden.close();
        }
    }
}
