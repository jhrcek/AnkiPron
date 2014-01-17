package cz.janhrcek.ankipron.search;

import java.util.Objects;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author jhrcek
 */
public abstract class AbstractSearcher implements Searcher {

    protected final WebDriver driver;
    protected String pronUrl = null;
    protected int counter = 0; //Counting the number of words searched for loggin purposes

    public AbstractSearcher(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver);
    }

    @Override
    public String getPronURL() {
        return pronUrl;
    }

    @Override
    public void close() {
        driver.close();
    }
}
