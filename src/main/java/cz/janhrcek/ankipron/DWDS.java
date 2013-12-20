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
    private static final By SEARCH_INPUT = By.cssSelector("#query");
    private static final By FAST_SEARCH_INPUT = By.cssSelector("#query_fast_search");
    private static final By SEARCH_SUBMIT = By.cssSelector("#search_submit");
    private static final By SEARCHBAR_SUBMIT = By.cssSelector("#searchbar_submit");
    private String pronUrl = null;
    private int counter = 0;

    public DWDS() {
        driver = new FirefoxDriver();
        driver.get("http://www.dwds.de");
        performInitialSearch();
        closeUnwantedPanelsOnQuicksearchPage();
    }

    /**
     * To get away from initial search page and onto the quick search page.
     */
    private void performInitialSearch() {
        driver.findElement(SEARCH_INPUT).sendKeys("Aberglaube");
        driver.findElement(SEARCH_SUBMIT).click();
        waitForElementVisible(By.cssSelector(".wb_lzga"));
    }

    private void closeUnwantedPanelsOnQuicksearchPage() {
        try { //Wait for all panels to load so we can close them
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
        }

        List<WebElement> closeImages;
        do {
            closeImages = driver.findElements(By.cssSelector(".panel_remove>img"));
            closeImages.get(1).click();
        } while (closeImages.size() > 2);
    }

    public SearchResult search(String aWord) {
        Objects.requireNonNull(aWord, "aWord must not be null!");
        pronUrl = null;
        System.out.print("Processing word " + ++counter + ": '" + aWord + "' - ");
        driver.findElement(FAST_SEARCH_INPUT).clear();
        driver.findElement(FAST_SEARCH_INPUT).sendKeys(aWord);
        driver.findElement(SEARCHBAR_SUBMIT).click();

        try {
            waitForElementVisible(By.cssSelector(".wb_lzga"));
            try {
                waitForElementVisible(By.cssSelector("#oneBitInsert_1"));
                pronUrl = getAudioURL();
                System.out.println("Aussprache URL: " + pronUrl);
                return SearchResult.PRON_FOUND;
            } catch (TimeoutException ex) { //Word in dictionary, but no pronounciation available
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

    private void waitForElementVisible(By by) {
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public enum SearchResult {

        PRON_FOUND,
        PRON_NOT_AVAILABLE,
        WORD_NOT_FOUND,
        UNKNOWN;
    }

    private String getAudioURL() {
        String flashvars = driver.findElement(By.cssSelector("#oneBitInsert_1")).getAttribute("flashvars");
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
