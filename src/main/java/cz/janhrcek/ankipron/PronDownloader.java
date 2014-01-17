package cz.janhrcek.ankipron;

import cz.janhrcek.ankipron.search.DWDS;
import cz.janhrcek.ankipron.search.SearchResult;
import cz.janhrcek.ankipron.search.Searcher;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.firefox.FirefoxDriver;

import static cz.janhrcek.ankipron.search.SearchResult.PRON_FOUND;
import static cz.janhrcek.ankipron.search.SearchResult.PRON_NOT_AVAILABLE;
import static cz.janhrcek.ankipron.search.SearchResult.WORD_NOT_FOUND;

public class PronDownloader {

    public void performDownload(List<String> wordsToDownload) {
        System.out.println(wordsToDownload.size() + " words to download");
        Map<String, String> word2pronURL = new HashMap<>();
        List<String> wordsNotFound = new ArrayList<>();
        List<String> pronNotAvailable = new ArrayList<>();
        List<String> unknownErrors = new ArrayList<>();

        Searcher searcher = new DWDS(new FirefoxDriver());

        for (String word : wordsToDownload) {
            SearchResult sr = searcher.search(word);
            switch (sr) {
                case PRON_FOUND:
                    word2pronURL.put(word, searcher.getPronURL());
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

        searcher.close();

        //Phase 2: download prons based on URL
        List<String> failedRenames = new ArrayList<>();
        for (Map.Entry<String, String> entry : word2pronURL.entrySet()) {
            boolean downloadAndRenameSuccesfull = downloadAndRenamePronFile(entry.getKey(), entry.getValue());
            if (!downloadAndRenameSuccesfull) {
                failedRenames.add(entry.getKey());
            }
        }

        System.out.println("----- REPORT -----");
        if (!word2pronURL.keySet().isEmpty()) {
            System.out.printf("OK : %d %s%n", word2pronURL.keySet().size(), word2pronURL.keySet());
        }
        if (!wordsNotFound.isEmpty()) {
            System.out.printf("NOT FOUND : %d %s%n", wordsNotFound.size(), wordsNotFound);
        }
        if (!pronNotAvailable.isEmpty()) {
            System.out.printf("PRON NOT AVAILABLE : %d %s%n", pronNotAvailable.size(), pronNotAvailable);
        }
        if (!unknownErrors.isEmpty()) {
            System.out.printf("UNKNOWN ERRORS : %d %s%n", unknownErrors.size(), unknownErrors);
        }
        if (!failedRenames.isEmpty()) {
            System.out.printf("FAILED TO RENAME: %d %s%n", failedRenames.size(), failedRenames);
        }
    }

    private boolean downloadAndRenamePronFile(String word, String pronUrl) {
        ProcessBuilder pb = new ProcessBuilder("wget", pronUrl);
        pb.directory(Project.getDownloadDir());

        boolean renameSuccessfull = false;
        Process p;
        try {
            p = pb.start();
            System.out.println("Downloading pronunciation of word '" + word + "' from URL '" + pronUrl + "'");
            p.waitFor();

            String expectedFilename = getSimpleFilename(pronUrl);
            File downloadedPronFile = new File(Project.getDownloadDir(), expectedFilename);
            System.out.println("Checking for presence of '" + expectedFilename + (downloadedPronFile.exists()
                    ? "' - OK, present" : "' - NOT PRESENT!"));
            renameSuccessfull = downloadedPronFile.renameTo(new File(Project.getDownloadDir(), word + ".mp3"));
        } catch (IOException | InterruptedException ex) {
            System.err.
                    println("Error downloading pron for '" + word + "' from URL '" + pronUrl + "' because of \n" + ex);
        }
        return renameSuccessfull;
    }

    private static String getSimpleFilename(String url) {
        return url.substring(url.lastIndexOf("/"));
    }
}
