package cz.janhrcek.ankipron.search;

/**
 * The goal of the searcher is to search for the word and get a URL of a downloadable pronunciation audio file for that
 * word.
 *
 * @author jhrcek
 */
public interface Searcher {

    SearchResult search(String word);

    String getPronURL();

    void close();
}
