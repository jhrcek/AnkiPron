package cz.janhrcek.ankipron;

import cz.janhrcek.ankipron.anki.AnkiDatabase;
import cz.janhrcek.ankipron.anki.AnkiNote;
import cz.janhrcek.ankipron.search.DWDS;
import cz.janhrcek.ankipron.search.Duden;
import cz.janhrcek.ankipron.search.Searcher;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length == 0) {
            printUsageAndExit();
        }

        switch (args[0]) {
            case "verify": //Use case 1: Verify that word data in anki database have proper format
                verifyAnkiNotesIntegrity();
                break;
            case "download": //Use case 2: download pronunciation mp3s for words from anki DB
                try (Searcher searcher = createSearcher(args)) {
                    downloadMp3sForWords(searcher);
                }
                break;
            case "add": //Use case 3: having pron mp3s downloaded, add references to them to anki DB
                addMp3RefsToAnkiDb();
                break;
            default:
                printUsageAndExit();
        }
    }

    /* Instantiate searcher based on cmd line args. */
    private static Searcher createSearcher(String[] args) {
        WebDriver wd = new FirefoxDriver();
        if (args.length > 1 && "duden".equals(args[1])) {//Use DWDS by default, Duden only on explicit request
            return new Duden(wd);
        } else {
            return new DWDS(wd);
        }
    }

    public static void downloadMp3sForWords(Searcher searcher) throws IOException, ClassNotFoundException {
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

        Map<String, String> pronsToDownload = searcher.batchSearch(wordsWithoutPron);
        PronDownloader downloader = new PronDownloader(Project.getDownloadDir());
        downloader.performDownload(pronsToDownload);
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
            String tags = note.getTags();
            String articleError = "Note has flag %s, but does not start with %s: %s%n";
            //Notes with tag Femininum must have german field starting with e or r/e
            if (tags.contains("Femininum") && !(deutsch.startsWith("e ") || deutsch.startsWith("r/e "))) {
                System.err.printf(articleError, "Femininum", "e or r/e", note);
            }

            //Notes with tag Maskulinum must start with r or r/e or r/s
            if (tags.contains("Maskulinum")
                    && !(deutsch.startsWith("r ") || deutsch.startsWith("r/e ")
                    || deutsch.startsWith("r/s "))) {
                System.err.printf(articleError, "Maskulinum", "r or r/e", note);
            }

            //Notes with tag Neutrum must start with s or (s)
            if (tags.contains("Neutrum")
                    && !(deutsch.startsWith("s ") || deutsch.startsWith("(s) ")
                    || deutsch.startsWith("r/s "))) {
                System.err.printf(articleError, "Neutrum", "s or (s)", note);
            }

            //No nbsp; in notes!
            if (note.getFlds().contains("nbsp;")) {
                System.err.printf("Note contains nbsp; :%s%n", note);
            }

            //No special characters in words!
            String word = note.getWord();
            if (word != null && (word.contains("<") || word.contains(" ") || word.contains(">") || word.contains("­"))) {
                System.err.printf("Note contains one of characters <, ,>,-: %s%n", note);
            }

            //When note has Maskulinum, Femininum or Neutrum, then it must have wort
            if ((tags.contains("Maskulinum") || tags.contains("Femininum") || tags.contains("Neutrum"))
                    && !tags.contains("wort")) {
                System.err.printf("Note has Maskulinu, Femininum or Neutrum, but doesn't have wort: %s%n", note);
            }
        }
    }

    private static void logCollectionInfo(Collection<?> c, String description, boolean alsoLogContents) {
        System.out.println("----- " + description + " :" + c.size());
        if (alsoLogContents) {
            System.out.println(c);
        }
    }

    private static void printUsageAndExit() {
        System.err.println("Usage: java -jar AnkiPron.jar [verify|download[dwds|duden]|addprons]");
        System.exit(1);
    }
}
