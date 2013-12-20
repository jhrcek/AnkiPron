package cz.janhrcek.ankipron;

import java.util.List;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This class encapsulates the use of a web page dwds.de
 *
 * @author jhrcek
 */
public class DWDS {

    private final WebDriver driver;
    private static final By SEARCH_INPUT = By.cssSelector("#query_fast_search");
    private static final By SEARCH_SUBMIT = By.cssSelector("#searchbar_submit");
    private static final By PANEL_LOADING = By.cssSelector(".panel_loading");
    private static final By PRONOUNCIATION = By.cssSelector("#oneBitInsert_1");
    private static final By WORD_FOUND = By.cssSelector(".wb_lzga");
    private static final By PANEL_CLOSER = By.cssSelector(".panel_remove>img");

    private String pronUrl = null;
    private int counter = 0;

    public DWDS() {
        driver = new FirefoxDriver();
        performInitialSearch();
        closeUnwantedPanels();
    }

    private void performInitialSearch() {
        driver.get("http://www.dwds.de/?qu=und");
        waitForPageLoad();
    }

    private void closeUnwantedPanels() {
        List<WebElement> closeImages;
        do {
            closeImages = driver.findElements(PANEL_CLOSER);
            closeImages.get(1).click();
        } while (closeImages.size() > 2);
    }

    public SearchResult search(String word) {
        Objects.requireNonNull(word, "aWord must not be null!");
        pronUrl = null;
        System.out.print("Processing word " + ++counter + ": '" + word + "' - ");
        driver.findElement(SEARCH_INPUT).clear();
        driver.findElement(SEARCH_INPUT).sendKeys(word);
        driver.findElement(SEARCH_SUBMIT).click();

        try {
            waitForPageLoad();
            driver.findElement(WORD_FOUND);
            try {
                driver.findElement(PRONOUNCIATION);
                pronUrl = getAudioURL();
                System.out.println("Aussprache URL: " + pronUrl);
                return SearchResult.PRON_FOUND;
            } catch (NoSuchElementException ex) { //Word in dictionary, but no pronounciation available
                System.out.println("Aussprache nicht vorhanden");
                return SearchResult.PRON_NOT_AVAILABLE;
            }
        } catch (TimeoutException | NoSuchElementException ex) { //Word not in the dictionary
            List<WebElement> notFoundElems = driver.findElements(By.cssSelector("#panel_147 div.panel_empty"));
            if (!notFoundElems.isEmpty()) {
                System.out.println(notFoundElems.get(0).getText());
                return SearchResult.WORD_NOT_FOUND;
            }

        }
        return SearchResult.UNKNOWN;
    }

    private void waitForPageLoad() {
        new WebDriverWait(driver, 5, 100).until(
                ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(PANEL_LOADING)));
    }

    public enum SearchResult {

        PRON_FOUND,
        PRON_NOT_AVAILABLE,
        WORD_NOT_FOUND,
        UNKNOWN;
    }

    private String getAudioURL() {
        String flashvars = driver.findElement(PRONOUNCIATION).getAttribute("flashvars");
        int mediaUrlBeginIndex = flashvars.indexOf("filename=") + "filename=".length();
        return flashvars.substring(mediaUrlBeginIndex);
    }

    public String getPronURL() {
        return pronUrl;
    }

    public void close() {
        driver.close();
    }
}
