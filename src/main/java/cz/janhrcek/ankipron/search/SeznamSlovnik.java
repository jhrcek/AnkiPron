package cz.janhrcek.ankipron.search;

import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 * @author jhrcek
 */
public class SeznamSlovnik extends AbstractSearcher {

    private static final String INITIAL_URL = "http://slovnik.seznam.cz/de/?q=und";
    private static final By SOUND = By.id("playsound");
    private static final By SEARCH_INPUT = By.id("q");
    private static final By SEARCH_BUTTON = By.id("searchButton");

    private static final By SUGGESTION = By.xpath("//h3[contains(text(), 'Nechtěli jste hledat:')]");
    private static final By NOTHING_FOUND = By.xpath("//h3[contains(text(), 'nebylo nic nalezeno')]");

    public SeznamSlovnik(WebDriver driver) {
        super(driver);
        driver.get(INITIAL_URL);
    }

    @Override
    public SearchResult search(String word) {
        Objects.requireNonNull(word, "aWord must not be null!");
        WebElement searchInput = driver.findElement(SEARCH_INPUT);
        searchInput.clear();
        searchInput.sendKeys(word);
        driver.findElement(SEARCH_BUTTON).click();

        System.out.print("Searching word " + ++counter + ": '" + word + "' - ");
        pronUrl = null;

        if (wordNotFound()) {
            return SearchResult.WORD_NOT_FOUND;
        }
        try {
            pronUrl = driver.findElement(SOUND).getAttribute("href");
            System.out.println("Pron URL: " + pronUrl);
            return SearchResult.PRON_FOUND;
        } catch (NoSuchElementException ex) { //Word in dictionary, but no pronounciation available
            System.out.println("Pron NOT available");
            return SearchResult.PRON_NOT_AVAILABLE;
        }
    }

    public boolean wordNotFound() {
        return !driver.findElements(SUGGESTION).isEmpty()
                || !driver.findElements(NOTHING_FOUND).isEmpty();
    }
}
