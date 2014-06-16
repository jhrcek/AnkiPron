package cz.janhrcek.ankipron.search;

import java.util.List;
import java.util.Map;

/**
 * The goal of the searcher is to search for the word and get a URL of a downloadable pronunciation audio file for that
 * word.
 *
 * @author jhrcek
 */
public interface Searcher {

    Map<String, String> batchSearch(List<String> words);

    SearchResult search(String word);

    String getPronURL();

    void close();
}
