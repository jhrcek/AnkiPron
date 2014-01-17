package cz.janhrcek.ankipron.search;

import java.util.List;
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

    private static final String RESTSCHREIBUNG_URL = "http://www.duden.de/rechtschreibung/";
    private static final By PRONOUNCIATION = By.cssSelector("#mp3_mini_1 a[title^='Als mp3']");
    private static final By WORD_FOUND = By.cssSelector(".lemma");

    public Duden(WebDriver driver) {
        super(driver);
    }

    @Override
    public SearchResult search(String word) {
        Objects.requireNonNull(word, "aWord must not be null!");
        pronUrl = null;
        System.out.print("Searching word " + ++counter + ": '" + word + "' - ");
        driver.get(RESTSCHREIBUNG_URL + normalizeSearchWord(word));

        try {
            driver.findElement(WORD_FOUND);
            try {
                pronUrl = driver.findElement(PRONOUNCIATION).getAttribute("href");
                System.out.println("Pron URL: " + pronUrl);
                return SearchResult.PRON_FOUND;
            } catch (NoSuchElementException nse) { //word in dict, but pron not available
                System.out.println("Pron NOT available");
                return SearchResult.PRON_NOT_AVAILABLE;
            }
        } catch (NoSuchElementException nse) {
            List<WebElement> notFoundElems = driver.findElements(By.cssSelector(".error404 > strong"));
            if (!notFoundElems.isEmpty()) {
                System.out.println(notFoundElems.get(0).getText());
                return SearchResult.WORD_NOT_FOUND;
            }
        }
        return SearchResult.UNKNOWN;
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
