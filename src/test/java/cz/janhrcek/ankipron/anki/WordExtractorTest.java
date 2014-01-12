package cz.janhrcek.ankipron.anki;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 *
 * @author jhrcek
 */
public class WordExtractorTest {

    @Test(dataProvider = "words")
    public void extractWord(String flds, String expectedWord) {
        WordExtractor we = new WordExtractor();
        assertEquals(we.extractWord(flds), expectedWord);
    }

    @DataProvider
    private Object[][] words() {
        return new Object[][]{
            {"pralinkae Praline (-, -n)y", "Praline"}
        };
    }
}
