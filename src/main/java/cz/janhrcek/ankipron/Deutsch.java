package cz.janhrcek.ankipron;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

import static cz.janhrcek.ankipron.DWDS.SearchResult.PRON_FOUND;
import static cz.janhrcek.ankipron.DWDS.SearchResult.PRON_NOT_AVAILABLE;
import static cz.janhrcek.ankipron.DWDS.SearchResult.WORD_NOT_FOUND;

/**
 *
 */
public class Deutsch {

    private static final String PROJECT_DIR = "/home/jhrcek/Temp/AnkiDeutschPron/";
    private static final String DOWNLOAD_DIR = PROJECT_DIR + "Downloaded";
    private static final String INPUT_FILE = PROJECT_DIR + "in.txt";

    public static void main(String[] args) throws IOException {
        Map<String, String> word2pronURL = new HashMap<>();
        List<String> wordsNotFound = new ArrayList<>();
        List<String> pronNotAvailable = new ArrayList<>();
        List<String> unknownErrors = new ArrayList<>();

        DWDS dwds = new DWDS();
        List<String> words = FileUtils.readLines(new File(INPUT_FILE));

        for (String word : words) {
            DWDS.SearchResult sr = dwds.search(word);
            switch (sr) {
                case PRON_FOUND:
                    word2pronURL.put(word, dwds.getPronURL());
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

        dwds.close();

        //Phase 2: download prons based on URL
        for (Map.Entry<String, String> entry : word2pronURL.entrySet()) {
            downloadAndRenamePronFile(entry.getKey(), entry.getValue());
        }

        System.out.println("----- REPORT -----");
        if (!word2pronURL.keySet().isEmpty()) {
            System.out.println(" -- OK : " + word2pronURL.keySet().size());
            System.out.println(word2pronURL.keySet());
        }
        if (!wordsNotFound.isEmpty()) {
            System.out.println(" -- not found: " + wordsNotFound.size());
            System.out.println(wordsNotFound);
        }
        if (!pronNotAvailable.isEmpty()) {
            System.out.println(" -- pron not available:: " + pronNotAvailable.size());
            System.out.println(pronNotAvailable);
        }
        if (!unknownErrors.isEmpty()) {
            System.out.println(" -- unknown errors: " + unknownErrors.size());
            System.out.println(unknownErrors);
        }
    }

    private static void downloadAndRenamePronFile(String word, String pronUrl) {
        ProcessBuilder pb = new ProcessBuilder("wget", pronUrl);
        pb.directory(new File(DOWNLOAD_DIR));

        Process p;
        try {
            p = pb.start();
            System.out.println("Downloading pronunciation of word '" + word + "' from URL '" + pronUrl + "'");
            p.waitFor();

            String expectedFilename = getSimpleFilename(pronUrl);
            File downloadedPronFile = new File(DOWNLOAD_DIR, expectedFilename);
            System.out.println("Checking for presence of '" + expectedFilename + (downloadedPronFile.exists()
                    ? "' - OK, present" : "' - NOT PRESENT!"));

            downloadedPronFile.renameTo(new File(DOWNLOAD_DIR, word + ".mp3"));

        } catch (IOException | InterruptedException ex) {
            System.err.
                    println("Error downloading pron for '" + word + "' from URL '" + pronUrl + "' because of \n" + ex);
        }
    }

    private static String getSimpleFilename(String url) {
        return url.substring(url.lastIndexOf("/"));
    }
}
