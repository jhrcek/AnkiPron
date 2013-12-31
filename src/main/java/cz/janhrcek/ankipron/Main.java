package cz.janhrcek.ankipron;

import cz.janhrcek.ankipron.anki.AnkiDatabaseUtil;
import java.io.IOException;
import java.util.List;

import static cz.janhrcek.ankipron.PronDownloader.PROJECT_DIR;

/**
 *
 * @author jhrcek
 */
public class Main {

    private static final String INPUT_FILE = PROJECT_DIR + "in.txt";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //Check which words don't have pronounciation associated with them in anki db
        List<String> wordsWithoutPron = new AnkiDatabaseUtil().getWordsWithoutPron();
        //Alternatively, we can just read them from input file
        //wordsWithoutPron = FileUtils.readLines(new File(INPUT_FILE));

        PronDownloader downloader = new PronDownloader();

        //Don't download words we already have downloaded
        wordsWithoutPron.removeAll(downloader.getDownloadedProns());

        //Don't download words that don't have downloadable pron on DWDS
        wordsWithoutPron.removeAll(downloader.getWordsForWhichPronNotAvailable());

        //Don't try to download words that cannot be found on DWDS
        wordsWithoutPron.removeAll(downloader.getWordsNotFound());

        System.out.println("----- " + wordsWithoutPron.size() + " words remaining to download");
        new PronDownloader().performDownload(wordsWithoutPron);
    }
}
