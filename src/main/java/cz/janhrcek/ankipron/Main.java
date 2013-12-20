package cz.janhrcek.ankipron;

import cz.janhrcek.ankipron.anki.AnkiDatabaseUtil;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author jhrcek
 */
public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //Check which words don't have pronounciation associated with them in anki db
        List<String> wordsWithoutPron = new AnkiDatabaseUtil().getWordsWithoutPron();
        //Alternatively, we can just read them from input file
        //wordsWithoutPron = FileUtils.readLines(new File(INPUT_FILE));
        System.out.println("----- " + wordsWithoutPron.size() + " words don't have pron associated in Anki DB");

        //Don't try to download words we already have downloaded
        PronDownloader downloader = new PronDownloader();
        List<String> downloadedProns = downloader.getDownloadedProns();
        System.out.println("----- We have " + downloadedProns.size() + " downloaded files");
        System.out.println("Words: " + downloadedProns);
        wordsWithoutPron.removeAll(downloadedProns);

        //Don't try to download words that we know don't have downloadable pron on DWDS
        List<String> wordsWithoutPronAvailable = downloader.getWordsForWhichPronNotAvailable();
        System.out.println("----- " + wordsWithoutPronAvailable.size() + " words don't have pron available");
        System.out.println("Words: " + wordsWithoutPronAvailable);
        wordsWithoutPron.removeAll(wordsWithoutPronAvailable);

        //TODO: don't try to download words, that cannot be found on DWDS
        System.out.println("----- " + wordsWithoutPron.size() + " words remaining to download");
        new PronDownloader().performDownload(wordsWithoutPron);
    }
}
