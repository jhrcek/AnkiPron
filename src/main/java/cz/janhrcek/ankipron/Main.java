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

        //Check which ones we already have downloaded
        List<String> downloadedProns = new PronDownloader().getDownloadedProns();
        System.out.println("----- We have " + downloadedProns.size() + " downloaded files");
        System.out.println("Words: " + downloadedProns);

        //Check which words don't have pronounciation associated with them in anki db
        List<String> wordsWithoutPron = new AnkiDatabaseUtil().getWordsWithoutPron();
        //Alternatively, we can just read them from input file
        //wordsWithoutPron = FileUtils.readLines(new File(INPUT_FILE));
        System.out.println("----- " + wordsWithoutPron.size() + " words don't have pron associated in Anki DB");

        //Only leave words for which we don't have pron downloaded
        wordsWithoutPron.removeAll(downloadedProns);

        System.out.println("----- " + wordsWithoutPron.size() + " words remaining to download");
        new PronDownloader().performDownload(wordsWithoutPron);
    }
}
