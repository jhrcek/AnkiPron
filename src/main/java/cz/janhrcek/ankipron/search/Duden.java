package cz.janhrcek.ankipron.search;

import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Represents www.duden.de online corpus.
 *
 * @author jhrcek
 */
public class Duden extends AbstractSearcher {

    private static final String SEARCH_URL = "http://www.duden.de/suchen/dudenonline/";
    private static final By FIRST_HIT = By.cssSelector("#block-duden-tiles-0 h2 > a");
    private static final By PRONOUNCIATION = By.cssSelector("a.audio[title^='Als mp3']");

    public Duden(WebDriver driver) {
        super(driver);
    }

    @Override
    public SearchResult search(String word) {
        Objects.requireNonNull(word, "aWord must not be null!");
        pronUrl = null;
        System.out.print("Searching word " + ++counter + ": '" + word + "' - ");
        driver.get(SEARCH_URL + normalizeSearchWord(word));

        try {
            WebElement firstHitLink = driver.findElement(FIRST_HIT);
            firstHitLink.click();
            try {
                pronUrl = driver.findElement(PRONOUNCIATION).getAttribute("href");
                System.out.println("Pron URL: " + pronUrl);
                return SearchResult.PRON_FOUND;
            } catch (NoSuchElementException nse) { //word in dict, but pron not available
                System.out.println("Pron NOT available");
                return SearchResult.PRON_NOT_AVAILABLE;
            }
        } catch (NoSuchElementException nse) {
            System.out.println("Word not found");
            return SearchResult.WORD_NOT_FOUND;
        }
    }

    private String normalizeSearchWord(String word) {
        return word
                .replaceAll("ß", "sz")
                .replaceAll("Ä", "Ae")
                .replaceAll("ä", "ae")
                .replaceAll("Ö", "Oe")
                .replaceAll("ö", "oe")
                .replaceAll("Ü", "Ue")
                .replaceAll("ü", "ue");
    }
}
