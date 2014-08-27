package cz.janhrcek.ankipron.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openqa.selenium.WebDriver;

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
        driver.quit();
    }

    @Override
    public Map<String, String> batchSearch(List<String> words) {
        System.out.println(words.size() + " words to search");
        Map<String, String> word2pronUrl = new HashMap<>();
        List<String> wordsNotFound = new ArrayList<>();
        List<String> pronNotAvailable = new ArrayList<>();
        List<String> unknownErrors = new ArrayList<>();

        for (String word : words) {
            SearchResult sr = search(word);
            switch (sr) {
                case PRON_FOUND:
                    word2pronUrl.put(word, getPronURL());
                    break;
                case WORD_NOT_FOUND:
                    wordsNotFound.add(word);
                    break;
                case PRON_NOT_AVAILABLE:
                    pronNotAvailable.add(word);
                    break;
                default:
                    unknownErrors.add(word);
            }
        }
        printReport(word2pronUrl.keySet(), wordsNotFound, pronNotAvailable, unknownErrors);
        return word2pronUrl;
    }

    private void printReport(Set<String> wordsFound, List<String> wordsNotFound, List<String> pronNotAvailable,
            List<String> unknownErrors) {
        System.out.println("----- REPORT -----");
        reportCollection("OK", wordsFound);
        reportCollection("NOT FOUND", wordsNotFound);
        reportCollection("PRON NOT AVAILABLE", pronNotAvailable);
        reportCollection("UNKNOWN ERRORS", unknownErrors);
    }

    private void reportCollection(String collectionName, Collection<String> collection) {
        if (!collection.isEmpty()) {
            System.out.printf("%s : %d %s%n", collectionName, collection.size(), collection);
        }
    }
}
