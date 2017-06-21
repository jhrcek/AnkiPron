package cz.janhrcek.ankipron.search;

import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * This class encapsulates the use of a web page dwds.de
 * @author jhrcek
 */
public class DWDS extends AbstractSearcher {

    private static final By PRONUNCIATION = By.cssSelector("audio>source");
    private static final By WORD_FOUND = By.cssSelector(".dwdswb-artikel");

    public DWDS(WebDriver driver) {
        super(driver);
    }

    @Override
    public SearchResult search(String word) {
        Objects.requireNonNull(word, "word");
        wait(2);
        pronUrl = null;
        System.out.print("Searching word " + ++counter + ": '" + word + "' - ");
        driver.get("http://www.dwds.de/?qu=" + word);
        try {
            driver.findElement(WORD_FOUND);
            try {
                pronUrl = getAudioURL();
                System.out.println("Pron URL: " + pronUrl);
                return SearchResult.PRON_FOUND;
            } catch (NoSuchElementException ex) { //Word in dictionary, but pronunciation not available
                System.out.println("Pron NOT available");
                return SearchResult.PRON_NOT_AVAILABLE;
            }
        } catch (TimeoutException | NoSuchElementException ex) {
            System.out.println("Word not found");
            return SearchResult.WORD_NOT_FOUND;
        }
    }

    private String getAudioURL() {
        return driver.findElement(PRONUNCIATION).getAttribute("src");
    }

    // Wait to avoid DWDS throttling repeated searches
    private void wait(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
