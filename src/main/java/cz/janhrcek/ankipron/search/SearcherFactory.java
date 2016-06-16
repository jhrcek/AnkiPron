package cz.janhrcek.ankipron.search;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 *
 * @author hrk
 */
public class SearcherFactory {

    public static Searcher newDuden() {
        return new Duden(createWebDriver());
    }

    public static Searcher newDwds() {
        return new DWDS(createWebDriver());
    }

    public static Searcher newSeznam() {
        return new SeznamSlovnik(createWebDriver());
    }

    private static WebDriver createWebDriver() {
        String currentDir = System.getProperty("user.dir");
        Path chromedriverBinary = Paths.get(currentDir, "chromedriver");
        if (!Files.exists(chromedriverBinary)) {
            throw new IllegalStateException("Expecting chromedriver binary to exist: " + chromedriverBinary.toAbsolutePath());
        }
        
        return new ChromeDriver();
    }
}
