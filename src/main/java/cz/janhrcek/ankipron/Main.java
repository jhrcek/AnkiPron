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

        //Use case 1: for words without pron, download the prons
        verifyAnkiNotesIntegrity();
        //downloadMp3sForWords();

        //Use case 2: having pron mp3s downloaded, add references to them to anki db
        //addMp3RefsToAnkiDb();
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

    public static void addMp3RefsToAnkiDb() throws ClassNotFoundException {
        AnkiDatabase db = new AnkiDatabase();
        List<AnkiNote> notesWithoutPron = db.getNotesWithoutPron();
        db.addMp3ReferencesToAnkiDb(notesWithoutPron);
    }

    /**
     * We must make sure that Anki DB data have sufficient quality to begin with.
     */
    public static void verifyAnkiNotesIntegrity() throws ClassNotFoundException {
        List<AnkiNote> notesWithoutPron = new AnkiDatabase().getNotesWithoutPron();
        for (AnkiNote note : notesWithoutPron) {
            String deutsch = note.getDeutsch();
            String articleError = "Note has flag %s, but does not start with %s: %s%n";
            //Notes with tag Femininum must have german field starting with e or r/e
            if (note.getTags().contains("Femininum")
                    && !(deutsch.startsWith("e ") || deutsch.startsWith("r/e "))) {
                System.err.printf(articleError, "Femininum", "e or r/e", note);
            }

            //Notes with tag Maskulinum must start with r or r/e or r/s
            if (note.getTags().contains("Maskulinum")
                    && !(deutsch.startsWith("r ") || deutsch.startsWith("r/e ")
                    || deutsch.startsWith("r/s "))) {
                System.err.printf(articleError, "Maskulinum", "r or r/e", note);
            }

            //Notes with tag Neutrum must start with s or (s)
            if (note.getTags().contains("Neutrum")
                    && !(deutsch.startsWith("s ") || deutsch.startsWith("(s) ")
                    || deutsch.startsWith("r/s "))) {
                System.err.printf(articleError, "Neutrum", "s or (s)", note);
            }

            //No nbsp; in notes!
            if (deutsch.contains("nbsp;")) {
                System.err.printf("Note contains nbsp; :%s%n", note);
            }

            //No special characters in words!
            String word = note.getWord();
            if (word != null && (word.contains("<") || word.contains(" ") || word.contains(">") || word.contains("­"))) {
                System.err.printf("Note contains one of characters <, ,>,-: %s%n", note);
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
