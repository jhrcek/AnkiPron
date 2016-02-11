package cz.janhrcek.ankipron.search;

import cz.janhrcek.ankipron.Project;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

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
        return new SeznamSlovnik(createWebDriver(true));
    }

    private static WebDriver createWebDriver(boolean withAdBlock) {
        //Try adding AddBlock extension to firefox to spead up Duden search
        if (withAdBlock) {
            Path adblock = Project.getRootDir().resolve("adblock_plus-2.6.9.xpi");
            if (Files.exists(adblock)) {
                FirefoxProfile ffProfile = new FirefoxProfile();
                try {
                    ffProfile.addExtension(adblock.toFile());
                    return new FirefoxDriver(ffProfile);
                } catch (IOException ex) {
                    System.err.println("Failed to add Adblock Plus extension to firefox - the search might be slower");
                }
            } else {
                System.err.println("Adblock file not found - starting Firefox without it: " + adblock);
            }
        }
        return new FirefoxDriver();
    }
}
