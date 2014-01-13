package cz.janhrcek.ankipron;

import cz.janhrcek.ankipron.anki.AnkiDatabase;
import cz.janhrcek.ankipron.anki.AnkiNote;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author jhrcek
 */
public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        verifyAnkiNotesIntegrity();
        downloadMp3sForWords();
    }

    public static void downloadMp3sForWords() throws IOException, ClassNotFoundException {
        List<String> alreadyDownloaded = Project.getWordsDownloaded();
        List<String> notInDictionary = Project.getWordsNotFound();
        List<String> withoutDownloadablePron = Project.getWordsForWhichPronNotAvailable();

        //Check which words don't have pronounciation associated with them in anki db
        List<String> wordsWithoutPron = new AnkiDatabase().getWordsToSearch();
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

    /**
     * We must make sure that Anki DB data have sufficient quality to begin with.
     */
    public static void verifyAnkiNotesIntegrity() throws ClassNotFoundException {
        List<AnkiNote> notesWithoutPron = new AnkiDatabase().getNotesWithoutPron();
        for (AnkiNote note : notesWithoutPron) {
            String deutsch = note.getDeutsch();
            //Notes with tag Femininum must have german field starting with e or r/e
            if (note.getTags().contains("Femininum")
                    && !(deutsch.startsWith("e ") || deutsch.startsWith("r/e "))) {
                System.out.println(note);
            }

            //Notes with tag Maskulinum must start with r or r/e or r/s
            if (note.getTags().contains("Maskulinum")
                    && !(deutsch.startsWith("r ") || deutsch.startsWith("r/e ")
                    || deutsch.startsWith("r/s "))) {
                System.out.println(note);
            }

            //Notes with tag Neutrum must start with s or (s)
            if (note.getTags().contains("Neutrum")
                    && !(deutsch.startsWith("s ") || deutsch.startsWith("(s) ")
                    || deutsch.startsWith("r/s "))) {
                System.out.println(note);
            }

            //No nbsp; in notes!
            if (deutsch.contains("nbsp;")) {
                System.out.println(note);
            }

            //No special characters in words!
            String word = note.getWord();
            if (word != null && (word.contains("<") || word.contains(" ") || word.contains(">") || word.contains("­"))) {
                System.out.println(note);
            }
        }
    }

    private static void logCollectionInfo(Collection<?> c, String description, boolean alsoLogContents) {
        System.out.println("----- " + description + " :" + c.size());
        if (alsoLogContents) {
            System.out.println(c);
        }
    }
}
