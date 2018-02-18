package cz.janhrcek.ankipron.search;

import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Experiment with downloading TTS mp3 from Google translate.
 */
public class GoogleTranslate {

    private static WebDriver driver;

    public static void main(String[] args) {
        LoggingPreferences loggingPrefs = new LoggingPreferences();
        loggingPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, loggingPrefs);

        driver = new ChromeDriver(chromeOptions);
        driver.get("https://translate.google.com/");
        driver.findElement(By.id("gt-sl-gms")).click();
        driver.findElement(By.xpath("//div[contains(text(),'German')]")).click();
        driver.findElement(By.id("source")).sendKeys("Ihm fällt aber auch immer irgendeine Antwort ein.");

        WebElement generateSpeech = new WebDriverWait(driver, 2000)
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("gt-src-listen")));

        generateSpeech.click();

        LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);

        Optional<String> maybeUrl = StreamSupport.stream(logEntries.spliterator(), false)
                .filter(entry -> entry.getMessage().contains("translate_tts"))
                .map(entry -> extractUrl(entry.getMessage()))
                .findFirst();

        if (maybeUrl.isPresent()) {
            System.out.println("Url: " + maybeUrl.get());
        }
        driver.close();
    }

    private static String extractUrl(String logEntry) {
        int urlStart = logEntry.indexOf("\"url\":\"") + "\"url\":\"".length();
        String urlTmp = logEntry.substring(urlStart);
        return urlTmp.split("\"")[0];
    }
}
