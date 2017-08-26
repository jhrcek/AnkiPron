package cz.janhrcek.ankipron.search;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 *
 * @author hrk
 */
public class SearcherFactory {

    public static Searcher newDuden() {
        return new Duden(createWebDriver(true));
    }

    public static Searcher newDwds() {
        return new DWDS(createWebDriver(false));
    }

    public static Searcher newSeznam() {
        return new SeznamSlovnik(createWebDriver(false));
    }

    private static WebDriver createWebDriver(boolean withAdBlock) {
        String currentDir = System.getProperty("user.dir");
        Path chromedriverBinary = Paths.get(currentDir, "chromedriver");
        
        if (!Files.exists(chromedriverBinary)) {
            throw new IllegalStateException("Expecting chromedriver binary to exist: " + chromedriverBinary.toAbsolutePath()
                    + "\nYou can download it from https://sites.google.com/a/chromium.org/chromedriver/downloads");
        }

        if (withAdBlock) {
            return createWebDriverWithAdBlock(currentDir);
        }

        return new ChromeDriver();
    }

    private static WebDriver createWebDriverWithAdBlock(String currentDir) {
        Path adBlockPlusFile = Paths.get(currentDir, "adblockpluschrome.crx");
        if (!Files.exists(adBlockPlusFile)) {
            String msg = "Expecting AdBlockPlus extension file to exist: " + adBlockPlusFile.toAbsolutePath()
                    + "\nYou can download it from https://downloads.adblockplus.org/devbuilds/adblockpluschrome/";
            throw new IllegalStateException(msg);
        }

        ChromeOptions options = new ChromeOptions();
        options.addExtensions(adBlockPlusFile.toFile());
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        return new ChromeDriver(capabilities);
    }
}
