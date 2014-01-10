package cz.janhrcek.ankipron;

import cz.janhrcek.ankipron.anki.AnkiDatabase;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author jhrcek
 */
public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<String> alreadyDownloaded = Project.getWordsDownloaded();
        List<String> notInDictionary = Project.getWordsNotFound();
        List<String> withoutDownloadablePron = Project.getWordsForWhichPronNotAvailable();

        //Check which words don't have pronounciation associated with them in anki db
        List<String> wordsWithoutPron = new AnkiDatabase().getWordsWithoutPron();
        logCollectionInfo(wordsWithoutPron, "Words in Anki, that don't have associated pron", false);
        wordsWithoutPron.removeAll(alreadyDownloaded);
        wordsWithoutPron.removeAll(withoutDownloadablePron);
        wordsWithoutPron.removeAll(notInDictionary);

        logCollectionInfo(alreadyDownloaded, "Words already downloaded", false);
        logCollectionInfo(notInDictionary, "Words, that can't be found in DWDS", false);
        logCollectionInfo(withoutDownloadablePron, "Words that don't have downloadable pron", false);
        logCollectionInfo(wordsWithoutPron, "Words remaining to be downloaded", true);

        PronDownloader downloader = new PronDownloader();
        downloader.performDownload(wordsWithoutPron);
    }

    private static void logCollectionInfo(Collection<?> c, String description, boolean alsoLogContents) {
        System.out.println("----- " + description + " :" + c.size());
        if (alsoLogContents) {
            System.out.println(c);
        }
    }
}
